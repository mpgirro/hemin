module Page.PodcastDetail exposing (Model, Msg(..), init, view, update)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Podcast exposing (..)


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
  | Success Podcast


init : () -> (Model, Cmd Msg)
init _ =
  ( Loading , getPodcast )



-- UPDATE


type Msg
  = GotPodcast (Result Http.Error Podcast)
--  | GotEpisodes (Result Http.Error String)


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    GotPodcast result ->
      case result of
        Ok podcast ->
          (Success podcast, Cmd.none)

        Err cause ->
          (Failure cause, Cmd.none)



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none



-- VIEW


view : Model -> Html Msg
view model =
  case model of
    Failure cause ->
      viewHttpFailure cause

    Loading ->
      text "Loading..."

    Success podcast ->
      viewPodcast podcast

viewHttpFailure : Http.Error -> Html Msg
viewHttpFailure cause =
  case cause of 
    Http.BadUrl msg       -> text ("Unable to load the podcast; reason: " ++ msg)
    Http.Timeout          -> text "Unable to load the podcast; reason: timeout"
    Http.NetworkError     -> text "Unable to load the podcast; reason: network error"
    Http.BadStatus status -> text ("Unable to load the podcast; reason: status " ++ (String.fromInt status))
    Http.BadBody msg      -> text ("Unable to load the podcast; reason: " ++ msg)

viewPodcast : Podcast -> Html Msg
viewPodcast podcast =
  div []
    [ h1 [] [ text podcast.title ]
    , a [ href podcast.link ] [ text podcast.link ]
    , p [] [ text podcast.description ]
    ]

-- HTTP


getPodcast : Cmd Msg
getPodcast = 
  Http.get
    { url = "https://api.hemin.io/json-examples/podcast.json"
    , expect = Http.expectJson GotPodcast podcastDecoder
    }