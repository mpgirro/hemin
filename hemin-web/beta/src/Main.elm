module Main exposing (Model, Msg(..), init, main, subscriptions, update, view, viewEpisodePage, viewHomePage, viewLink, viewNotFound, viewPodcastPage, viewResultPage)

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
import SearchPage
import SearchResult exposing (ResultPage, resultPageDecoder)
import Skeleton exposing (Page)
import Url exposing (Url)



-- MAIN


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



-- MODEL


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
    | PodcastContent Podcast
    | EpisodeContent Episode
    | SearchResultContent ResultPage


init : () -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init flags url key =
--    ( Model key url HomePage HomeContent, Cmd.none )
--    update (UrlChanged url) (Model key url (Router.fromUrl url) HomeContent)
    let
        model = 
            { key = key
            , url = url
            , route = Router.fromUrl url
            , content = Loading
            }
        --    Model key url (Router.fromUrl url) Loading
    in
    ( model, Browser.Navigation.pushUrl key (Url.toString url) )





-- UPDATE


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | LoadPodcast String
    | LoadedPodcast (Result Http.Error Podcast)
    | LoadEpisode String
    | LoadedEpisode (Result Http.Error Episode)
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

        --( { model | route = route }, Cmd.none ) -- (cmdFromRoute route)
        LoadPodcast id ->
            ( model, getPodcast id )

        LoadedPodcast result ->
            case result of
                Ok podcast ->
                    ( { model | content = PodcastContent podcast }, Cmd.none )

                Err cause ->
                    ( { model | content = Failure cause }, Cmd.none )

        -- TODO outsource to utility func
        LoadEpisode id ->
            ( model, getEpisode id )

        LoadedEpisode result ->
            case result of
                Ok episode ->
                    ( { model | content = EpisodeContent episode }, Cmd.none )

                Err cause ->
                    ( { model | content = Failure cause }, Cmd.none )

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
            ( { model | content = Loading }, getPodcast id )

        EpisodePage id ->
            ( { model | content = Loading }, getEpisode id )

        SearchPage query pageNumber pageSize ->
            ( { model | content = Loading }, getSearchResults query pageNumber pageSize )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- VIEW


view : Model -> Browser.Document Msg
view model =
    case model.content of
        Failure cause ->
            viewHttpFailurePage cause

        Loading ->
            viewLoadingPage

        NotFound ->
            viewNotFound

        HomeContent ->
            viewHomePage

        PodcastContent podcast ->
            viewPodcastPage podcast

        EpisodeContent episode ->
            viewEpisodePage episode

        SearchResultContent resultPage ->
            viewResultPage resultPage


viewLoadingPage : Page msg
viewLoadingPage =
    let
        body =
            div [] [ p [] [ text "Loading..." ] ]
    in
    Skeleton.view "Loading" body


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
    Skeleton.view "HEMIN | Home" body


viewPodcast : Podcast -> Html msg
viewPodcast podcast =
    div []
        [ h1 [] [ text podcast.title ]
        , a [ href podcast.link ] [ text podcast.link ]
        , p [] [ text podcast.description ]
        ]


viewPodcastPage : Podcast -> Page msg
viewPodcastPage podcast =
    Skeleton.view "Podcast" (viewPodcast podcast)


viewEpisode : Episode -> Html msg
viewEpisode episode =
    div []
        [ h1 [] [ text episode.title ]
        , a [ href episode.link ] [ text episode.link ]
        , p [] [ text episode.description ]
        ]


viewEpisodePage : Episode -> Page msg
viewEpisodePage episode =
    Skeleton.view "Episode" (viewEpisode episode)



-- TODO replace with propper impl.


viewResultPage : ResultPage -> Page msg
viewResultPage resultPage =
    let
        body =
            div [] [ p [] [ text "Search Results Page" ] ]
    in
    Skeleton.view "Search" body


viewLink : String -> Html msg
viewLink path =
    li []
        [ a [ href path ] [ text path ]
        ]


viewHttpFailurePage : Http.Error -> Page msg
viewHttpFailurePage cause =
    Skeleton.view "Error" (Skeleton.viewHttpFailure cause)



-- HTTP


getPodcast : String -> Cmd Msg
getPodcast id =
    -- TODO id is currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/podcast.json"
        , expect = Http.expectJson LoadedPodcast podcastDecoder
        }


getEpisode : String -> Cmd Msg
getEpisode id =
    -- TODO id is currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/episode.json" -- "http://localhost:9000/api/v1/episode/8DTKUxDwRO991"
        , expect = Http.expectJson LoadedEpisode episodeDecoder
        }


getSearchResults : Maybe String -> Maybe Int -> Maybe Int -> Cmd Msg
getSearchResults query pageNumber pageSize =
    -- TODO args are currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/search.json"
        , expect = Http.expectJson LoadedResultPage resultPageDecoder
        }
