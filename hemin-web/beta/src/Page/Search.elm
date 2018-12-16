module Page.Search exposing (Model(..), Msg(..), SearchState, getSearchResult, subscriptions, update, view, redirectLocalUrl)

import Browser
import Browser.Navigation
import Data.Episode exposing (Episode, episodeDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder)
import Data.ResultPage exposing (ResultPage, resultPageDecoder)
import Html exposing (Attribute, Html, b, br, div, form, h1, input, li, p, span, text, ul, a)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput, onSubmit)
import Http
import Json.Decode exposing (Decoder, bool, field, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)
import Maybe.Extra
import RestApi
import Skeleton exposing (Page)
import Url exposing (Url)
import Util exposing (maybeAsString, maybeAsText, emptyHtml)


-- MODEL


type Model
    = Failure Http.Error
--    | Ready
--    | Loading
--    | Content ResultPage
    | Loading SearchState
    | Content SearchState

type alias SearchState =
  { key : Maybe Browser.Navigation.Key
  , query : Maybe String
  , pageNumber : Maybe Int
  , pageSize : Maybe Int
  , results : Maybe ResultPage
  }


-- UPDATE


type Msg
    = UpdateSearchUrl (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | LoadSearchResult (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | LoadedSearchResult (Result Http.Error ResultPage)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateSearchUrl key query pageNumber pageSize ->
            case model of
                Failure cause ->
                    -- TODO this is bullshit and must be changed
                    (Failure cause, Cmd.none)

                Loading state ->
                    let
                        s : SearchState
                        s = { key = key
                            , query = query
                            , pageNumber = pageNumber
                            , pageSize = pageSize
                            , results = Nothing
                            }
                    in
                    ( Loading s, redirectLocalUrl s )

                Content state ->
                    let
                        s : SearchState
                        s = { key = key
                            , query = query
                            , pageNumber = pageNumber
                            , pageSize = pageSize
                            , results = Nothing
                            }
                    in
                    ( Loading s, redirectLocalUrl s )

        LoadSearchResult key query pageNumber pageSize ->
            let
                s : SearchState
                s = { key = key
                    , query = query
                    , pageNumber = pageNumber
                    , pageSize = pageSize
                    , results = Nothing
                    }
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
                                s = { key = Nothing
                                    , query = Nothing
                                    , pageNumber = Nothing
                                    , pageSize = Nothing
                                    , results = Just searchResult
                                    }
                            in
                            ( Content s, Cmd.none)

                        Loading state ->
                            ( Content { state | results = (Just searchResult) }, Cmd.none)

                        -- TODO should I be receiving a Loaded msg in a Content state anyway?
                        Content state ->
                            ( Content { state | results = (Just searchResult) }, Cmd.none)

                Err cause ->
                    ( Failure cause, Cmd.none )


redirectLocalUrl : SearchState -> Cmd Msg
redirectLocalUrl state =
    let
                q : Maybe String
                q = maybeQuery state.query

                p : Maybe String
                p = maybePageNumber state.pageNumber

                s : Maybe String
                s = maybePageSize state.pageSize

                params : String
                params = "" ++ String.join "&" (Maybe.Extra.values [q, p, s])

                url : String
                url = "/search" ++ ( if params == "" then "" else "?" ++ params  )
    in
    case state.key of
      Just key ->
        Browser.Navigation.pushUrl key url
      Nothing ->
        Cmd.none -- TODO


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

{--
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
--}
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
                , onInput (searchOnInput state)
                --, onInput (redirectLocalUrl state)
                --, onSubmit searchOnInput
                ]
                []


searchOnInput : SearchState -> String -> Msg
searchOnInput state query =
    UpdateSearchUrl state.key (Just query) (Just 1) (Just 20)

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
      url : String
      url =
          case doc.docType of
              "podcast" ->
                  "/p/" ++ doc.id
              "episode" ->
                  "/e/" ++ doc.id
              _ ->
                  ""
    in
    a [ href url, class "f3" ]
      [ maybeAsText doc.title ]


viewDocType : IndexDoc -> Html Msg
viewDocType doc =
    case doc.docType of
        "podcast" ->
          span [ class "Label", class "bg-yellow" ] [ text "PODCAST" ]
        "episode" ->
          span [ class "Label", class "bg-blue" ] [ text "EPISODE" ]
        _ -> emptyHtml


-- HTTP


maybeQuery : Maybe String -> Maybe String
maybeQuery query =
    case query of
        Just q ->
            Just ("q=" ++ q)
        Nothing ->
            Nothing

maybePageNumber : Maybe Int -> Maybe String
maybePageNumber pageNumber =
     case pageNumber of
         Just page ->
             Just ("p=" ++ String.fromInt page)
         Nothing ->
             Nothing

maybePageSize : Maybe Int -> Maybe String
maybePageSize pageSize =
     case pageSize of
         Just size ->
             Just ("s=" ++ String.fromInt size)
         Nothing ->
             Nothing

getSearchResult : Maybe String -> Maybe Int -> Maybe Int -> Cmd Msg
getSearchResult query pageNumber pageSize =
    case (query, pageNumber, pageSize) of
        (Nothing, Nothing, Nothing) ->
            Cmd.none
        (_, _, _) ->
            RestApi.getSearchResult LoadedSearchResult query pageNumber pageSize
