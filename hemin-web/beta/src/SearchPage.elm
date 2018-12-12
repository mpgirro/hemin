module SearchPage exposing (Model(..), Msg(..), getResults, init, main, subscriptions, update, view, viewHttpFailure, viewResultPage)

import Browser
import Episode exposing (..)
import Html exposing (Attribute, Html, div, h1, input, text)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)
import Http
import Json.Decode exposing (Decoder, bool, field, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)
import Podcast exposing (..)
import SearchResult exposing (..)



-- MAIN


main =
    Browser.element
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        }



-- MODEL


type Model
    = Failure Http.Error
    | Empty
    | Loading
    | Success ResultPage


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getResults "" Nothing Nothing )



-- UPDATE


type Msg
    = SendSearchRequest String Int Int
    | GotSearchResult (Result Http.Error ResultPage)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SendSearchRequest query page size ->
            ( Empty, Cmd.none )

        -- TODO send HTTP request!
        GotSearchResult result ->
            case result of
                Ok resultPage ->
                    ( Success resultPage, Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- VIEW


view : Model -> Html Msg
view model =
    case model of
        Failure cause ->
            viewHttpFailure cause

        Empty ->
            text "Ready to search:"

        Loading ->
            text "Loading..."

        Success resultPage ->
            viewResultPage resultPage


viewHttpFailure : Http.Error -> Html Msg
viewHttpFailure cause =
    case cause of
        Http.BadUrl msg ->
            text ("Unable to load the search results; reason: " ++ msg)

        Http.Timeout ->
            text "Unable to load the search results; reason: timeout"

        Http.NetworkError ->
            text "Unable to load the search results; reason: network error"

        Http.BadStatus status ->
            text ("Unable to load the search results; reason: status " ++ String.fromInt status)

        Http.BadBody msg ->
            text ("Unable to load the search results; reason: " ++ msg)


viewResultPage : ResultPage -> Html Msg
viewResultPage page =
    div []
        [ text "Add results here"
        ]



-- HTTP


getResults : String -> Maybe Int -> Maybe Int -> Cmd Msg
getResults query pageNumber pageSize =
    -- TODO arguments are currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/search.json"
        , expect = Http.expectJson GotSearchResult resultPageDecoder
        }
