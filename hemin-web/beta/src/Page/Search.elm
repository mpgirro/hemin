module Page.Search exposing (Model(..), Msg(..), getSearchResult, init, main, subscriptions, update, view)

import Browser
import Data.Episode exposing (Episode, episodeDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder)
import Data.ResultPage exposing (ResultPage, resultPageDecoder)
import Html exposing (Attribute, Html, b, br, div, form, h1, input, li, p, span, text, ul)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onSubmit)
import Http
import Json.Decode exposing (Decoder, bool, field, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)
import RestApi
import Skeleton exposing (Page)
import Util exposing (maybeAsString, maybeAsText)



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
    | Ready
    | Loading
    | Content ResultPage


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getSearchResult Nothing Nothing Nothing )



-- UPDATE


type Msg
    = LoadSearchResult (Maybe String) (Maybe Int) (Maybe Int)
    | LoadedSearchResult (Result Http.Error ResultPage)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadSearchResult query pageNumber pageSize ->
            ( model, getSearchResult query pageNumber pageSize )

        -- TODO send HTTP request!
        LoadedSearchResult result ->
            case result of
                Ok searchResult ->
                    ( Content searchResult, Cmd.none )

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
            Skeleton.viewHttpFailure cause

        Ready ->
            div [ class "col-md-10", class "p-2", class "mx-auto" ]
                [ viewSearchInput ]

        Loading ->
            text "Loading..."

        Content searchResult ->
            div [ class "col-md-10", class "p-2", class "mx-auto" ]
              [ viewSearchInput
              , viewSearchResult searchResult
              ]

viewSearchInput : Html Msg
viewSearchInput =
    input
                [ class "form-control"
                , class "input-block"
                , type_ "text"
                , placeholder "Search for podcasts/episodes"
                , onInput searchOnInput
                --, onSubmit searchOnInput
                ]
                []
    {--
    Html.form []
        [
        ]
    --}

searchOnInput : (String -> Msg)
searchOnInput query =
    LoadSearchResult (Just query) (Just 1) (Just 20)

-- TODO
-- next page if viable
--searchOnTurnPageOver : String -> Int -> Int -> Msg


-- TODO
-- previous page if viable
--searchOnTurnPageBack : String -> Int -> Int -> Msg

viewSearchResult : ResultPage -> Html Msg
viewSearchResult searchResult =
    div []
        [ p []
            [ text "Search Results Page" ]
        , span [ class "Label", class "Label--gray", class "mx-2" ]
            [ text ("currPage:" ++ String.fromInt searchResult.currPage) ]
        , span [ class "Label", class "Label--gray", class "mx-2" ]
            [ text ("maxPage:" ++ String.fromInt searchResult.maxPage) ]
        , span [ class "Label", class "Label--gray", class "mx-2" ]
            [ text ("totalHits:" ++ String.fromInt searchResult.totalHits) ]
        , ul [ class "list-style-none" ]
            <| List.map viewIndexDoc searchResult.results
        ]


viewIndexDoc : IndexDoc -> Html Msg
viewIndexDoc doc =
    li [ class "py-2" ]
        [ div [ class "clearfix", class "p-2", class "border" ]
            [ div [ class "float-left", class "p-3", class "mr-3", class "bg-gray" ]
                [ text "Image" ]
            , div [ class "overflow-hidden" ]
                [ b [] [ maybeAsText doc.title ]
                , br [] []
                , Skeleton.viewLink (maybeAsString doc.link)
                , p [] [ maybeAsText doc.description ]
                ]
            ]
        ]



-- HTTP


getSearchResult : Maybe String -> Maybe Int -> Maybe Int -> Cmd Msg
getSearchResult query pageNumber pageSize =
    case (query, pageNumber, pageSize) of
        (Nothing, Nothing, Nothing) ->
            Cmd.none
        (_, _, _) ->
            RestApi.getSearchResult LoadedSearchResult query pageNumber pageSize
