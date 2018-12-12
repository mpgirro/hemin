module PodcastPage exposing (Model, Msg(..), init, update, view)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Podcast exposing (..)
import Skeleton exposing (Page, viewHttpFailure)


-- MAIN


main =
    Browser.element
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        }



-- MODEL


type Model
    = Failure Http.Error
    | Loading
    | Content Podcast


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getPodcast "" )


initWithId : String -> ( Model, Cmd Msg )
initWithId id =
    ( Loading, getPodcast id )



-- UPDATE


type Msg
    = LoadPodcast String
    | LoadedPodcast (Result Http.Error Podcast)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadPodcast id ->
            ( model, getPodcast id )

        LoadedPodcast result ->
            case result of
                Ok podcast ->
                    ( Content podcast, Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- VIEW


view : Model -> Html msg
view model =
    case model of
        Failure cause ->
            Skeleton.viewHttpFailure cause

        Loading ->
            text "Loading..."

        Content podcast ->
            viewPodcast podcast


viewPodcast : Podcast -> Html msg
viewPodcast podcast =
    div []
        [ h1 [] [ text podcast.title ]
        , a [ href podcast.link ] [ text podcast.link ]
        , p [] [ text podcast.description ]
        ]



-- HTTP


getPodcast : String -> Cmd Msg
getPodcast id =
    -- TODO id is currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/podcast.json"
        , expect = Http.expectJson LoadedPodcast podcastDecoder
        }
