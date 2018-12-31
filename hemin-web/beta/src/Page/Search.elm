module Page.Search exposing
    ( Model
    , Msg
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
import Html exposing (Attribute, Html, a, b, br, button, div, em, form, h1, img, input, li, nav, p, span, text, ul)
import Html.Attributes exposing (..)
import Html.Attributes.Aria exposing (..)
import Html.Events exposing (keyCode, on, onClick, onInput, onSubmit)
import Http
import Maybe.Extra
import Page.Error as ErrorPage
import RemoteData exposing (WebData)
import RestApi
import Router exposing (redirectByIndexDocType)
import Skeleton exposing (Page)
import String.Extra
import Url exposing (Url)
import Util exposing (emptyHtml, maybeAsString, maybeAsText, maybePageNumberParam, maybePageSizeParam, maybeQueryParam, viewInnerHtml)



--- MODEL ---


type alias Model =
    { key : Maybe Browser.Navigation.Key
    , query : Maybe String
    , pageNumber : Maybe Int
    , pageSize : Maybe Int
    , result : WebData SearchResult
    }


emptyModel : Model
emptyModel =
    { key = Nothing
    , query = Nothing
    , pageNumber = Nothing
    , pageSize = Nothing
    , result = RemoteData.NotAsked
    }


init : Maybe Browser.Navigation.Key -> Maybe String -> Maybe Int -> Maybe Int -> Maybe SearchResult -> ( Model, Cmd Msg )
init key query pageNumber pageSize result =
    let
        model : Model
        model =
            initModelFromParams key query pageNumber pageSize

        cmd : Cmd Msg
        cmd =
            getSearchResult query pageNumber pageSize
    in
    ( model, cmd )



--- UPDATE ---


type Msg
    = UpdateModel Model
    | UpdateSearchUrl (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | LoadSearchResult (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | LoadedSearchResult (WebData SearchResult)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        UpdateModel m ->
            ( m, Cmd.none )

        UpdateSearchUrl key query pageNumber pageSize ->
            let
                m : Model
                m =
                    loadingModelFromParams key query pageNumber pageSize
            in
            ( m, redirectLocalUrl m )

        LoadSearchResult key query pageNumber pageSize ->
            let
                m : Model
                m =
                    loadingModelFromParams key query pageNumber pageSize
            in
            ( m, getSearchResult query pageNumber pageSize )

        -- TODO send HTTP request!
        LoadedSearchResult result ->
            ( { model | result = result }, Cmd.none )


updateModelQuery : Model -> String -> Msg
updateModelQuery model query =
    UpdateModel { model | query = Just query }


updateSearchUrl : Model -> Msg
updateSearchUrl model =
    UpdateSearchUrl model.key model.query Nothing Nothing


redirectLocalUrl : Model -> Cmd Msg
redirectLocalUrl model =
    let
        q =
            maybeQueryParam model.query

        p =
            maybePageNumberParam model.pageNumber

        s =
            maybePageSizeParam model.pageSize

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
    case model.key of
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

        body : Html Msg
        body =
            div
                [ class "col-md-10", class "p-2", class "mx-auto" ]
                [ viewSearchForm model
                , viewSearchResults model
                ]
    in
    ( title, body )


viewSearchForm : Model -> Html Msg
viewSearchForm model =
    Html.form
        [ onSubmit (updateSearchUrl model) ]
        [ div
            [ class "input-group"
            , class "mt-5"
            ]
            [ viewSearchInput model
            , viewSearchButton model
            ]
        ]


viewSearchInput : Model -> Html Msg
viewSearchInput model =
    input
        [ class "form-control"

        --, class "input-block"
        , class "input"
        , attribute "style" "height: 44px !important"
        , type_ "text"
        , value (maybeAsString model.query)
        , placeholder "What are you looking for?"
        , autocomplete False
        , spellcheck False
        , onInput (updateModelQuery model)
        ]
        []


viewSearchButton : Model -> Html Msg
viewSearchButton model =
    span [ class "input-group-button" ]
        [ button
            [ class "btn"
            , class "text-normal"
            , type_ "button"
            , ariaLabel "Search"
            , onClick (updateSearchUrl model)
            ]
            [ FeatherIcons.search
                |> FeatherIcons.toHtml []
            ]
        ]


viewSearchResults : Model -> Html Msg
viewSearchResults model =
    case model.result of
        RemoteData.NotAsked ->
            emptyHtml

        RemoteData.Loading ->
            text "Loading..."

        RemoteData.Failure error ->
            ErrorPage.viewHttpFailure error

        RemoteData.Success result ->
            viewSearchResult model.query model.pageNumber model.pageSize result


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
        [ div [ class "clearfix" ]
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
            RestApi.getSearchResult (RemoteData.fromResult >> LoadedSearchResult) query pageNumber pageSize



--- INTERNAL HELPERS ---


initModelFromParams : Maybe Browser.Navigation.Key -> Maybe String -> Maybe Int -> Maybe Int -> Model
initModelFromParams key query pageNumber pageSize =
    { key = key
    , query = query
    , pageNumber = pageNumber
    , pageSize = pageSize
    , result = RemoteData.NotAsked
    }


loadingModelFromParams : Maybe Browser.Navigation.Key -> Maybe String -> Maybe Int -> Maybe Int -> Model
loadingModelFromParams key query pageNumber pageSize =
    { key = key
    , query = query
    , pageNumber = pageNumber
    , pageSize = pageSize
    , result = RemoteData.Loading
    }
