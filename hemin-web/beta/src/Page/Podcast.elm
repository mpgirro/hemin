module Page.Podcast exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import Data.Episode exposing (Episode)
import Data.Feed exposing (Feed)
import Data.Podcast exposing (Podcast)
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Page.Error as ErrorPage
import Podlove.SubscribeButton as PodloveButton
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
    , feeds : List Feed
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
            , feeds = []
            }

        ( _, podloveButtonCmd ) =
            PodloveButton.init

        initPodloveButton : Cmd Msg
        initPodloveButton =
            wrapPodloveButtonMsg podloveButtonCmd

        cmd : Cmd Msg
        cmd =
            Cmd.batch [ getPodcast id, getEpisodes id, getFeeds id, initPodloveButton ]
    in
    ( model, cmd )



---- UPDATE ----


type Msg
    = LoadPodcast String
    | LoadedPodcast (Result Http.Error Podcast)
    | LoadEpisodes String
    | LoadedEpisodes (Result Http.Error (List Episode))
    | LoadFeeds String
    | LoadedFeeds (Result Http.Error (List Feed))
    | PodloveButtonMsg PodloveButton.Msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadPodcast id ->
            ( model, getPodcast id )

        LoadedPodcast result ->
            case result of
                Ok podcast ->
                    {--
                    let
                        mod : Model
                        mod = { model | podcast = Just podcast, status = Ready }

                        ( _, cmd ) = updatePodloveButton mod PodloveButton.SendToJs
                    in
                    ( mod, cmd )
                    --}
                    let
                        buttonModel : PodloveButton.Model
                        buttonModel =
                            toPodloveButtonModel model

                        cmd : Cmd Msg
                        cmd = wrapPodloveButtonMsg (PodloveButton.sendPodloveSubscribeButtonModel buttonModel)
                    in
                    ( { model | podcast = Just podcast, status = Ready }, cmd )

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

        LoadFeeds id ->
            ( model, getFeeds id )

        LoadedFeeds result ->
            case result of
                Ok feeds ->
                    {--
                    let
                        mod : Model
                        mod = { model | feeds = feeds }

                        ( _, cmd ) = updatePodloveButton mod PodloveButton.SendToJs
                    in
                    ( mod, cmd )
                    --}
                    let
                        buttonModel : PodloveButton.Model
                        buttonModel =
                            toPodloveButtonModel model

                        cmd : Cmd Msg
                        cmd = wrapPodloveButtonMsg (PodloveButton.sendPodloveSubscribeButtonModel buttonModel)
                    in
                    ( { model | feeds = feeds }, Cmd.none )

                Err error ->
                    ( { model | failure = Just error }, Cmd.none )

        PodloveButtonMsg buttonMsg ->
            --updatePodloveButton model buttonMsg
            ( model, Cmd.none )


updatePodloveButton : Model -> PodloveButton.Msg -> ( Model, Cmd Msg )
updatePodloveButton model buttonMsg =
    let
        buttonModel : PodloveButton.Model
        buttonModel =
            toPodloveButtonModel model

        ( _, m ) =
            PodloveButton.update buttonMsg buttonModel
    in
    ( model, wrapPodloveButtonMsg m )

---- VIEW ----


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Podcast"

        body : Html Msg
        body =
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
                        , viewPodloveButton model
                        , viewEpisodes model.episodes
                        , viewFeeds model.feeds
                        ]
    in
    ( title, body )


viewHttpError : Maybe Http.Error -> Html Msg
viewHttpError maybeError =
    case maybeError of
        Just error ->
            div [ class "flash", class "flash-full", class "flash-error" ]
                [ ErrorPage.view (ErrorPage.HttpFailure error) ]

        Nothing ->
            emptyHtml


viewPodcast : Maybe Podcast -> Html Msg
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


viewCoverImage : Podcast -> Html Msg
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


viewTitle : Podcast -> Html Msg
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


viewLink : Podcast -> Html Msg
viewLink podcast =
    Skeleton.viewLink podcast.link


viewDecription : Podcast -> Html Msg
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


viewDescriptionParagraph : String -> Html Msg
viewDescriptionParagraph description =
    p [ class "mt-4" ] [ text description ]


viewCategories : Podcast -> Html Msg
viewCategories podcast =
    div [ class "mt-3" ] <|
        List.map viewCategory podcast.itunes.categories


viewCategory : String -> Html Msg
viewCategory category =
    -- TODO add the OpenIconic stuff
    span [ class "Label", class "Label--gray", class "p-1", class "mr-1" ]
        [ span [ class "oi", class "oi-tag" ] []
        , text category
        ]


viewPodloveButton : Model -> Html Msg
viewPodloveButton model =
    case (model.podcast, model.feeds) of
        (Just podcast, head :: _ ) ->
            let
                buttonModel : PodloveButton.Model
                buttonModel =
                    toPodloveButtonModel model
            in
            wrapPodloveButtonHtml (PodloveButton.view buttonModel)

        ( _, _ ) ->
            emptyHtml


viewEpisodes : List Episode -> Html Msg
viewEpisodes episodes =
    case episodes of
        [] ->
            emptyHtml

        first :: _ ->
            div [ class "mt-4" ]
                [ div [ class "Subhead" ]
                    [ div [ class "Subhead-heading" ] [ text "Episodes" ] ]
                , ul [ class "list-style-none" ] <|
                    List.map viewEpisodeTeaser episodes
                ]


viewEpisodeTeaser : Episode -> Html Msg
viewEpisodeTeaser episode =
    let
        viewEpisodeTeaserCover : Html Msg
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

        viewEpisodeTeaserTitle : Html Msg
        viewEpisodeTeaserTitle =
            a
                [ href (redirectToEpisode episode), class "f4" ]
                [ maybeAsText episode.title ]

        viewEpisodeTeaserDescription : Html Msg
        viewEpisodeTeaserDescription =
            let
                description : Maybe String
                description =
                    case ( episode.description, episode.itunes.summary, episode.contentEncoded ) of
                        ( Just d, _, _ ) ->
                            Just d

                        ( Nothing, Just s, _ ) ->
                            Just s

                        ( Nothing, Nothing, Just c ) ->
                            Just c

                        ( Nothing, Nothing, Nothing ) ->
                            Nothing

                teaser : String
                teaser =
                    case description of
                        Just s ->
                            (truncate s) ++ "..."

                        Nothing ->
                            ""

                stripped s =
                    String.Extra.stripTags s

                truncate s =
                    String.left 70 s
            in
            p [] [ text teaser ]
    in
    li [ class "py-2" ]
        [ div [ class "clearfix", class "p-2" ]
            [ viewEpisodeTeaserCover
            , div [ class "overflow-hidden" ]
                [ viewEpisodeTeaserTitle
                , br [] []
                , viewEpisodeTeaserDescription
                ]
            ]
        ]


viewFeeds : List Feed -> Html Msg
viewFeeds feeds =
    let
        viewFeed : Feed -> String
        viewFeed feed =
            maybeAsString feed.url
    in
    case feeds of
        [] ->
            emptyHtml

        first :: _ ->
            div [ class "mt-4" ]
                [ div [ class "Subhead" ]
                    [ div [ class "Subhead-heading" ] [ text "Feeds" ] ]
                , pre []
                    [ text (String.join "\n" (List.map viewFeed feeds))
                    ]
                ]



--- HTTP ---


getPodcast : String -> Cmd Msg
getPodcast id =
    RestApi.getPodcast LoadedPodcast id


getEpisodes : String -> Cmd Msg
getEpisodes id =
    RestApi.getEpisodesByPodcast LoadedEpisodes id


getFeeds : String -> Cmd Msg
getFeeds id =
    RestApi.getFeedsByPodcast LoadedFeeds id


--- INTERNALS ---


wrapPodloveButtonMsg : Cmd PodloveButton.Msg -> Cmd Msg
wrapPodloveButtonMsg msg =
    Cmd.map PodloveButtonMsg msg


wrapPodloveButtonHtml : Html PodloveButton.Msg -> Html Msg
wrapPodloveButtonHtml msg =
    Html.map PodloveButtonMsg msg


toPodloveButtonModel : Model -> PodloveButton.Model
toPodloveButtonModel model =
    let
       toButtonModel : Podcast -> List Feed -> PodloveButton.Model
       toButtonModel podcast feeds =
           { title = podcast.title
           , subtitle = podcast.itunes.subtitle
           , description = podcast.description
           , cover = podcast.image
           , feeds = toButtonFeeds feeds
           }

       toButtonFeeds : List Feed -> List PodloveButton.Feed
       toButtonFeeds feeds =
           List.map toButtonFeed feeds

       toButtonFeed : Feed -> PodloveButton.Feed
       toButtonFeed feed =
           { type_ = Nothing
           , format = Nothing
           , url = feed.url
           , variant = Nothing
           , directoryUrlItunes = Nothing
           }
    in
    case (model.podcast, model.feeds) of
        (Just podcast, head :: _) ->
            toButtonModel podcast model.feeds

        (_, _) ->
            PodloveButton.emptyModel


