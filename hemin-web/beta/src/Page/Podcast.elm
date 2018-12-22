module Page.Podcast exposing (Model, Msg(..), getPodcast, init, update, view)

import Browser
import Data.Episode exposing (Episode)
import Data.Podcast exposing (Podcast)
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Page.Error as ErrorPage
import RestApi
import Router exposing (redirectToEpisode)
import Skeleton exposing (Page)
import String.Extra
import Util exposing (emptyHtml, maybeAsString, maybeAsText)



---- MODEL ----


type alias Model =
    { status : Status
    , failure : Maybe Http.Error
    , podcast : Maybe Podcast
    , episodes : List Episode
    }


type Status
    = Loading
    | Ready


init : String -> ( Model, Cmd Msg )
init id =
    let
        model : Model
        model =
            { status = Loading
            , failure = Nothing
            , podcast = Nothing
            , episodes = []
            }

        cmds : Cmd Msg
        cmds =
            Cmd.batch [ getPodcast id, getEpisodes id ]
    in
    ( model, cmds )



---- UPDATE ----


type Msg
    = LoadPodcast String
    | LoadedPodcast (Result Http.Error Podcast)
    | LoadEpisodes String
    | LoadedEpisodes (Result Http.Error (List Episode))


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadPodcast id ->
            ( model, getPodcast id )

        LoadedPodcast result ->
            case result of
                Ok podcast ->
                    ( { model | podcast = Just podcast, status = Ready }, Cmd.none )

                Err error ->
                    ( { model | failure = Just error, status = Ready }, Cmd.none )

        LoadEpisodes id ->
            ( model, getEpisodes id )

        LoadedEpisodes result ->
            case result of
                Ok episodes ->
                    ( { model | episodes = episodes }, Cmd.none )

                Err error ->
                    ( { model | failure = Just error }, Cmd.none )



---- VIEW ----


view : Model -> Html msg
view model =
    case model.status of
        Loading ->
            text "Loading..."

        Ready ->
            div
                [ class "col-sm-8"
                , class "col-md-6"
                , class "col-lg-6"
                , class "p-2"
                , class "mx-auto"
                ]
                [ viewHttpError model.failure
                , viewPodcast model.podcast
                , viewEpisodes model.episodes
                ]


viewHttpError : Maybe Http.Error -> Html msg
viewHttpError maybeError =
    case maybeError of
        Just error ->
            div [ class "flash", class "flash-full", class "flash-error" ]
                [ ErrorPage.view (ErrorPage.HttpFailure error) ]

        Nothing ->
            emptyHtml


viewPodcast : Maybe Podcast -> Html msg
viewPodcast maybePodcast =
    case maybePodcast of
        Just podcast ->
            div []
                [ viewCoverImage podcast
                , viewTitle podcast
                , viewLink podcast
                , viewDecription podcast
                , viewCategories podcast
                ]

        Nothing ->
            emptyHtml


viewCoverImage : Podcast -> Html msg
viewCoverImage podcast =
    case podcast.image of
        Just image ->
            let
                altText =
                    "cover image of podcast: " ++ maybeAsString podcast.title
            in
            div [] [ img [ src image, alt altText, class "width-full" ] [] ]

        Nothing ->
            emptyHtml


viewTitle : Podcast -> Html msg
viewTitle podcast =
    case podcast.title of
        Just title ->
            h1
                [ class "f2-light"
                , class "lh-condensed-ultra"
                , class "mt-4"
                , class "mb-0"
                ]
                [ maybeAsText podcast.title ]

        Nothing ->
            emptyHtml


viewLink : Podcast -> Html msg
viewLink podcast =
    Skeleton.viewLink podcast.link


viewDecription : Podcast -> Html msg
viewDecription podcast =
    case podcast.itunes.summary of
        Just summary ->
            viewDescriptionParagraph summary

        Nothing ->
            case podcast.description of
                Just description ->
                    viewDescriptionParagraph description

                Nothing ->
                    emptyHtml


viewDescriptionParagraph : String -> Html msg
viewDescriptionParagraph description =
    p [ class "mt-4" ] [ text description ]


viewCategories : Podcast -> Html msg
viewCategories podcast =
    div [ class "mt-3" ] <|
        List.map viewCategory podcast.itunes.categories


viewCategory : String -> Html msg
viewCategory category =
    -- TODO add the OpenIconic stuff
    span [ class "Label", class "Label--gray", class "p-1", class "mr-1" ]
        [ span [ class "oi", class "oi-tag" ] []
        , text category
        ]


viewEpisodes : List Episode -> Html msg
viewEpisodes episodes =
    case episodes of
        [] ->
            emptyHtml

        first :: _ ->
            div []
                [ h2 [] [ text "Episodes" ]
                , ul [ class "list-style-none" ] <|
                    List.map viewEpisodeTeaser episodes
                ]


viewEpisodeTeaser : Episode -> Html msg
viewEpisodeTeaser episode =
    let
        viewEpisodeTeaserCover : Html msg
        viewEpisodeTeaserCover =
            case episode.image of
                Just image ->
                    let
                        altText =
                            "cover image of " ++ maybeAsString episode.title
                    in
                    div [ class "float-left", class "mr-3", class "mt-1", class "bg-gray" ]
                        [ img
                            [ src (maybeAsString episode.image)
                            , alt ("cover image of episode: " ++ maybeAsString episode.title)
                            , class "avatar"
                            , width 56
                            , height 56
                            ]
                            []
                        ]

                Nothing ->
                    emptyHtml

        viewEpisodeTeaserTitle : Html msg
        viewEpisodeTeaserTitle =
            a
                [ href (redirectToEpisode episode), class "f4" ]
                [ maybeAsText episode.title ]

        viewEpisodeTeaserDescription : Html msg
        viewEpisodeTeaserDescription =
            let
                description : String
                description =
                    case ( episode.description, episode.itunes.summary ) of
                        ( Just d, _ ) ->
                            d

                        ( Nothing, Just s ) ->
                            s

                        ( Nothing, Nothing ) ->
                            ""

                stripped =
                    String.Extra.stripTags description

                truncate =
                    String.left 280 stripped
            in
            p [] [ text (truncate ++ "...") ]
    in
    li [ class "py-2" ]
        [ div [ class "clearfix", class "p-2" ]
            [ viewEpisodeTeaserCover
            , div []
                [ viewEpisodeTeaserTitle

                --, br [] []
                --, viewEpisodeTeaserDescription
                ]
            ]
        ]



--- HTTP ---


getPodcast : String -> Cmd Msg
getPodcast id =
    RestApi.getPodcast LoadedPodcast id


getEpisodes : String -> Cmd Msg
getEpisodes id =
    RestApi.getEpisodesByPodcast LoadedEpisodes id
