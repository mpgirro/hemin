module Page.Propose exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import Const
import Html exposing (Html, button, div, form, input, p, span, text)
import Html.Attributes exposing (autocomplete, class, placeholder, spellcheck, type_, value)
import Html.Attributes.Aria exposing (ariaLabel)
import Html.Events exposing (onClick, onInput, onSubmit)
import Http
import Page.Error as ErrorPage
import RestApi
import Skeleton exposing (Page)



-- MODEL


type Model
    = Failure Http.Error
    | FeedUrl String
    | Proposing String
    | Success String


init : ( Model, Cmd Msg )
init =
    let
        model : Model
        model =
            FeedUrl ""
    in
    ( model, Cmd.none )


type Msg
    = Propose
    | Proposed (Result Http.Error ())
    | NewUrl String


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NewUrl feed ->
            ( FeedUrl feed, Cmd.none )

        Propose ->
            case model of
                FeedUrl feed ->
                    ( Proposing feed, proposeFeed feed )

                _ ->
                    -- TODO is this the error handling I want?
                    ( FeedUrl "", Cmd.none )

        Proposed response ->
            case response of
                Ok _ ->
                    ( Success "", Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Search"

        body : Html Msg
        body =
            case model of
                FeedUrl url ->
                    viewForm url

                Proposing feed ->
                    div []
                        [ viewForm feed
                        , div [ class "Box", class "mt-3" ]
                            [ div
                                [ class "flash", class "flash-full", class "flash-warn" ]
                                [ text "Proposing..." ]
                            ]

                        --, p [ class "mt-2" ] [ text "Proposing..." ]
                        ]

                Success feed ->
                    div []
                        [ viewForm feed
                        , div [ class "Box", class "mt-3" ]
                            [ div
                                [ class "flash", class "flash-full", class "flash-success" ]
                                [ text "Feed successfully proposed. "
                                , text Const.siteName
                                , text " will process it shortly."
                                , text " If the feed is valid and not yet in our database, the podcast and episodes will be available soon."
                                ]
                            ]

                        --, p [ class "mt-2" ] [ text "Feed successfully proposed. HEMIN will process it shortly." ]
                        ]

                Failure cause ->
                    ErrorPage.view (ErrorPage.HttpFailure cause)
    in
    ( title, body )


viewForm : String -> Html Msg
viewForm url =
    div []
        [ p []
            [ text "Please submit the URL to the feed of the podcast that you want to add to "
            , text Const.siteName
            , text ":"
            ]
        , Html.form
            [ onSubmit Propose ]
            [ div [ class "input-group" ]
                [ viewInput url
                , viewSubmitButton
                ]
            ]
        ]


viewInput : String -> Html Msg
viewInput url =
    let
        placeholderValue =
            "Enter the feed to propose here"
    in
    input
        [ class "form-control"
        , class "input-block"
        , type_ "text"
        , value url
        , placeholder placeholderValue
        , autocomplete False
        , spellcheck False
        , ariaLabel placeholderValue
        , onInput NewUrl
        ]
        []


viewSubmitButton : Html Msg
viewSubmitButton =
    span [ class "input-group-button" ]
        [ button
            [ class "btn"
            , class "text-normal"

            --, attribute "style" "padding: 0"
            , type_ "button"
            , ariaLabel "Submit"
            , onClick Propose
            ]
            [ text "Submit" ]
        ]



--- HTTP ---


proposeFeed : String -> Cmd Msg
proposeFeed feed =
    RestApi.proposeFeed Proposed feed
