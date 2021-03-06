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
import Html.Attributes exposing (alt, attribute, autocomplete, class, height, href, placeholder, rel, spellcheck, src, type_, value, width)
import Html.Attributes.Aria exposing (ariaLabel)
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
import Util exposing (buildSearchUrl, emptyHtml, maybeAsString, maybeAsText, maybePageNumberParam, maybePageSizeParam, maybeQueryParam, prettyDateHtml, viewInnerHtml)



--- MODEL ---


type alias Model =
    { navigationKey : Maybe Browser.Navigation.Key
    , query : Maybe String
    , pageNumber : Maybe Int
    , pageSize : Maybe Int
    , result : WebData SearchResult
    }


emptyModel : Model
emptyModel =
    { navigationKey = Nothing
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
    | GetSearchResult (Maybe Browser.Navigation.Key) (Maybe String) (Maybe Int) (Maybe Int)
    | GotSearchResultData (WebData SearchResult)


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

        GetSearchResult key query pageNumber pageSize ->
            let
                m : Model
                m =
                    loadingModelFromParams key query pageNumber pageSize
            in
            ( m, getSearchResult query pageNumber pageSize )

        -- TODO send HTTP request!
        GotSearchResultData result ->
            ( { model | result = result }, Cmd.none )


updateModelQuery : Model -> String -> Msg
updateModelQuery model query =
    UpdateModel { model | query = Just query }


updateSearchUrl : Model -> Msg
updateSearchUrl model =
    UpdateSearchUrl model.navigationKey model.query Nothing Nothing


redirectLocalUrl : Model -> Cmd Msg
redirectLocalUrl model =
    let
        path : String
        path =
            buildSearchUrl model.query model.pageNumber model.pageSize
    in
    case model.navigationKey of
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
    div [ class "input-group-append" ]
        [ button
            [ class "btn"
            , class "btn-outline-primary"
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
            Skeleton.viewLoadingText

        RemoteData.Failure error ->
            ErrorPage.viewHttpFailure error

        RemoteData.Success searchResult ->
            div []
                [ viewTotalHits searchResult
                , ul [ class "list-unstyled" ] <|
                    List.map viewIndexDoc searchResult.results
                , viewPagination model.query model.pageNumber model.pageSize searchResult
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
    li [ class "media", class "my-4" ]
        [ viewCoverImage doc
        , div [ class "media-body" ]
            [ viewIndexDocTitleAsLink doc
            , viewIndexDocSubtitle doc
            , p []
                [ viewDocType doc
                , viewPubDate doc
                ]
            , viewStrippedDescription doc
            ]
        ]


viewCoverImage : IndexDoc -> Html Msg
viewCoverImage doc =
    img
        [ src (maybeAsString doc.image)
        , alt ("cover image of " ++ maybeAsString doc.title)
        , class "mr-3"
        , width 72
        , height 72
        ]
        []


viewStrippedDescription : IndexDoc -> Html Msg
viewStrippedDescription doc =
    let
        stripped : String
        stripped =
            String.Extra.stripTags (maybeAsString doc.description)

        truncate : String
        truncate =
            String.left 280 stripped
    in
    p [] [ text (truncate ++ "...") ]


viewIndexDocTitleAsLink : IndexDoc -> Html Msg
viewIndexDocTitleAsLink doc =
    a
        [ href (redirectByIndexDocType doc), class "h4", class "mt-0", class "mb-1" ]
        [ maybeAsText doc.title ]


viewIndexDocSubtitle : IndexDoc -> Html Msg
viewIndexDocSubtitle doc =
    case ( doc.docType, doc.podcastTitle ) of
        ( "podcast", _ ) ->
            emptyHtml

        ( "episode", Just podcastTitle ) ->
            p [ class "note" ] [ text podcastTitle ]

        ( _, _ ) ->
            emptyHtml


viewDocType : IndexDoc -> Html Msg
viewDocType doc =
    let
        viewLabel : String -> Html Msg
        viewLabel label =
            span [ class "Label", class "Label--outline", class "mr-2" ] [ text label ]
    in
    case doc.docType of
        "podcast" ->
            viewLabel "PODCAST"

        "episode" ->
            viewLabel "EPISODE"

        _ ->
            emptyHtml


viewPubDate : IndexDoc -> Html Msg
viewPubDate doc =
    let
        viewDate : Html Msg
        viewDate =
            case doc.pubDate of
                Just pubDate ->
                    span [ class "note", class "mr-2" ] [ text "date:", prettyDateHtml pubDate ]

                Nothing ->
                    emptyHtml
    in
    case doc.docType of
        "podcast" ->
            emptyHtml

        "episode" ->
            viewDate

        _ ->
            emptyHtml


viewPagination : Maybe String -> Maybe Int -> Maybe Int -> SearchResult -> Html Msg
viewPagination query pageNumber pageSize searchResult =
    let
        path : Int -> String
        path p =
            buildSearchUrl query (Just p) pageSize

        viewFirst : Html Msg
        viewFirst =
            if searchResult.currPage > 1 then
                li [ class "page-item" ]
                    [ a
                        [ class "page-link"
                        , rel "first"
                        , ariaLabel "First"
                        , href (path 1)
                        ]
                        [ text "1" ]
                    ]

            else
                emptyHtml

        viewPrev : Html Msg
        viewPrev =
            if searchResult.currPage > 1 then
                li [ class "page-item" ]
                    [ a
                        [ class "page-link"
                        , rel "previous"
                        , ariaLabel "Previous"
                        , href (path (searchResult.currPage - 1))
                        ]
                        [ text "Previous" ]
                    ]

            else
                emptyHtml

        viewLowerGap : Html Msg
        viewLowerGap =
            if searchResult.currPage > 2 then
                li [ class "page-item" ]
                    [ span [ class "page-link" ] [ text "…" ]
                    ]

            else
                emptyHtml

        viewCurrent : Html Msg
        viewCurrent =
            li [ class "page-item", class "active" ]
                [ em [ class "page-link" ] [ text (String.fromInt searchResult.currPage) ]
                ]

        viewHigherGap : Html Msg
        viewHigherGap =
            if searchResult.currPage < searchResult.maxPage - 1 then
                li [ class "page-item" ]
                    [ span [ class "page-link" ] [ text "…" ]
                    ]

            else
                emptyHtml

        viewNext : Html Msg
        viewNext =
            if searchResult.currPage < searchResult.maxPage then
                li [ class "page-item" ]
                    [ a
                        [ class "page-link"
                        , rel "next"
                        , ariaLabel "Next"
                        , href (path (searchResult.currPage + 1))
                        ]
                        [ text "Next" ]
                    ]

            else
                emptyHtml

        viewLast : Html Msg
        viewLast =
            if searchResult.currPage < searchResult.maxPage then
                li [ class "page-item" ]
                    [ a
                        [ class "page-link"
                        , rel "last"
                        , ariaLabel "Last"
                        , href (path searchResult.maxPage)
                        ]
                        [ text (String.fromInt searchResult.maxPage) ]
                    ]

            else
                emptyHtml
    in
    if searchResult.maxPage > 1 then
        nav [ ariaLabel "Pagination" ]
            [ div
                [ class "pagination", class "justify-content-center" ]
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
            RestApi.getSearchResult (RemoteData.fromResult >> GotSearchResultData) query pageNumber pageSize



--- INTERNAL HELPERS ---


initModelFromParams : Maybe Browser.Navigation.Key -> Maybe String -> Maybe Int -> Maybe Int -> Model
initModelFromParams key query pageNumber pageSize =
    { navigationKey = key
    , query = query
    , pageNumber = pageNumber
    , pageSize = pageSize
    , result = RemoteData.NotAsked
    }


loadingModelFromParams : Maybe Browser.Navigation.Key -> Maybe String -> Maybe Int -> Maybe Int -> Model
loadingModelFromParams key query pageNumber pageSize =
    { navigationKey = key
    , query = query
    , pageNumber = pageNumber
    , pageSize = pageSize
    , result = RemoteData.Loading
    }
