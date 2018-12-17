module Page.Propose exposing (Model(..), Msg(..), update, view)

import Html exposing (Html, form, input, div, text, p, span, button)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)
import Html.Attributes.Aria exposing (..)
import Http
import Skeleton exposing (Page)



-- MODEL


type Model
    = Failure Http.Error
    | Ready


type Msg
    = Propose String


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        Propose url ->
            ( Ready, Cmd.none )


view : Model -> Html Msg
view model =
    div []
        [ p [] [ text "Please submit the URL to the feed of the podcast that you want to add to HEMIN:" ]
        , Html.form []
            [ div [ class "input-group" ]
                [ viewInput
                , viewSubmitButton
                ]
            ]

        ]

viewInput : Html Msg
viewInput =
    let
        placeholderValue = "Enter the feed to propose here"
    in
    input
        [ class "form-control"
        , class "input-block"
        , type_ "text"
        , placeholder placeholderValue
        , ariaLabel placeholderValue
        , onInput Propose
        ]
        []

viewSubmitButton : Html Msg
viewSubmitButton =
    span [ class "input-group-button" ]
        [ button
            [ class "btn"
            , type_ "button"
            , ariaLabel "Submit"
            ]
            [ text "Submit" ]
        ]
