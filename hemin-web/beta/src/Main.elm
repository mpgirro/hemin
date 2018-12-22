module Main exposing
    ( Model
    , Msg
    , init
    , main
    , subscriptions
    , update
    , view
    , viewNotFound
    )

import Browser
import Browser.Navigation
import Const
import Data.Episode exposing (Episode, episodeDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder)
import Data.SearchResult exposing (SearchResult, searchResultDecoder)
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
    = NotFound
    | HomeContent HomePage.Model
    | PodcastContent PodcastPage.Model
    | EpisodeContent EpisodePage.Model
    | SearchContent SearchPage.Model
    | DiscoverContent DiscoverPage.Model
    | ProposeContent ProposePage.Model


init : () -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        ( content, _ ) =
            HomePage.init

        model =
            { key = key
            , url = url
            , route = Router.fromUrl url
            , content = HomeContent content
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
                ( c, m ) =
                    PodcastPage.init id
            in
            ( { model | content = wrapPodcastContent c }, wrapPodcastMsg m )

        EpisodePage id ->
            let
                ( c, m ) =
                    EpisodePage.init id
            in
            ( { model | content = wrapEpisodeContent c }, wrapEpisodeMsg m )

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

                ( c, m ) =
                    SearchPage.init state
            in
            ( { model | content = wrapSearchContent c }, wrapSearchMsg m )

        DiscoverPage ->
            let
                ( c, m ) =
                    DiscoverPage.init
            in
            ( { model | content = wrapDiscoverContent c }, wrapDiscoverMsg m )

        ProposePage ->
            let
                ( c, m ) =
                    ProposePage.init
            in
            ( { model | content = wrapProposeContent c }, wrapProposeMsg m )


updateHomeContent : Model -> HomePage.Msg -> ( Model, Cmd Msg )
updateHomeContent model msg =
    case model.content of
        HomeContent content ->
            let
                ( c, m ) =
                    HomePage.update msg content
            in
            ( { model | content = wrapHomeContent c }, wrapHomeMsg m )

        _ ->
            ( model, Cmd.none )


updatePodcastContent : Model -> PodcastPage.Msg -> ( Model, Cmd Msg )
updatePodcastContent model msg =
    case model.content of
        PodcastContent content ->
            let
                ( c, m ) =
                    PodcastPage.update msg content
            in
            ( { model | content = wrapPodcastContent c }, wrapPodcastMsg m )

        _ ->
            ( model, Cmd.none )


updateEpisodeContent : Model -> EpisodePage.Msg -> ( Model, Cmd Msg )
updateEpisodeContent model msg =
    case model.content of
        EpisodeContent content ->
            let
                ( c, m ) =
                    EpisodePage.update msg content
            in
            ( { model | content = wrapEpisodeContent c }, wrapEpisodeMsg m )

        _ ->
            ( model, Cmd.none )


updateSearchContent : Model -> SearchPage.Msg -> ( Model, Cmd Msg )
updateSearchContent model msg =
    case model.content of
        SearchContent content ->
            let
                ( c, m ) =
                    SearchPage.update msg content
            in
            ( { model | content = wrapSearchContent c }, wrapSearchMsg m )

        _ ->
            ( model, Cmd.none )


updateDiscoverContent : Model -> DiscoverPage.Msg -> ( Model, Cmd Msg )
updateDiscoverContent model msg =
    case model.content of
        DiscoverContent content ->
            let
                ( c, m ) =
                    DiscoverPage.update msg content
            in
            ( { model | content = wrapDiscoverContent c }, wrapDiscoverMsg m )

        _ ->
            ( model, Cmd.none )


updateProposeContent : Model -> ProposePage.Msg -> ( Model, Cmd Msg )
updateProposeContent model msg =
    case model.content of
        ProposeContent content ->
            let
                ( c, m ) =
                    ProposePage.update msg content
            in
            ( { model | content = wrapProposeContent c }, wrapProposeMsg m )

        _ ->
            ( model, Cmd.none )



--- SUBSCRIPTIONS ---


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



---- VIEW ----


view : Model -> Browser.Document Msg
view model =
    let
        ( title, body ) =
            viewContent model
    in
    Skeleton.view title body


viewContent : Model -> ( String, Html Msg )
viewContent model =
    case model.content of
        NotFound ->
            viewNotFound

        HomeContent content ->
            HomePage.view content

        PodcastContent content ->
            PodcastPage.view content

        EpisodeContent content ->
            EpisodePage.view content

        SearchContent content ->
            let
                ( title, body ) =
                    SearchPage.view content
            in
            ( title, wrapSearchHtml body )

        DiscoverContent content ->
            DiscoverPage.view content

        ProposeContent content ->
            let
                ( title, body ) =
                    ProposePage.view content
            in
            ( title, wrapProposeHtml body )


viewNotFound : ( String, Html msg )
viewNotFound =
    let
        body =
            div [] [ p [] [ text "Not Found" ] ]
    in
    ( "Not Found", body )



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
