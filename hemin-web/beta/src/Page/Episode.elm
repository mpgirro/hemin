module Page.Episode exposing
    ( Model
    , Msg
    , getEpisode
    , init
    , update
    , view
    )

import Browser
import Data.Episode exposing (Episode)
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Page.Error as ErrorPage
import RestApi
import Router exposing (redirectToParent)
import Skeleton exposing (Page)
import Util exposing (emptyHtml, maybeAsString, maybeAsText, prettyDateHtml, viewInnerHtml)



---- MODEL ----


type Model
    = Failure Http.Error
    | Loading
    | Content Episode


init : String -> ( Model, Cmd Msg )
init id =
    let
        model : Model
        model =
            Loading

        cmd : Cmd Msg
        cmd =
            getEpisode id
    in
    ( model, cmd )



---- UPDATE ----


type Msg
    = LoadEpisode String
    | LoadedEpisode (Result Http.Error Episode)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadEpisode id ->
            ( model, getEpisode id )

        LoadedEpisode result ->
            case result of
                Ok episode ->
                    ( Content episode, Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )



---- VIEW ----


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Episode"

        body : Html Msg
        body =
            case model of
                Failure cause ->
                    ErrorPage.view (ErrorPage.HttpFailure cause)

                Loading ->
                    text "Loading..."

                Content episode ->
                    viewEpisode episode
    in
    ( title, body )


viewEpisode : Episode -> Html Msg
viewEpisode episode =
    div
        [ class "col-sm-8"
        , class "col-md-6"
        , class "col-lg-6"
        , class "p-2"
        , class "mx-auto"
        ]
        [ viewPodcastTitle episode
        , viewCoverImage episode
        , viewTitle episode
        , viewLink episode
        , viewSmallInfos episode
        , viewDecription episode
        ]


viewPodcastTitle : Episode -> Html Msg
viewPodcastTitle episode =
    case episode.podcastTitle of
        Just title ->
            div
                [ class "text-center"
                , class "f4"
                , class "lh-condensed-ultra"
                , class "mb-2"
                ]
                [ a
                    [ href (redirectToParent episode)
                    , class "link-gray"
                    ]
                    [ text title ]
                ]

        Nothing ->
            emptyHtml


viewCoverImage : Episode -> Html Msg
viewCoverImage episode =
    case episode.image of
        Just image ->
            let
                altText =
                    "cover image of " ++ maybeAsString episode.title
            in
            div [] [ img [ src image, alt altText, class "width-full" ] [] ]

        Nothing ->
            emptyHtml


viewTitle : Episode -> Html Msg
viewTitle episode =
    case episode.title of
        Just title ->
            h1
                [ class "f2-light"
                , class "lh-condensed-ultra"
                , class "mt-4"
                , class "mb-0"
                ]
                [ maybeAsText episode.title ]

        Nothing ->
            emptyHtml


viewLink : Episode -> Html Msg
viewLink episode =
    div [ class "mt-1" ] [ Skeleton.viewLink episode.link ]


viewDecription : Episode -> Html Msg
viewDecription episode =
    case toDescriptionTriple episode of
        ( Just content, _, _ ) ->
            viewDescriptionParagraph content

        ( Nothing, Just description, _ ) ->
            viewDescriptionParagraph description

        ( Nothing, Nothing, Just summary ) ->
            viewDescriptionParagraph summary

        ( Nothing, Nothing, Nothing ) ->
            emptyHtml


viewDescriptionParagraph : String -> Html Msg
viewDescriptionParagraph description =
    p [ class "mt-4" ]
        [ viewInnerHtml description
        ]


viewSmallInfos : Episode -> Html Msg
viewSmallInfos episode =
    case ( episode.pubDate, episode.itunes.duration ) of
        ( Nothing, Nothing ) ->
            emptyHtml

        ( _, _ ) ->
            div [ class "mt-3" ]
                [ small [ class "note" ]
                    [ viewPubDate episode
                    , viewItunesDuration episode
                    ]
                ]


viewPubDate : Episode -> Html Msg
viewPubDate episode =
    case episode.pubDate of
        Just pubDate ->
            span [ class "mr-2" ] [ prettyDateHtml pubDate ]

        Nothing ->
            text "n/a"


viewItunesDuration : Episode -> Html Msg
viewItunesDuration episode =
    case episode.itunes.duration of
        Just duration ->
            span [ class "mr-2" ] [ text duration ]

        Nothing ->
            text "n/a"



--- HTTP ---


getEpisode : String -> Cmd Msg
getEpisode id =
    RestApi.getEpisode LoadedEpisode id



--- INTERNAL HELPERS ---


toDescriptionTriple : Episode -> ( Maybe String, Maybe String, Maybe String )
toDescriptionTriple e =
    let
        emptyToNothing : Maybe String -> Maybe String
        emptyToNothing maybeString =
            case maybeString of
                Just str ->
                    if String.isEmpty str then
                        Nothing

                    else
                        Just str

                Nothing ->
                    Nothing
    in
    ( emptyToNothing e.contentEncoded, emptyToNothing e.description, e.itunes.summary )
