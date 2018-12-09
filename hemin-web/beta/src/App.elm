module App exposing (..)

import Routes exposing (..)
import Html exposing (..)
import Html.Attributes exposing (..)
import Html
import Navigation

-- MAIN


main =
  Browser.application
    { init = init
    , view = view
    , update = update
    , subscriptions = subscriptions
    , onUrlRequest = LinkClicked
    , onUrlChange = UrlChanged
    }



-- MODEL


type alias Model =
  { key : Nav.Key
  , page : Page
  }


type Page
  = NotFound Session.Data
  | SearchPage String Int Int
  | PodcastDetailPage Podcast
  | EpisoeDetailPage Episode



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none

-- VIEW


view : Model -> Browser.Document Msg
view model =
  case model.page of
    NotFound _ ->
      Skeleton.view never
        { title = "Not Found"
        , header = []
        , warning = Skeleton.NoProblems
        , attrs = Problem.styles
        , kids = Problem.notFound
        }

    SearchPage query page size ->
      Skeleton.view SearchMsg (Search.view search)

    PodcastDetailPage podcast ->
      Skeleton.view DocsMsg (Docs.view docs)

    EpisodeDetailPage episode ->
      Skeleton.view never (Diff.view diff)

    Help help ->
      Skeleton.view never (Help.view help)



-- INIT


init : () -> Url.Url -> Nav.Key -> ( Model, Cmd Msg )
init _ url key =
  stepUrl url
    { key = key
    , page = NotFound Session.empty
    }