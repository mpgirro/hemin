module Page.Search exposing
    ( Model
    , Msg
    , SearchState
    , init
    , update
    , view
    )

import Browser
import Browser.Navigation
import Data.Episode exposing (Episode, episodeDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder)
import Data.SearchResult exposing (SearchResult, searchResultDecoder)
import FeatherIcons
import Html exposing (Attribute, Html, a, b, br, div, em, form, h1, img, input, li, nav, p, span, text, ul, button)
import Html.Attributes exposing (..)
import Html.Attributes.Aria exposing (..)
import Html.Events exposing (keyCode, on, onInput, onSubmit, onClick)
import Html.Events.Extra exposing (onEnter)
import Http
import Maybe.Extra
import Page.Error as ErrorPage
import RestApi
import Router exposing (redirectByIndexDocType)
import Skeleton exposing (Page)
import String.Extra
import Url exposing (Url)
import Util exposing (emptyHtml, maybeAsString, maybeAsText, maybePageNumberParam, maybePageSizeParam, maybeQueryParam, viewInnerHtml)



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
    , result : Maybe SearchResult
    }


emptySearchState : SearchState
emptySearchState =
    { key = Nothing
    , query = Nothing
    , pageNumber = Nothing
    , pageSize = Nothing
    , result = Nothing
    }


init : Maybe Browser.Navigation.Key -> Maybe String -> Maybe Int -> Maybe Int -> Maybe SearchResult -> ( Model, Cmd Msg )
init key query pageNumber pageSize result =
    let
        state : SearchState
        state =
            SearchState key query pageNumber pageSize result

        model : Model
        model =
            Loading state

        cmd : Cmd Msg
        cmd =
            getSearchResult query pageNumber pageSize
    in
    ( model, cmd )



--- UPDATE ---


type Msg
    = UpdateState SearchState
    | UpdateSearchUrl (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | LoadSearchResult (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | LoadedSearchResult (Result Http.Error SearchResult)


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
                                    , result = Just searchResult
                                    }
                            in
                            ( Content s, Cmd.none )

                        Loading state ->
                            ( Content { state | result = Just searchResult }, Cmd.none )

                        -- TODO should I be receiving a Loaded msg in a Content state anyway?
                        Content state ->
                            ( Content { state | result = Just searchResult }, Cmd.none )

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

        params : String
        params =
            String.join "&" (Maybe.Extra.values [ q, p, s ])

        urlQuery : String
        urlQuery =
            if params == "" then
                ""

            else
                "?" ++ params

        path : String
        path =
            (++) "/search" urlQuery
    in
    case state.key of
        Just key ->
            Browser.Navigation.pushUrl key path

        Nothing ->
            Cmd.none



--- VIEW ---


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Search"

        childNodes : List (Html Msg)
        childNodes =
            case model of
                Failure cause ->
                    [ viewSearchForm emptySearchState
                    , ErrorPage.view (ErrorPage.HttpFailure cause)
                    ]

                Loading state ->
                    [ viewSearchForm state ]

                Content state ->
                    case state.result of
                        Nothing ->
                            [ viewSearchInput state ]

                        Just searchResults ->
                            [ viewSearchForm state
                            , viewSearchResult state.query state.pageNumber state.pageSize searchResults
                            ]

        body : Html Msg
        body =
            div [ class "col-md-10", class "p-2", class "mx-auto" ] childNodes
    in
    ( title, body )


viewSearchForm : SearchState -> Html Msg
viewSearchForm state =
    Html.form
        [ onSubmit (updateSearchUrl state) ]
        [ div
            [ class "input-group"
            , class "mt-5"
            ]
            [ viewSearchInput state
            , viewSearchButton state
            ]
        ]

viewSearchInput : SearchState -> Html Msg
viewSearchInput state =
    input
        [ class "form-control"
        --, class "input-block"
        , class "input"
        , attribute "style" "height: 44px !important"
        , type_ "text"
        , value (maybeAsString state.query)
        , placeholder "What are you looking for?"
        , autocomplete False
        , spellcheck False
        , onInput (updateStateQuery state)
        , onEnter (updateSearchUrl state)
        ]
        []

viewSearchButton : SearchState -> Html Msg
viewSearchButton state =
    span [ class "input-group-button" ]
        [ button
            [ class "btn"
            , class "text-normal"
            , type_ "button"
            , ariaLabel "Search"
            , onClick (updateSearchUrl state)
            ]
            [ FeatherIcons.search
                |> FeatherIcons.toHtml []
            ]
        ]

viewSearchResult : Maybe String -> Maybe Int -> Maybe Int -> SearchResult -> Html Msg
viewSearchResult query pageNumber pageSize searchResult =
    div []
        [ viewTotalHits searchResult
        , ul [ class "list-style-none" ] <|
            List.map viewIndexDoc searchResult.results
        , viewPagination query pageNumber pageSize searchResult
        ]


viewTotalHits : SearchResult -> Html Msg
viewTotalHits searchResult =
    p
        [ class "note", class "my-3", class "px-2" ]
        [ text "Search resulted in "
        , text (String.fromInt searchResult.totalHits)
        , text " hits"
        ]


viewIndexDoc : IndexDoc -> Html Msg
viewIndexDoc doc =
    li [ class "py-2" ]
        [ div [ class "clearfix", class "p-2" ]
            [ viewCoverImage doc
            , div [ class "overflow-hidden" ]
                [ viewIndexDocTitleAsLink doc
                , br [] []
                , viewDocType doc
                , viewStrippedDescription doc

                --, viewInnerHtml (maybeAsString doc.description)
                ]
            ]
        ]


viewCoverImage : IndexDoc -> Html Msg
viewCoverImage doc =
    div [ class "float-left", class "mr-3", class "mt-1", class "bg-gray" ]
        [ img
            [ src (maybeAsString doc.image)
            , alt ("cover image of " ++ maybeAsString doc.title)
            , class "avatar"
            , width 72
            , height 72
            ]
            []
        ]


viewStrippedDescription : IndexDoc -> Html Msg
viewStrippedDescription doc =
    let
        stripped =
            String.Extra.stripTags (maybeAsString doc.description)

        truncate =
            String.left 280 stripped
    in
    p [] [ text (truncate ++ "...") ]


viewIndexDocTitleAsLink : IndexDoc -> Html Msg
viewIndexDocTitleAsLink doc =
    a
        [ href (redirectByIndexDocType doc), class "f4" ]
        [ maybeAsText doc.title ]


viewDocType : IndexDoc -> Html Msg
viewDocType doc =
    case doc.docType of
        "podcast" ->
            span [ class "Label", class "Label--outline" ] [ text "PODCAST" ]

        "episode" ->
            span [ class "Label", class "Label--outline" ] [ text "EPISODE" ]

        _ ->
            emptyHtml


viewPagination : Maybe String -> Maybe Int -> Maybe Int -> SearchResult -> Html Msg
viewPagination query pageNumber pageSize searchResult =
    let
        qParam : String
        qParam =
            "q=" ++ maybeAsString query

        sParam : String
        sParam =
            case pageSize of
                Just s ->
                    "&s=" ++ String.fromInt s

                Nothing ->
                    ""

        path : Int -> String
        path p =
            "/search?" ++ qParam ++ "&p=" ++ String.fromInt p ++ sParam

        viewFirst : Html Msg
        viewFirst =
            if searchResult.currPage > 1 then
                a
                    [ class "first_page"
                    , rel "first"
                    , ariaLabel "First"
                    , href (path 1)
                    ]
                    [ text "1" ]

            else
                emptyHtml

        viewPrev : Html Msg
        viewPrev =
            if searchResult.currPage > 1 then
                a
                    [ class "previous_page"
                    , rel "previous"
                    , ariaLabel "Previous"
                    , href (path (searchResult.currPage - 1))
                    ]
                    [ text "Previous" ]

            else
                emptyHtml

        viewLowerGap : Html Msg
        viewLowerGap =
            if searchResult.currPage > 1 then
                span [ class "gap" ] [ text "…" ]

            else
                emptyHtml

        viewCurrent : Html Msg
        viewCurrent =
            em [ class "current", class "selected" ] [ text (String.fromInt searchResult.currPage) ]

        viewHigherGap : Html Msg
        viewHigherGap =
            if searchResult.currPage < searchResult.maxPage then
                span [ class "gap" ] [ text "…" ]

            else
                emptyHtml

        viewNext : Html Msg
        viewNext =
            if searchResult.currPage < searchResult.maxPage then
                a
                    [ class "next_page"
                    , rel "next"
                    , ariaLabel "Next"
                    , href (path (searchResult.currPage + 1))
                    ]
                    [ text "Next" ]

            else
                emptyHtml

        viewLast : Html Msg
        viewLast =
            if searchResult.currPage < searchResult.maxPage then
                a
                    [ class "last_page"
                    , rel "last"
                    , ariaLabel "Last"
                    , href (path searchResult.maxPage)
                    ]
                    [ text (String.fromInt searchResult.maxPage) ]

            else
                emptyHtml
    in
    if searchResult.maxPage > 1 then
        nav [ class "paginate-container", ariaLabel "Pagination" ]
            [ div
                [ class "pagination" ]
                [ viewPrev
                , viewFirst
                , viewLowerGap
                , viewCurrent
                , viewHigherGap
                , viewLast
                , viewNext
                ]
            ]

    else
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
    , result = Nothing
    }
