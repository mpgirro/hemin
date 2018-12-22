module Main exposing (Model, Msg, init, main, subscriptions, update, view, viewHomePage, viewNotFound)

--import RestApi

import Browser
import Browser.Navigation
import Const
import Data.Episode exposing (Episode, episodeDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder)
import Data.ResultPage exposing (ResultPage, resultPageDecoder)
import Html exposing (Html, div, p, text)
import Http
import Page.Discover as DiscoverPage
import Page.Episode as EpisodePage
import Page.Error as ErrorPage
import Page.Home as HomePage
import Page.Podcast as PodcastPage
import Page.Propose as ProposePage
import Page.Search as SearchPage
import Router exposing (Route(..), fromUrl, parser)
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
    = Loading
    | NotFound
    | HomeContent HomePage.Model
    | PodcastContent PodcastPage.Model
    | EpisodeContent EpisodePage.Model
    | SearchContent SearchPage.Model
    | DiscoverContent DiscoverPage.Model
    | ProposeContent ProposePage.Model


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
    | HomeMsg HomePage.Msg
    | PodcastMsg PodcastPage.Msg
    | EpisodeMsg EpisodePage.Msg
    | SearchMsg SearchPage.Msg
    | DiscoverMsg DiscoverPage.Msg
    | ProposeMsg ProposePage.Msg


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
            updateUrlChanged { model | route = Router.fromUrl url }

        HomeMsg msg ->
            updateHomeContent model msg

        PodcastMsg msg ->
            updatePodcastContent model msg

        EpisodeMsg msg ->
            updateEpisodeContent model msg

        SearchMsg msg ->
            updateSearchContent model msg

        DiscoverMsg msg ->
            updateDiscoverContent model msg

        ProposeMsg msg ->
            updateProposeContent model msg


updateUrlChanged : Model -> ( Model, Cmd Msg )
updateUrlChanged model =
    case model.route of
        HomePage ->
            -- TODO: here we do not dispatch a message that will replace the Loading model
            ( { model | content = wrapHomeContent HomePage.Loading }, Cmd.none )

        PodcastPage id ->
            let
                ( m, c ) =
                    PodcastPage.init id
            in
            ( { model | content = wrapPodcastContent m }, wrapPodcastMsg c )

        EpisodePage id ->
            let
                ( m, c ) =
                    EpisodePage.init id
            in
            ( { model | content = wrapEpisodeContent m }, wrapEpisodeMsg c )

        SearchPage query pageNum pageSize ->
            let
                state : SearchPage.SearchState
                state =
                    { key = Just model.key
                    , query = query
                    , pageNumber = pageNum
                    , pageSize = pageSize
                    , results = Nothing
                    }

                ( m, c ) =
                    SearchPage.init state
            in
            ( { model | content = wrapSearchContent m }, wrapSearchMsg c )

        DiscoverPage ->
            let
                ( m, c ) =
                    DiscoverPage.init
            in
            ( { model | content = wrapDiscoverContent m }, wrapDiscoverMsg c )

        ProposePage ->
            let
                ( m, c ) =
                    ProposePage.init
            in
            ( { model | content = wrapProposeContent m } , wrapProposeMsg c )


updateHomeContent : Model -> HomePage.Msg -> ( Model, Cmd Msg )
updateHomeContent model msg =
    case model.content of
        HomeContent content ->
            let
                ( m, c ) =
                    HomePage.update msg content
            in
            ( { model | content = wrapHomeContent m }, wrapHomeMsg c )

        _ ->
            ( model, Cmd.none )


updatePodcastContent : Model -> PodcastPage.Msg -> ( Model, Cmd Msg )
updatePodcastContent model msg =
    case model.content of
        PodcastContent content ->
            let
                ( m, c ) =
                    PodcastPage.update msg content
            in
            ( { model | content = wrapPodcastContent m }, wrapPodcastMsg c )

        _ ->
            ( model, Cmd.none )


updateEpisodeContent : Model -> EpisodePage.Msg -> ( Model, Cmd Msg )
updateEpisodeContent model msg =
    case model.content of
        EpisodeContent content ->
            let
                ( m, c ) =
                    EpisodePage.update msg content
            in
            ( { model | content = wrapEpisodeContent m }, wrapEpisodeMsg c )

        _ ->
            ( model, Cmd.none )


updateSearchContent : Model -> SearchPage.Msg -> ( Model, Cmd Msg )
updateSearchContent model msg =
    case model.content of
        SearchContent content ->
            let
                ( m, c ) =
                    SearchPage.update msg content
            in
            ( { model | content = wrapSearchContent m }, wrapSearchMsg c )

        _ ->
            ( model, Cmd.none )


updateDiscoverContent : Model -> DiscoverPage.Msg -> ( Model, Cmd Msg )
updateDiscoverContent model msg =
    case model.content of
        DiscoverContent content ->
            let
                ( m, c ) =
                    DiscoverPage.update msg content
            in
            ( { model | content = wrapDiscoverContent m }, wrapDiscoverMsg c )

        _ ->
            ( model, Cmd.none )


updateProposeContent : Model -> ProposePage.Msg -> ( Model, Cmd Msg )
updateProposeContent model msg =
    case model.content of
        ProposeContent content ->
            let
                ( m, c ) =
                    ProposePage.update msg content
            in
            ( { model | content = wrapProposeContent m }, wrapProposeMsg c )

        _ ->
            ( model, Cmd.none )



--- SUBSCRIPTIONS ---


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



---- VIEW ----


view : Model -> Browser.Document Msg
view model =
    case model.content of
        --Failure cause ->
        --    Skeleton.view "Error" (Skeleton.viewHttpFailure cause)
        Loading ->
            Skeleton.viewLoadingPage

        NotFound ->
            viewNotFound

        HomeContent content ->
            Skeleton.view "Home" (HomePage.view content)

        --viewHomePage
        PodcastContent content ->
            Skeleton.view "Podcast" (PodcastPage.view content)

        EpisodeContent content ->
            Skeleton.view "Episode" (EpisodePage.view content)

        SearchContent content ->
            Skeleton.view "Search" (wrapSearchHtml (SearchPage.view content))

        DiscoverContent content ->
            Skeleton.view "Discover" (DiscoverPage.view content)

        ProposeContent content ->
            Skeleton.view "Propose" (wrapProposeHtml (ProposePage.view content))


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
    Skeleton.view (Const.siteName ++ " : Podcast Catalog & Search") body



--- UTILITIES (for documenting type signatures) ---


wrapHomeContent : HomePage.Model -> Content
wrapHomeContent model =
    HomeContent model


wrapHomeMsg : Cmd HomePage.Msg -> Cmd Msg
wrapHomeMsg msg =
    Cmd.map HomeMsg msg


wrapPodcastContent : PodcastPage.Model -> Content
wrapPodcastContent model =
    PodcastContent model


wrapPodcastMsg : Cmd PodcastPage.Msg -> Cmd Msg
wrapPodcastMsg msg =
    Cmd.map PodcastMsg msg


wrapEpisodeContent : EpisodePage.Model -> Content
wrapEpisodeContent model =
    EpisodeContent model


wrapEpisodeMsg : Cmd EpisodePage.Msg -> Cmd Msg
wrapEpisodeMsg msg =
    Cmd.map EpisodeMsg msg


wrapSearchContent : SearchPage.Model -> Content
wrapSearchContent model =
    SearchContent model


wrapSearchMsg : Cmd SearchPage.Msg -> Cmd Msg
wrapSearchMsg msg =
    Cmd.map SearchMsg msg


wrapSearchHtml : Html SearchPage.Msg -> Html Msg
wrapSearchHtml msg =
    Html.map SearchMsg msg


wrapDiscoverContent : DiscoverPage.Model -> Content
wrapDiscoverContent model =
    DiscoverContent model


wrapDiscoverMsg : Cmd DiscoverPage.Msg -> Cmd Msg
wrapDiscoverMsg msg =
    Cmd.map DiscoverMsg msg


wrapProposeContent : ProposePage.Model -> Content
wrapProposeContent model =
    ProposeContent model


wrapProposeMsg : Cmd ProposePage.Msg -> Cmd Msg
wrapProposeMsg msg =
    Cmd.map ProposeMsg msg


wrapProposeHtml : Html ProposePage.Msg -> Html Msg
wrapProposeHtml msg =
    Html.map ProposeMsg msg
