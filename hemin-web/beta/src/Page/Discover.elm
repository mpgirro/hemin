module Page.Discover exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import Data.Podcast exposing (Podcast)
import Html exposing (Html, a, div, img, li, text, ul)
import Html.Attributes exposing (alt, class, href, src)
import Http
import Page.Error as ErrorPage
import RemoteData exposing (WebData)
import RestApi
import Router exposing (redirectToPodcast)
import Skeleton exposing (Page)
import Util exposing (maybeAsString, maybeAsText)



---- MODEL ----


type alias Model =
    { podcasts : WebData (List Podcast) }


init : ( Model, Cmd Msg )
init =
    let
        model : Model
        model =
            { podcasts = RemoteData.NotAsked }

        cmd : Cmd Msg
        cmd =
            getAllPodcast 1 36
    in
    ( model, cmd )



---- UPDATE ----


type Msg
    = LoadDiscover Int Int
    | LoadedDiscover (WebData (List Podcast))


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadDiscover pageNumber pageSize ->
            ( model, getAllPodcast pageNumber pageSize )

        LoadedDiscover podcasts ->
            ( { model | podcasts = podcasts }, Cmd.none )



---- VIEW ----


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Discover"

        body : Html Msg
        body =
            case model.podcasts of
                RemoteData.NotAsked ->
                    text "Initialising..."

                RemoteData.Loading ->
                    text "Loading..."

                RemoteData.Failure error ->
                    ErrorPage.viewHttpFailure error

                RemoteData.Success podcasts ->
                    viewDiscover podcasts
    in
    ( title, body )


viewDiscover : List Podcast -> Html Msg
viewDiscover podcasts =
    div []
        [ div [ class "Subhead" ]
            [ div [ class "Subhead-heading" ]
                [ text "Discover Podcasts" ]
            , div [ class "Subhead-description" ]
                [ text "Discover new podcasts in this grid of cover images" ]
            ]
        , ul [ class "list-style-none" ] <|
            List.map viewPodcastCover podcasts
        ]


viewPodcastCover : Podcast -> Html Msg
viewPodcastCover podcast =
    li [ class "d-inline-block", class "col-2", class "p-2" ]
        [ a [ href (redirectToPodcast podcast) ]
            [ img
                [ class "width-full"
                , class "avatar"
                , src (maybeAsString podcast.image)
                , alt (maybeAsString podcast.title)
                ]
                []
            ]
        ]



--- HTTP ---


getAllPodcast : Int -> Int -> Cmd Msg
getAllPodcast pageNumber pageSize =
    RestApi.getAllPodcasts (RemoteData.fromResult >> LoadedDiscover) pageNumber pageSize
