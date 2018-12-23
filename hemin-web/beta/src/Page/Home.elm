module Page.Home exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import Html exposing (..)
import Html.Attributes exposing (..)
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
            div [] [ p [] [ text "Home Page" ] ]
    in
    ( title, body )
