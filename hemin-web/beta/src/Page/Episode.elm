module Page.Episode exposing (Model(..), Msg(..), getEpisode, update, view)

import Browser
import Data.Episode exposing (Episode)
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Page.Error as ErrorPage
import RestApi
import Router exposing (redirectToParent)
import Skeleton exposing (Page)
import Util exposing (emptyHtml, maybeAsString, maybeAsText)



---- MODEL ----


type Model
    = Failure Http.Error
    | Loading
    | Content Episode



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


view : Model -> Html msg
view model =
    case model of
        Failure cause ->
            ErrorPage.view (ErrorPage.HttpFailure cause)

        Loading ->
            text "Loading..."

        Content episode ->
            viewEpisode episode


viewEpisode : Episode -> Html msg
viewEpisode episode =
    div [ class "col-sm-8", class "col-md-6", class "col-lg-6", class "p-2", class "mx-auto" ]
        [ viewPodcastTitle episode
        , viewCoverImage episode
        , viewTitle episode
        , viewLink episode
        , viewDecription episode
        ]


viewPodcastTitle : Episode -> Html msg
viewPodcastTitle episode =
    case episode.podcastTitle of
        Just title ->
            div
                [ class "text-center"
                , class "f3-light"
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

viewCoverImage : Episode -> Html msg
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

viewTitle : Episode -> Html msg
viewTitle episode =
    case episode.title of
        Just title ->
            h1 [ class "f2-light", class "lh-condensed-ultra", class "mt-4", class "mb-0" ] [ maybeAsText episode.title ]

        Nothing ->
            emptyHtml

viewLink : Episode -> Html msg
viewLink episode =
    Skeleton.viewLink episode.link

viewDecription : Episode -> Html msg
viewDecription episode =
    case episode.itunes.summary of
        Just summary ->
            viewDescriptionParagraph summary

        Nothing ->
            case episode.description of
                Just description ->
                    viewDescriptionParagraph description

                Nothing ->
                    emptyHtml

viewDescriptionParagraph : String -> Html msg
viewDescriptionParagraph description =
    p [ class "mt-4" ] [ text description ]


--- HTTP ---


getEpisode : String -> Cmd Msg
getEpisode id =
    RestApi.getEpisode LoadedEpisode id
