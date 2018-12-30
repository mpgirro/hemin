module Page.Home exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import FeatherIcons
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Attributes.Aria exposing (ariaLabel, role)
import Html.Events exposing (onClick, onInput)
import Html.Events.Extra exposing (onEnter)
import Http
import Skeleton exposing (Page)



-- MODEL


type Model
    = Failure Http.Error
    | Loading
    | Content


init : ( Model, Cmd Msg )
init =
    ( Loading, Cmd.none )


type Msg
    = LoadHome
    | LoadedHome (Result Http.Error String)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadHome ->
            ( model, Cmd.none )

        LoadedHome result ->
            case result of
                Ok podcast ->
                    ( Content, Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )


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
                , viewNewestEpisodes
                , viewRecentlyAddedPodcast
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


viewNewestEpisodes : Html Msg
viewNewestEpisodes =
    div []
        [ div [ class "Subhead", class "Subhead--spacious" ]
            [ div [ class "Subhead-heading" ] [ text "Latests Episodes" ]
            , div [ class "Subhead-actions" ]
                [ a [ href "" ] [ text "more" ]
                ]
            ]
        , text "TODO"
        ]


viewRecentlyAddedPodcast : Html Msg
viewRecentlyAddedPodcast =
    div []
        [ div [ class "Subhead", class "Subhead--spacious" ]
            [ div [ class "Subhead-heading" ] [ text "Poscasts new to Hemin" ]
            , div [ class "Subhead-actions" ]
                [ a [ href "" ] [ text "more" ]
                ]
            ]
        , text "TODO"
        ]


viewNews : Html Msg
viewNews =
    div []
        [ div [ class "Subhead", class "Subhead--spacious" ]
            [ div [ class "Subhead-heading" ] [ text "News" ]
            ]
        , text "TODO"
        ]
