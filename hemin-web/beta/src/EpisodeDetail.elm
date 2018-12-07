import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Episode exposing (..)


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
  | Success Episode


init : () -> (Model, Cmd Msg)
init _ =
  ( Loading , getEpisode )



-- UPDATE


type Msg
  = GotEpisode (Result Http.Error Episode)
--  | GotChapters (Result Http.Error String)


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    GotEpisode result ->
      case result of
        Ok episode ->
          (Success episode, Cmd.none)

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

    Success episode ->
      viewEpisode episode

viewHttpFailure : Http.Error -> Html Msg
viewHttpFailure cause =
  case cause of 
    Http.BadUrl msg       -> text ("Unable to load the episode; reason: " ++ msg)
    Http.Timeout          -> text "Unable to load the episode; reason: timeout"
    Http.NetworkError     -> text "Unable to load the episode; reason: network error"
    Http.BadStatus status -> text ("Unable to load the episode; reason: status " ++ (String.fromInt status))
    Http.BadBody msg      -> text ("Unable to load the episode; reason: " ++ msg)

viewEpisode : Episode -> Html Msg
viewEpisode episode =
  div []
    [ h1 [] [ text episode.title ]
    , a [ href episode.link ] [ text episode.link ]
    , p [] [ text episode.description ]
    ]

-- HTTP


getEpisode : Cmd Msg
getEpisode = 
  Http.get
    { url = "https://api.hemin.io/json-examples/episode.json" -- "http://localhost:9000/api/v1/episode/8DTKUxDwRO991"
    , expect = Http.expectJson GotEpisode episodeDecoder
    }