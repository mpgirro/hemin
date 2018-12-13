module Main exposing (Model, Msg(..), init, main, subscriptions, update, view, viewHomePage, viewLink, viewNotFound, viewResultPage)

--import RestApi

import Browser
import Browser.Navigation
import Episode exposing (Episode, episodeDecoder)
import EpisodePage
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Podcast exposing (Podcast, podcastDecoder)
import PodcastPage
import Router exposing (Route(..), fromUrl, parser)
import SearchResult exposing (IndexDoc, ResultPage, resultPageDecoder)
import Skeleton exposing (Page)
import Url exposing (Url)



---- PROGRAM ----


main : Program () Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlRequest = LinkClicked
        , onUrlChange = UrlChanged
        }



---- MODEL ----


type alias Model =
    { key : Browser.Navigation.Key
    , url : Url.Url
    , route : Route
    , content : Content
    }


type Content
    = Failure Http.Error
    | Loading
    | NotFound
    | HomeContent
    | PodcastContent PodcastPage.Model
    | EpisodeContent EpisodePage.Model
    | SearchResultContent ResultPage


init : () -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        model =
            { key = key
            , url = url
            , route = Router.fromUrl url
            , content = Loading
            }
    in
    ( model, Browser.Navigation.pushUrl key (Url.toString url) )



---- UPDATE ----


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | PodcastMsg PodcastPage.Msg
    | EpisodeMsg EpisodePage.Msg
    | LoadResultPage (Maybe String) (Maybe Int) (Maybe Int)
    | LoadedResultPage (Result Http.Error ResultPage)


update : Msg -> Model -> ( Model, Cmd Msg )
update message model =
    case message of
        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( model, Browser.Navigation.pushUrl model.key (Url.toString url) )

                Browser.External href ->
                    ( model, Browser.Navigation.load href )

        UrlChanged url ->
            let
                route =
                    Router.fromUrl url
            in
            updateUrlChanged { model | route = route }

        PodcastMsg msg ->
            updatePodcastContent model msg

        EpisodeMsg msg ->
            updateEpisodeContent model msg

        -- TODO outsource to utility func
        LoadResultPage query pageNumber pageSize ->
            ( model, getSearchResults query pageNumber pageSize )

        LoadedResultPage result ->
            case result of
                Ok resultPage ->
                    ( { model | content = SearchResultContent resultPage }, Cmd.none )

                Err cause ->
                    ( { model | content = Failure cause }, Cmd.none )



-- TODO outsource to utility func


updateUrlChanged : Model -> ( Model, Cmd Msg )
updateUrlChanged model =
    case model.route of
        HomePage ->
            ( { model | content = HomeContent }, Cmd.none )

        PodcastPage id ->
            ( { model | content = wrapPodcastModel PodcastPage.Loading }, wrapPodcastMsg (PodcastPage.getPodcast id) )

        EpisodePage id ->
            ( { model | content = wrapEpisodeModel EpisodePage.Loading }, wrapEpisodeMsg (EpisodePage.getEpisode id) )

        SearchPage query pageNumber pageSize ->
            ( { model | content = Loading }, getSearchResults query pageNumber pageSize )


updatePodcastContent : Model -> PodcastPage.Msg -> ( Model, Cmd Msg )
updatePodcastContent model msg =
    case model.content of
        PodcastContent content ->
            let
                ( model_, msg_ ) =
                    PodcastPage.update msg content
            in
            ( { model | content = wrapPodcastModel model_ }, wrapPodcastMsg msg_ )

        _ ->
            ( model, Cmd.none )


wrapPodcastModel : PodcastPage.Model -> Content
wrapPodcastModel model =
    PodcastContent model


wrapPodcastMsg : Cmd PodcastPage.Msg -> Cmd Msg
wrapPodcastMsg msg =
    Cmd.map PodcastMsg msg


updateEpisodeContent : Model -> EpisodePage.Msg -> ( Model, Cmd Msg )
updateEpisodeContent model msg =
    case model.content of
        EpisodeContent content ->
            let
                ( model_, msg_ ) =
                    EpisodePage.update msg content
            in
            ( { model | content = wrapEpisodeModel model_ }, wrapEpisodeMsg msg_ )

        _ ->
            ( model, Cmd.none )


wrapEpisodeModel : EpisodePage.Model -> Content
wrapEpisodeModel model =
    EpisodeContent model


wrapEpisodeMsg : Cmd EpisodePage.Msg -> Cmd Msg
wrapEpisodeMsg msg =
    Cmd.map EpisodeMsg msg



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



---- VIEW ----


view : Model -> Browser.Document Msg
view model =
    case model.content of
        Failure cause ->
            viewHttpFailurePage cause

        Loading ->
            Skeleton.viewLoadingPage

        NotFound ->
            viewNotFound

        HomeContent ->
            viewHomePage

        PodcastContent content ->
            Skeleton.view "Podcast" (PodcastPage.view content)

        EpisodeContent content ->
            Skeleton.view "Episode" (EpisodePage.view content)

        SearchResultContent resultPage ->
            viewResultPage resultPage


viewNotFound : Page msg
viewNotFound =
    let
        body =
            div [] [ p [] [ text "Not Found" ] ]
    in
    Skeleton.view "Not Found" body


viewHomePage : Page msg
viewHomePage =
    let
        body =
            div [] [ p [] [ text "Homepage" ] ]
    in
    Skeleton.view "HEMIN : Podcast Catalog & Search" body



-- TODO replace with propper impl.


viewResultPage : ResultPage -> Page msg
viewResultPage resultPage =
    let
        body =
            div [] 
                [ p [] [ text "Search Results Page" ] 
                , p [] [ text ("currPage: " ++ String.fromInt resultPage.currPage)  ]
                , p [] [ text ("maxPage: " ++ String.fromInt resultPage.maxPage)  ]
                , p [] [ text ("totalHits: " ++ String.fromInt resultPage.totalHits)  ]
                , ul [] <|
                    List.map viewIndexDoc resultPage.results
                ]
    in
    Skeleton.view "Search" body

viewIndexDoc : IndexDoc -> Html msg
viewIndexDoc doc =
    li []
        [ b [] [ text doc.title ]
        , br [] []
        , a [ href doc.link ] [ text doc.link ]
        , p [] [ text doc.description ]
        ]


viewLink : String -> Html msg
viewLink path =
    li []
        [ a [ href path ] [ text path ]
        ]


viewHttpFailurePage : Http.Error -> Page msg
viewHttpFailurePage cause =
    Skeleton.view "Error" (Skeleton.viewHttpFailure cause)



-- HTTP


getSearchResults : Maybe String -> Maybe Int -> Maybe Int -> Cmd Msg
getSearchResults query pageNumber pageSize =
    -- TODO args are currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/search.json"
        , expect = Http.expectJson LoadedResultPage resultPageDecoder
        }
