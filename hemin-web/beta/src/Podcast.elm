import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onInput)



-- MAIN


main =
  Browser.sandbox { init = init, update = update, view = view }



-- MODEL


type alias Podcast =
  { 
    id : String,
    title : String,
    link : String,
    description : String
  }


init : Podcast
init =
  { 
    id = "test id", 
    title = "test title" ,
    link = "test link",
    description = "test description"
  }



-- UPDATE


type Msg
  = Change Podcast


update : Msg -> Podcast -> Podcast
update msg p =
  case msg of
    Change newP ->
      { p | id = newP.id, title = newP.title, link = newP.link, description = newP.description }


-- VIEW


view : Podcast -> Html Msg
view podcast =
  div []
    [ h1 [] [ text podcast.title ]
    , a [ href podcast.link ] [ text podcast.link ]
    , p [] [ text podcast.description ]
    ]