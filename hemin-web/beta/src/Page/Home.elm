module Page.Home exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import Data.Episode exposing (Episode)
import Data.Podcast exposing (Podcast)
import FeatherIcons
import Html exposing (Html, a, button, div, img, input, li, p, span, text, ul)
import Html.Attributes exposing (alt, attribute, autocomplete, class, height, href, placeholder, spellcheck, src, type_, value, width)
import Html.Attributes.Aria exposing (ariaLabel, role)
import Html.Events exposing (onClick, onInput)
import Html.Events.Extra exposing (onEnter)
import Http
import Page.Error as ErrorPage
import RemoteData exposing (WebData)
import Router exposing (redirectToEpisode, redirectToPodcast)
import Skeleton exposing (Page)
import Util exposing (emptyHtml, maybeAsString)



---- MODEL ----


type alias Model =
    { newestPodcasts : WebData (List Podcast)
    , latestEpisodes : WebData (List Episode)
    }


emptyModel : Model
emptyModel =
    { newestPodcasts = RemoteData.NotAsked
    , latestEpisodes = RemoteData.NotAsked
    }


init : ( Model, Cmd Msg )
init =
    ( emptyModel, Cmd.none )



---- UPDATE ----


type Msg
    = GotNewestPodcastListData (WebData (List Podcast))
    | GotLatestEpisodeListData (WebData (List Episode))


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GotNewestPodcastListData newestPodcasts ->
            ( { model | newestPodcasts = newestPodcasts }, Cmd.none )

        GotLatestEpisodeListData latestEpisodes ->
            ( { model | latestEpisodes = latestEpisodes }, Cmd.none )


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Home"

        body : Html Msg
        body =
            div []
                [ viewSearchForm
                , viewNavButtonRow
                , viewLatestEpisodes model.latestEpisodes
                , viewNewestPodcast model.newestPodcasts
                , viewNews
                ]
    in
    ( title, body )


viewSearchForm : Html Msg
viewSearchForm =
    Html.form [ class "col-md-10", class "p-2", class "mx-auto" ]
        [ div
            [ class "input-group"
            , class "mt-5"
            ]
            [ viewSearchInput
            , viewSearchButton
            ]
        , viewSearchNote
        ]


viewSearchInput : Html Msg
viewSearchInput =
    input
        [ class "form-control"
        , class "input"

        --, height 44
        , attribute "style" "height: 44px !important"
        , type_ "text"
        , value ""
        , placeholder "What are you looking for?"
        , autocomplete False
        , spellcheck False

        --, onInput (updateStateQuery state)
        --, onEnter (updateSearchUrl state)
        ]
        []


viewSearchButton : Html Msg
viewSearchButton =
    span [ class "input-group-button" ]
        [ button
            [ class "btn"
            , class "text-normal"
            , type_ "button"
            , ariaLabel "Search"

            --, onClick Propose
            ]
            [ FeatherIcons.search
                |> FeatherIcons.toHtml []
            ]
        ]


viewSearchNote : Html Msg
viewSearchNote =
    let
        episodeCount =
            "XXX"

        podcastCount =
            "YYY"

        msg =
            episodeCount ++ " episodes in " ++ podcastCount ++ " podcasts"
    in
    div
        [ class "note"
        , class "text-center"
        , class "mt-2"
        ]
        [ span [ class "Label", class "bg-red" ] [ text episodeCount ]
        , text " episodes in "
        , span [ class "Label", class "bg-red" ] [ text podcastCount ]
        , text " podcasts"
        ]


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
                , class "btn-outline"
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
                , class "btn-outline"
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
                , class "btn-outline"
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
        viewEpisodeCover : Episode -> Html Msg
        viewEpisodeCover episode =
            li [ class "d-inline-block", class "col-2", class "p-2" ]
                [ a [ href (redirectToEpisode episode) ]
                    [ img
                        [ class "width-full"
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
                    text "Initialising ..."

                RemoteData.Loading ->
                    text "Loading ..."

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
        viewPodcastCover : Podcast -> Html Msg
        viewPodcastCover podcast =
            li [ class "d-inline-block", class "col-2", class "p-2" ]
                [ a [ href (redirectToPodcast podcast) ]
                    [ img
                        [ class "width-full"
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
                    text "Initialising ..."

                RemoteData.Loading ->
                    text "Loading ..."

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
