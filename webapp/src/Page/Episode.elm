module Page.Episode exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import Browser
import Const exposing (podlovePlayerThemeHighlight, podlovePlayerThemeMain)
import Data.Episode exposing (Episode)
import Html exposing (Html, a, div, h1, img, p, small, span, text)
import Html.Attributes exposing (alt, class, href, src)
import Http
import Page.Error as ErrorPage
import Podlove.WebPlayer as PodlovePlayer
import RemoteData exposing (WebData)
import RestApi
import Router exposing (redirectToParent)
import Skeleton exposing (Page)
import Util exposing (emptyHtml, maybeAsString, maybeAsText, prettyDateHtml, viewInnerHtml)



---- MODEL ----


type alias Model =
    { episode : WebData Episode }


init : String -> ( Model, Cmd Msg )
init id =
    let
        model : Model
        model =
            { episode = RemoteData.NotAsked }

        ( _, podlovePlayerCmd ) =
            PodlovePlayer.init

        initPodlovePlayer : Cmd Msg
        initPodlovePlayer =
            wrapPodlovePlayerMsg podlovePlayerCmd

        cmd : Cmd Msg
        cmd =
            getEpisode id
    in
    ( model, cmd )



---- UPDATE ----


type Msg
    = GotEpisodeData (WebData Episode)
    | PodlovePlayerMsg PodlovePlayer.Msg


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        GotEpisodeData episode ->
            let
                mod : Model
                mod =
                    { model | episode = episode }

                cmd : Cmd Msg
                cmd =
                    sendPodlovePlayerModelToJs mod

            in
            ( mod, cmd )

        PodlovePlayerMsg playerMsg ->
            updatePodlovePlayer model playerMsg
            --( model, Cmd.none )


updatePodlovePlayer : Model -> PodlovePlayer.Msg -> ( Model, Cmd Msg )
updatePodlovePlayer model playerMsg =
    let
        playerModel : PodlovePlayer.Model
        playerModel =
            case model.episode of
                RemoteData.Success episode ->
                    episodeToPodlovePlayerModel episode

                _ ->
                    PodlovePlayer.emptyModel

        -- TODO ignore the result! we just need trigger the update
        ( _, m ) =
            PodlovePlayer.update playerMsg playerModel
    in
    ( model, wrapPodlovePlayerMsg m )
    --( model, Cmd.none )


sendPodlovePlayerModelToJs : Model -> Cmd Msg
sendPodlovePlayerModelToJs model =
    let
        buttonModel : PodlovePlayer.Model
        buttonModel =
            modelToPodlovePlayerModel model
    in
    wrapPodlovePlayerMsg (PodlovePlayer.sendPodloveWebPlayerModel buttonModel)



---- VIEW ----


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Episode"

        body : Html Msg
        body =
            case model.episode of
                RemoteData.NotAsked ->
                    Skeleton.viewInitializingText

                RemoteData.Loading ->
                    Skeleton.viewLoadingText

                RemoteData.Failure error ->
                    ErrorPage.viewHttpFailure error

                RemoteData.Success episode ->
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
        , viewPodlovePlayer episode
        , viewDecription episode
        ]


viewPodcastTitle : Episode -> Html Msg
viewPodcastTitle episode =
    let
        txt : String
        txt =
            case episode.podcastTitle of
                Just title ->
                    title

                Nothing ->
                    "Go to Podcast"
    in
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
                        [ text txt ]
                    ]



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
    let
        value : Html Msg
        value =
            case episode.pubDate of
                Just pubDate ->
                    prettyDateHtml pubDate

                Nothing ->
                    text "n/a"
    in
    span [ class "mr-2" ] [ text "date:", value ]


viewItunesDuration : Episode -> Html Msg
viewItunesDuration episode =
    let
        value : Html Msg
        value =
           case episode.itunes.duration of
            Just duration ->
                text duration

            Nothing ->
                text "n/a"
    in
    span [ class "mr-2" ] [ text "duration:", value ]


viewPodlovePlayer : Episode -> Html Msg
viewPodlovePlayer episode =
    let
        playerModel : PodlovePlayer.Model
        playerModel =
            episodeToPodlovePlayerModel episode
    in
    wrapPodloveButtonHtml (PodlovePlayer.view playerModel)


--- HTTP ---


getEpisode : String -> Cmd Msg
getEpisode id =
    RestApi.getEpisode (RemoteData.fromResult >> GotEpisodeData) id



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


wrapPodlovePlayerMsg : Cmd PodlovePlayer.Msg -> Cmd Msg
wrapPodlovePlayerMsg msg =
    Cmd.map PodlovePlayerMsg msg


wrapPodloveButtonHtml : Html PodlovePlayer.Msg -> Html Msg
wrapPodloveButtonHtml msg =
    Html.map PodlovePlayerMsg msg


modelToPodlovePlayerModel : Model -> PodlovePlayer.Model
modelToPodlovePlayerModel model =
    case model.episode of
        RemoteData.Success episode ->
            episodeToPodlovePlayerModel episode

        _ ->
            PodlovePlayer.emptyModel

episodeToPodlovePlayerModel : Episode -> PodlovePlayer.Model
episodeToPodlovePlayerModel episode =
    let
        toPlayerModel : Episode -> PodlovePlayer.Model
        toPlayerModel e =
            -- TODO
             { duration = episode.itunes.duration
                , audio =
                    { url = episode.enclosure.url
                    , size = episode.enclosure.length
                    , mimeType = episode.enclosure.typ
                    }
                -- TODO set the chapters; add them to episode
                , chapters = []
                , theme =
                    { main = Just podlovePlayerThemeMain
                    , highlight = Just podlovePlayerThemeHighlight
                    }
                , tabs =
                    { chapters = Just True
                    }
                , visibleComponents =
                    [ "tabChapters"
                    , "tabAudio"
                    , "progressbar"
                    , "controlSteppers"
                    , "controlChapters"
                    ]
                }

    in
    toPlayerModel episode
