module Page.Search exposing (Model(..), Msg(..), SearchState, getSearchResult, redirectLocalUrl, update, view)

import Browser
import Browser.Navigation
import Data.Episode exposing (Episode, episodeDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder)
import Data.ResultPage exposing (ResultPage, resultPageDecoder)
import Html exposing (Attribute, Html, a, b, br, div, form, h1, input, li, p, span, text, ul)
import Html.Attributes exposing (..)
import Html.Events exposing (keyCode, on, onInput, onSubmit)
import Html.Events.Extra exposing (onEnter)
import Http
import Json.Decode exposing (Decoder, bool, field, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)
import Maybe.Extra
import RestApi
import Skeleton exposing (Page)
import Url exposing (Url)
import Util exposing (emptyHtml, maybeAsString, maybeAsText, maybePageNumberParam, maybePageSizeParam, maybeQueryParam)



--- MODEL ---


type Model
    = Failure Http.Error
    | Loading SearchState
    | Content SearchState


type alias SearchState =
    { key : Maybe Browser.Navigation.Key
    , query : Maybe String
    , pageNumber : Maybe Int
    , pageSize : Maybe Int
    , results : Maybe ResultPage
    }



--- UPDATE ---


type Msg
    = UpdateState SearchState
    | UpdateSearchUrl (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | LoadSearchResult (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | LoadedSearchResult (Result Http.Error ResultPage)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateState state ->
            case model of
                Failure cause ->
                    -- TODO this is bullshit and must be changed
                    ( Failure cause, Cmd.none )

                _ ->
                    ( Loading state, Cmd.none )

        UpdateSearchUrl key query pageNumber pageSize ->
            let
                s : SearchState
                s =
                    stateFromParams key query pageNumber pageSize
            in
            ( Loading s, redirectLocalUrl s )

        LoadSearchResult key query pageNumber pageSize ->
            let
                s : SearchState
                s =
                    stateFromParams key query pageNumber pageSize
            in
            ( Loading s, getSearchResult query pageNumber pageSize )

        -- TODO send HTTP request!
        LoadedSearchResult result ->
            case result of
                Ok searchResult ->
                    -- This is a weired special case: we should not be receiving a search results if our
                    -- previous state was a failure. But hey, if we get something, just display it.
                    case model of
                        Failure _ ->
                            let
                                s : SearchState
                                s =
                                    { key = Nothing
                                    , query = Nothing
                                    , pageNumber = Nothing
                                    , pageSize = Nothing
                                    , results = Just searchResult
                                    }
                            in
                            ( Content s, Cmd.none )

                        Loading state ->
                            ( Content { state | results = Just searchResult }, Cmd.none )

                        -- TODO should I be receiving a Loaded msg in a Content state anyway?
                        Content state ->
                            ( Content { state | results = Just searchResult }, Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )


updateStateQuery : SearchState -> String -> Msg
updateStateQuery state query =
    UpdateState { state | query = Just query }


updateSearchUrl : SearchState -> Msg
updateSearchUrl state =
    UpdateSearchUrl state.key state.query Nothing Nothing


redirectLocalUrl : SearchState -> Cmd Msg
redirectLocalUrl state =
    let
        q =
            maybeQueryParam state.query

        p =
            maybePageNumberParam state.pageNumber

        s =
            maybePageSizeParam state.pageSize

        params =
            "" ++ String.join "&" (Maybe.Extra.values [ q, p, s ])

        urlQuery =
            if params == "" then
                ""

            else
                "?" ++ params

        path =
            "/search"
                ++ urlQuery
    in
    case state.key of
        Just key ->
            Browser.Navigation.pushUrl key path

        Nothing ->
            Cmd.none



--- VIEW ---


view : Model -> Html Msg
view model =
    case model of
        Failure cause ->
            Skeleton.viewHttpFailure cause

        Loading state ->
            div [ class "col-md-10", class "p-2", class "mx-auto" ]
                [ viewSearchInput state ]

        Content state ->
            case state.results of
                Nothing ->
                    div [ class "col-md-10", class "p-2", class "mx-auto" ]
                        [ viewSearchInput state ]

                Just searchResults ->
                    div [ class "col-md-10", class "p-2", class "mx-auto" ]
                        [ viewSearchInput state
                        , viewSearchResult searchResults
                        ]


viewSearchInput : SearchState -> Html Msg
viewSearchInput state =
    input
        [ class "form-control"
        , class "input-block"
        , type_ "text"
        , value (maybeAsString state.query)
        , placeholder "Search for podcasts/episodes"
        , onInput (updateStateQuery state)
        , onEnter (updateSearchUrl state)
        ]
        []



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
        , ul [ class "list-style-none" ] <|
            List.map viewIndexDoc searchResult.results
        ]


viewIndexDoc : IndexDoc -> Html Msg
viewIndexDoc doc =
    li [ class "py-2" ]
        [ div [ class "clearfix", class "p-2", class "border" ]
            [ div [ class "float-left", class "p-3", class "mr-3", class "bg-gray" ]
                [ text "Image" ]
            , div [ class "overflow-hidden" ]
                [ viewIndexDocTitleAsLink doc
                , br [] []
                , viewDocType doc
                , p [] [ maybeAsText doc.description ]
                ]
            ]
        ]


viewIndexDocTitleAsLink : IndexDoc -> Html Msg
viewIndexDocTitleAsLink doc =
    let
        path : String
        path =
            case doc.docType of
                "podcast" ->
                    "/p/" ++ doc.id

                "episode" ->
                    "/e/" ++ doc.id

                _ ->
                    ""
    in
    a [ href path, class "f3" ]
        [ maybeAsText doc.title ]


viewDocType : IndexDoc -> Html Msg
viewDocType doc =
    case doc.docType of
        "podcast" ->
            span [ class "Label", class "bg-yellow" ] [ text "PODCAST" ]

        "episode" ->
            span [ class "Label", class "bg-blue" ] [ text "EPISODE" ]

        _ ->
            emptyHtml



--- HTTP ---


getSearchResult : Maybe String -> Maybe Int -> Maybe Int -> Cmd Msg
getSearchResult query pageNumber pageSize =
    case ( query, pageNumber, pageSize ) of
        ( Nothing, Nothing, Nothing ) ->
            Cmd.none

        ( _, _, _ ) ->
            RestApi.getSearchResult LoadedSearchResult query pageNumber pageSize



--- INTERNAL HELPERS ---


stateFromParams : Maybe Browser.Navigation.Key -> Maybe String -> Maybe Int -> Maybe Int -> SearchState
stateFromParams key query pageNumber pageSize =
    { key = key
    , query = query
    , pageNumber = pageNumber
    , pageSize = pageSize
    , results = Nothing
    }
