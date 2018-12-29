module Page.Home exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Attributes.Aria exposing (role)
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
                [ viewSearchInput
                , viewSearchNote
                , viewButtonRow
                , viewNewestEpisodes
                , viewRecentlyAddedPodcast
                , viewNews
                ]
    in
    ( title, body )


viewSearchInput : Html Msg
viewSearchInput =
    div []
        [ input
            [ class "form-control"
            , class "input-block"
            , class "input-lg"
            , class "mt-5"
            , type_ "text"
            , value ""
            , placeholder "What are you looking for?"
            , autocomplete False
            , spellcheck False

            --, onInput (updateStateQuery state)
            --, onEnter (updateSearchUrl state)
            ]
            []
        ]

viewSearchNote : Html Msg
viewSearchNote =
    let
      podcastCount = "XXX"

      episodeCount = "YYY"

      msg = episodeCount ++ " episodes in " ++ podcastCount ++ "podcasts"
    in
    div
        [ class "note"
        , class "text-center"
        , class "mt-2"
        ]
        [ text msg ]

viewButtonRow : Html Msg
viewButtonRow =
    div
        [ class "mt-5"
        , class "d-flex"
        , class "flex-justify-center"
        ]
        [ p [ class "f3", class "mr-5" ]
            [ a
                [ class "btn"
                , class "btn-large"
                , class "btn-outline-purple"
                , href ""
                , role "button"
                ]
                [ text "Categories" ]
            ]
        , p [ class "f3" ]
            [ a
                [ class "btn"
                , class "btn-large"
                , class "btn-outline-purple"
                , href ""
                , role "button"
                ]
                [ text "Recent" ]
            ]
        , p [ class "f3", class "ml-5" ]
            [ a
                [ class "btn"
                , class "btn-large"
                , class "btn-outline-purple"
                , href ""
                , role "button"
                ]
                [ text "All Podcasts" ]
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
