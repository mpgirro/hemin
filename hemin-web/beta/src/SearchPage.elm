module Page.Search exposing (..)

import Browser
import Html exposing (Html, Attribute, div, input, text)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)
import Http
import Json.Decode exposing (Decoder, field, string, bool, list)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)




-- MAIN


main =
  Browser.element
    { init = init
    , update = update
    , subscriptions = subscriptions
    , view = view
    }


-- MODEL

type alias IndexDoc =
  { docType : String
  , id : String
  , title : String
  , link : String
  , description : String
  , pubDate : String
  , image : String
  , itunesAuthor : String
  , itunesSummary : String
  , podcastTitle : String
  }

type alias ResultPage =
  { currPage : Int
  , maxPage : Int
  , totalHits : Int
  , results : List IndexDoc
  }

emptyResultPage : ResultPage
emptyResultPage =
  { currPage = 0
  , maxPage = 0
  , totalHits = 0
  , results = []
  }


type Model
  = Failure Http.Error
  | Empty
  | Loading
  | Success ResultPage


init : Model
init =
  Empty



-- UPDATE


type Msg
  = SendSearchRequest String Int Int
  | GotSearchResult (Result Http.Error ResultPage)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    SendSearchRequest query page size ->
      (Empty, Cmd.none) -- TODO send HTTP request! 
    GotSearchResult result ->
      case result of
        Ok resultPage ->
          (Success resultPage, Cmd.none)

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

    Empty ->
      text "Ready to search:"

    Loading ->
      text "Loading..."

    Success resultPage ->
      viewResultPage resultPage

viewHttpFailure : Http.Error -> Html Msg
viewHttpFailure cause =
  case cause of 
    Http.BadUrl msg       -> text ("Unable to load the search results; reason: " ++ msg)
    Http.Timeout          -> text "Unable to load the search results; reason: timeout"
    Http.NetworkError     -> text "Unable to load the search results; reason: network error"
    Http.BadStatus status -> text ("Unable to load the search results; reason: status " ++ (String.fromInt status))
    Http.BadBody msg      -> text ("Unable to load the search results; reason: " ++ msg)

viewResultPage : ResultPage -> Html Msg
viewResultPage page =
  div []
    [ h1 [] [ text episode.title ]
    , a [ href episode.link ] [ text episode.link ]
    , p [] [ text episode.description ]
    ]