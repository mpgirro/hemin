module Page.Propose exposing (Model(..), Msg(..), update, view)

import Html exposing (..)
import Html.Attributes exposing (..)
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


view : Model -> Html msg
view model =
    div [] [ p [] [ text "Propose Page" ] ]
