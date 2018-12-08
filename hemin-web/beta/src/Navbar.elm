module Navbar exposing (view)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)

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
  = Default


init : () -> (Model, Cmd Msg)
init _ =
  ( Default , Cmd.none )


type Msg
  = NothinhgHappening


update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  (model, Cmd.none) -- currently the navbar does not do anything!



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none


-- VIEW


view : Model -> Html Msg
view model = -- the model is currently ignored! (we neither hold state nor provide functionality)
  nav [] 
    [ a [ href "/" ] [ text "HEMIN" ]
    , ul []
      [ li [] [ a [ href "/search" ] [ text "search" ] ]
      , li [] [ a [ href "/discover" ] [ text "discover" ] ]  
      , li [] [ a [ href "/propose" ] [ text "+feed" ] ]
      ]
    ]