module Page.Home exposing
    ( Model
    , Msg
    , emptyModel
    , init
    , update
    , view
    )

import Browser
import Browser.Navigation
import Const
import Data.DatabaseStats exposing (DatabaseStats)
import Data.Episode exposing (Episode)
import Data.Podcast exposing (Podcast)
import FeatherIcons
import Html exposing (Html, a, button, div, img, input, li, p, span, text, ul, h1, h2)
import Html.Attributes exposing (alt, attribute, autocomplete, class, height, href, placeholder, spellcheck, src, type_, value, width)
import Html.Attributes.Aria exposing (ariaLabel, role)
import Html.Events exposing (onClick, onInput, onSubmit)
import Html.Events.Extra exposing (onEnter)
import Http
import Page.Error as ErrorPage
import RemoteData exposing (WebData)
import RestApi
import Router exposing (redirectToEpisode, redirectToPodcast)
import Skeleton exposing (Page)
import Util exposing (emptyHtml, maybeAsString)



---- MODEL ----


type alias Model =
    { navigationKey : Maybe Browser.Navigation.Key
    , searchQuery : Maybe String
    , newestPodcasts : WebData (List Podcast)
    , latestEpisodes : WebData (List Episode)
    , databaseStats : WebData DatabaseStats
    }


emptyModel : Model
emptyModel =
    { navigationKey = Nothing
    , searchQuery = Nothing
    , newestPodcasts = RemoteData.NotAsked
    , latestEpisodes = RemoteData.NotAsked
    , databaseStats = RemoteData.NotAsked
    }


init : Maybe Browser.Navigation.Key -> ( Model, Cmd Msg )
init key =
    let
        cmd : Cmd Msg
        cmd =
            Cmd.batch
                [ getNewestPodcasts 1 6
                , getLatestEpisodes 1 6
                , getDatabaseStats
                ]
    in
    ( { emptyModel | navigationKey = key }, cmd )



---- UPDATE ----


type Msg
    = GotNewestPodcastListData (WebData (List Podcast))
    | GotLatestEpisodeListData (WebData (List Episode))
    | GotDatabaseStats (WebData DatabaseStats)
    | RedirectToSearch
    | UpdateModel Model


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateModel m ->
            ( m, Cmd.none)

        GotNewestPodcastListData newestPodcasts ->
            ( { model | newestPodcasts = newestPodcasts }, Cmd.none )

        GotLatestEpisodeListData latestEpisodes ->
            ( { model | latestEpisodes = latestEpisodes }, Cmd.none )

        GotDatabaseStats stats ->
            ( { model | databaseStats = stats }, Cmd.none )

        RedirectToSearch ->
            case (model.searchQuery, model.navigationKey) of
                (Just query, Just key) ->
                    ( model, Browser.Navigation.pushUrl key ("/search?q=" ++ query) )

                (_, _) ->
                    ( model, Cmd.none )


updateModelQuery : Model -> String -> Msg
updateModelQuery model query =
    UpdateModel { model | searchQuery = Just query }


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Home"

        body : Html Msg
        body =
            div []
                [ viewPageTitle
                , viewSearchForm model
                , viewNavButtonRow
                , viewLatestEpisodes model.latestEpisodes
                , viewNewestPodcast model.newestPodcasts
                , viewNews
                ]
    in
    ( title, body )

viewPageTitle : Html Msg
viewPageTitle =
    div [ class "text-center" ]
        [ h1 [ class "f1" ] [ text Const.siteName ]
        , h2 [ class "f3" ] [ text "Podcast Catalog & Search Engine" ]
        ]

viewSearchForm : Model -> Html Msg
viewSearchForm model =
    Html.form
        [ class "col-md-10"
        , class "mx-auto"
        , onSubmit RedirectToSearch
        ]
        [ div
            [ class "input-group"
            , class "mt-5"
            ]
            [ viewSearchInput model
            , viewSearchButton model
            ]
        , viewSearchNote model.databaseStats
        ]


viewSearchInput : Model -> Html Msg
viewSearchInput model =
    input
        [ class "form-control"
        , attribute "style" "height: 44px !important"
        , type_ "text"
        , value (maybeAsString model.searchQuery)
        , placeholder "What are you looking for?"
        , autocomplete False
        , spellcheck False
        , onInput (updateModelQuery model)
        ]
        []


viewSearchButton : Model -> Html Msg
viewSearchButton model =
    div [ class "input-group-append" ]
        [ button
            [ class "btn"
            , class "btn-outline-primary"
            , type_ "button"
            , ariaLabel "Search"
            , onClick RedirectToSearch
            ]
            [ FeatherIcons.search
                |> FeatherIcons.toHtml []
            ]
        ]


viewSearchNote : WebData DatabaseStats -> Html Msg
viewSearchNote dbStats =
    let
        info : DatabaseStats -> String
        info stats =
            (String.fromInt stats.episodeCount) ++ " episodes in " ++ (String.fromInt stats.podcastCount) ++ " podcasts"
    in
    case dbStats of
        RemoteData.NotAsked ->
            Skeleton.viewInitializingText

        RemoteData.Loading ->
           Skeleton.viewLoadingText

        RemoteData.Failure error ->
            --ErrorPage.viewHttpFailure error
            emptyHtml

        RemoteData.Success stats ->
            div [ class "note"
                , class "text-center"
                , class "mt-2"
                ]
                [ text (info stats) ]


viewNavButtonRow : Html Msg
viewNavButtonRow =
    div
        [ class "mt-5"
        , class "d-flex"
        , class "flex-justify-center"
        ]
        [ p [ class "f3-light", class "mr-5" ]
            [ a
                [ class "btn"
                , class "btn-large"
                , class "btn-outline-primary"
                , href ""
                , role "button"
                ]
                [ FeatherIcons.tag
                    |> FeatherIcons.toHtml [ width 18, height 18 ]
                , span [ class "ml-2" ] [ text "Categories" ]
                ]
            ]
        , p [ class "f3-light" ]
            [ a
                [ class "btn"
                , class "btn-large"
                , class "btn-outline-primary"
                , href ""
                , role "button"
                ]
                [ FeatherIcons.clock
                    |> FeatherIcons.toHtml [ width 18, height 18 ]
                , span [ class "ml-2" ] [ text "Recent" ]
                ]
            ]
        , p [ class "f3-light", class "ml-5" ]
            [ a
                [ class "btn"
                , class "btn-large"
                , class "btn-outline-primary"
                , href "/discover"
                , role "button"
                ]
                [ FeatherIcons.grid
                    |> FeatherIcons.toHtml [ width 18, height 18 ]
                , span [ class "ml-2" ] [ text "All Podcasts" ]
                ]
            ]
        ]


viewLatestEpisodes : WebData (List Episode) -> Html Msg
viewLatestEpisodes latestEpisodes =
    let
        tooltip : Episode -> String
        tooltip episode =
            case episode.title of
                Just title ->
                    title

                Nothing ->
                    ""

        viewEpisodeCover : Episode -> Html Msg
        viewEpisodeCover episode =
            li [ class "d-inline-block", class "col-2" ]
                [ a
                    [ href (redirectToEpisode episode)
                    , class "tooltipped"
                    , class "tooltipped-multiline"
                    , class "tooltipped-s"
                    , ariaLabel (tooltip episode)
                    ]
                    [ img
                        [ class "img-thumbnail"
                        , class "avatar"
                        , src (maybeAsString episode.image)
                        , alt (maybeAsString episode.title)
                        ]
                        []
                    ]
                ]

        viewCoverGrid : Html Msg
        viewCoverGrid =
            case latestEpisodes of
                RemoteData.NotAsked ->
                    Skeleton.viewInitializingText

                RemoteData.Loading ->
                    Skeleton.viewLoadingText

                RemoteData.Failure error ->
                    ErrorPage.viewHttpFailure error

                RemoteData.Success es ->
                    ul [ class "list-style-none" ] <|
                        List.map viewEpisodeCover es
    in
    div []
        [ div [ class "Subhead", class "Subhead--spacious" ]
            [ div [ class "Subhead-heading" ] [ text "Latests Episodes" ]
            , div [ class "Subhead-actions" ]
                [ a [ href "" ] [ text "more" ]
                ]
            ]
        , viewCoverGrid
        ]


viewNewestPodcast : WebData (List Podcast) -> Html Msg
viewNewestPodcast newestPodcasts =
    let
        tooltip : Podcast -> String
        tooltip podcast =
            case podcast.title of
                Just title ->
                    title

                Nothing ->
                    ""

        viewPodcastCover : Podcast -> Html Msg
        viewPodcastCover podcast =
            li [ class "d-inline-block", class "col-2" ]
                [ a
                    [ href (redirectToPodcast podcast)
                    , class "tooltipped"
                    , class "tooltipped-multiline"
                    , class "tooltipped-s"
                    , ariaLabel (tooltip podcast)
                    ]
                    [ img
                        [ class "img-thumbnail"
                        , class "avatar"
                        , src (maybeAsString podcast.image)
                        , alt (maybeAsString podcast.title)
                        ]
                        []
                    ]
                ]

        viewCoverGrid : Html Msg
        viewCoverGrid =
            case newestPodcasts of
                RemoteData.NotAsked ->
                    Skeleton.viewInitializingText

                RemoteData.Loading ->
                    Skeleton.viewLoadingText

                RemoteData.Failure error ->
                    ErrorPage.viewHttpFailure error

                RemoteData.Success ps ->
                    ul [ class "list-style-none" ] <|
                        List.map viewPodcastCover ps
    in
    div []
        [ div [ class "Subhead", class "Subhead--spacious" ]
            [ div [ class "Subhead-heading" ] [ text "Podcasts new to Hemin" ]
            , div [ class "Subhead-actions" ]
                [ a [ href "" ] [ text "more" ]
                ]
            ]
        , viewCoverGrid
        ]


viewNews : Html Msg
viewNews =
    div []
        [ div [ class "Subhead", class "Subhead--spacious" ]
            [ div [ class "Subhead-heading" ] [ text "News" ]
            ]
        , ul [ class "ml-5" ]
            [ li [] [ a [ href "" ] [ text "Great news, everyone!" ] ]
            ]
        ]



--- HTTP ---


getNewestPodcasts : Int -> Int -> Cmd Msg
getNewestPodcasts pageNumber pageSize =
    RestApi.getNewestPodcasts (RemoteData.fromResult >> GotNewestPodcastListData) pageNumber pageSize


getLatestEpisodes : Int -> Int -> Cmd Msg
getLatestEpisodes pageNumber pageSize =
    RestApi.getLatestEpisodes (RemoteData.fromResult >> GotLatestEpisodeListData) pageNumber pageSize


getDatabaseStats : Cmd Msg
getDatabaseStats =
    RestApi.getDatabaseStats (RemoteData.fromResult >> GotDatabaseStats)
