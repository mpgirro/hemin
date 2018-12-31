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
import Html exposing (Html, a, br, div, h1, img, li, p, pre, span, text, ul)
import Html.Attributes exposing (alt, class, height, href, src, width)
import Http
import Page.Error as ErrorPage
import Podlove.SubscribeButton as PodloveButton
import RemoteData exposing (WebData)
import RestApi
import Router exposing (redirectToEpisode)
import Skeleton exposing (Page)
import String.Extra
import Time exposing (Posix)
import Util exposing (emptyHtml, maybeAsString, maybeAsText)



---- MODEL ----


type alias Model =
    { podcast : WebData Podcast
    , episodes : WebData (List Episode)
    , feeds : WebData (List Feed)
    }


init : String -> ( Model, Cmd Msg )
init id =
    let
        model : Model
        model =
            { podcast = RemoteData.NotAsked
            , episodes = RemoteData.NotAsked
            , feeds = RemoteData.NotAsked
            }

        ( _, podloveButtonCmd ) =
            PodloveButton.init

        initPodloveButton : Cmd Msg
        initPodloveButton =
            wrapPodloveButtonMsg podloveButtonCmd

        cmd : Cmd Msg
        cmd =
            Cmd.batch [ getPodcast id, getEpisodes id, getFeeds id ]
    in
    ( model, cmd )



---- UPDATE ----


type Msg
    = LoadPodcast String
    | LoadedPodcast (WebData Podcast)
    | LoadEpisodes String
    | LoadedEpisodes (WebData (List Episode))
    | LoadFeeds String
    | LoadedFeeds (WebData (List Feed))
    | PodloveButtonMsg PodloveButton.Msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadPodcast id ->
            ( model, getPodcast id )

        LoadedPodcast podcast ->
            let
                mod : Model
                mod =
                    { model | podcast = podcast }

                cmd : Cmd Msg
                cmd =
                    sendPodloveButtonModelToJs mod
            in
            ( mod, Cmd.none )

        LoadEpisodes id ->
            ( model, getEpisodes id )

        LoadedEpisodes episodes ->
            let
                sortedEpisodes : WebData (List Episode)
                sortedEpisodes =
                    case episodes of
                        RemoteData.Success es ->
                            RemoteData.Success (sortEpisodes es)

                        _ ->
                            episodes
            in
            ( { model | episodes = sortedEpisodes }, Cmd.none )

        LoadFeeds id ->
            ( model, getFeeds id )

        LoadedFeeds feeds ->
            let
                mod : Model
                mod =
                    { model | feeds = feeds }

                cmd : Cmd Msg
                cmd =
                    sendPodloveButtonModelToJs mod
            in
            ( mod, Cmd.none )

        PodloveButtonMsg buttonMsg ->
            --updatePodloveButton model buttonMsg
            ( model, Cmd.none )


updatePodloveButton : Model -> PodloveButton.Msg -> ( Model, Cmd Msg )
updatePodloveButton model buttonMsg =
    let
        buttonModel : PodloveButton.Model
        buttonModel =
            toPodloveButtonModel model

        -- TODO ignore the result! we just need trigger the update
        ( _, m ) =
            PodloveButton.update buttonMsg buttonModel
    in
    --( model, wrapPodloveButtonMsg m )
    ( model, Cmd.none )


sendPodloveButtonModelToJs : Model -> Cmd Msg
sendPodloveButtonModelToJs model =
    let
        buttonModel : PodloveButton.Model
        buttonModel =
            toPodloveButtonModel model
    in
    wrapPodloveButtonMsg (PodloveButton.sendPodloveSubscribeButtonModel buttonModel)



---- VIEW ----


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Podcast"

        body : Html Msg
        body =
            div
                [ class "col-sm-8"
                , class "col-md-6"
                , class "col-lg-6"
                , class "p-2"
                , class "mx-auto"
                ]
                [ viewPodcast model.podcast
                , viewPodloveButton model
                , viewEpisodes model.episodes
                , viewFeeds model.feeds
                ]
    in
    ( title, body )


viewPodcast : WebData Podcast -> Html Msg
viewPodcast webdata =
    case webdata of
        RemoteData.NotAsked ->
            text "Initialising..."

        RemoteData.Loading ->
            text "Loading..."

        RemoteData.Failure error ->
            ErrorPage.viewHttpFailure error

        RemoteData.Success podcast ->
            div []
                [ viewCoverImage podcast
                , viewTitle podcast
                , viewLink podcast
                , viewDecription podcast
                , viewCategories podcast
                ]


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
    case ( model.podcast, model.feeds ) of
        ( RemoteData.Success _, RemoteData.Success (head :: _) ) ->
            let
                buttonModel : PodloveButton.Model
                buttonModel =
                    toPodloveButtonModel model
            in
            wrapPodloveButtonHtml (PodloveButton.view buttonModel)

        ( _, _ ) ->
            emptyHtml


viewEpisodes : WebData (List Episode) -> Html Msg
viewEpisodes webdata =
    case webdata of
        RemoteData.NotAsked ->
            text "Initialising..."

        RemoteData.Loading ->
            text "Loading..."

        RemoteData.Failure error ->
            ErrorPage.viewHttpFailure error

        RemoteData.Success episodes ->
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
                            truncate s ++ "..."

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


viewFeeds : WebData (List Feed) -> Html Msg
viewFeeds webdata =
    case webdata of
        RemoteData.NotAsked ->
            text "Initialising..."

        RemoteData.Loading ->
            text "Loading..."

        RemoteData.Failure error ->
            ErrorPage.viewHttpFailure error

        RemoteData.Success feeds ->
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
    RestApi.getPodcast (RemoteData.fromResult >> LoadedPodcast) id


getEpisodes : String -> Cmd Msg
getEpisodes id =
    RestApi.getEpisodesByPodcast (RemoteData.fromResult >> LoadedEpisodes) id


getFeeds : String -> Cmd Msg
getFeeds id =
    RestApi.getFeedsByPodcast (RemoteData.fromResult >> LoadedFeeds) id



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
        feeds : List Feed
        feeds =
            case model.feeds of
                RemoteData.Success fs ->
                    fs

                _ ->
                    []

        toButtonModel : Podcast -> PodloveButton.Model
        toButtonModel podcast =
            { title = podcast.title
            , subtitle = podcast.itunes.subtitle
            , description = podcast.description
            , cover = podcast.image
            , feeds = toButtonFeeds
            }

        toButtonFeeds : List PodloveButton.Feed
        toButtonFeeds =
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
    case ( model.podcast, feeds ) of
        ( RemoteData.Success podcast, head :: _ ) ->
            toButtonModel podcast

        ( _, _ ) ->
            PodloveButton.emptyModel


sortEpisodes : List Episode -> List Episode
sortEpisodes episodes =
    let
        posixToInt : Posix -> Int
        posixToInt posix =
            Time.posixToMillis posix

        inverseCompare : Int -> Int -> Order
        inverseCompare a b =
            if a > b then
                LT

            else if a < b then
                GT

            else
                EQ

        compareEpisodes : Episode -> Episode -> Order
        compareEpisodes e1 e2 =
            case ( e1.pubDate, e2.pubDate ) of
                ( Just d1, Just d2 ) ->
                    inverseCompare (posixToInt d1) (posixToInt d2)

                ( Just _, Nothing ) ->
                    GT

                ( Nothing, Just _ ) ->
                    LT

                ( Nothing, Nothing ) ->
                    EQ
    in
    List.sortWith compareEpisodes episodes
