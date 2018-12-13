module Page.SearchPage exposing (Model(..), Msg(..), getSearchResult, init, main, subscriptions, update, view)

import Browser
import Episode exposing (..)
import Html exposing (Attribute, Html, b, br, div, h1, input, li, p, text, ul)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)
import Http
import Json.Decode exposing (Decoder, bool, field, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)
import Podcast exposing (..)
import RestApi
import SearchResult exposing (..)
import Skeleton exposing (Page)



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


view : Model -> Html msg
view model =
    case model of
        Failure cause ->
            Skeleton.viewHttpFailure cause

        Loading ->
            text "Loading..."

        Content searchResult ->
            viewSearchResult searchResult


viewSearchResult : ResultPage -> Html msg
viewSearchResult searchResult =
    div []
        [ p [] [ text "Search Results Page" ]
        , p [] [ text ("currPage: " ++ String.fromInt searchResult.currPage) ]
        , p [] [ text ("maxPage: " ++ String.fromInt searchResult.maxPage) ]
        , p [] [ text ("totalHits: " ++ String.fromInt searchResult.totalHits) ]
        , ul [ class "list-style-none" ] <|
            List.map viewIndexDoc searchResult.results
        ]


viewIndexDoc : IndexDoc -> Html msg
viewIndexDoc doc =
    li [ class "py-2" ]
        [ div [ class "clearfix", class "p-2", class "border" ]
            [ div [ class "float-left", class "p-3", class "mr-3", class "bg-gray" ]
                [ text "Image" ]
            , div [ class "overflow-hidden" ]
                [ b [] [ text doc.title ]
                , br [] []
                , Skeleton.viewLink doc.link
                , p [] [ text doc.description ]
                ]
            ]
        ]
                
            
            



-- HTTP


getSearchResult : Maybe String -> Maybe Int -> Maybe Int -> Cmd Msg
getSearchResult query pageNumber pageSize =
    RestApi.getSearchResult LoadedSearchResult query pageNumber pageSize
