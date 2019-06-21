port module Podlove.SubscribeButton exposing
    ( Feed
    , Model
    , Msg(..)
    , emptyModel
    , init
    , sendPodloveSubscribeButtonModel
    , update
    , view
    )

import Html exposing (Html, div)
import Html.Attributes exposing (id)
import Json.Encode exposing (encode, list, object, string)
import Util exposing (encodeMaybeString)



---- OUTBOUND PORTS ----


port podloveSubscribeButton : Json.Encode.Value -> Cmd msg


sendPodloveSubscribeButtonModel : Model -> Cmd msg
sendPodloveSubscribeButtonModel model =
    let
        json : Json.Encode.Value
        json =
            encodeModel model
    in
    podloveSubscribeButton json



---- MODEL ----


type alias Model =
    { title : Maybe String
    , subtitle : Maybe String
    , description : Maybe String
    , cover : Maybe String
    , feeds : List Feed
    }


type alias Feed =
    { type_ : Maybe String
    , format : Maybe String
    , url : Maybe String
    , variant : Maybe String
    , directoryUrlItunes : Maybe String
    }


emptyModel : Model
emptyModel =
    { title = Nothing
    , subtitle = Nothing
    , description = Nothing
    , cover = Nothing
    , feeds = []
    }


init : ( Model, Cmd Msg )
init =
    let
        model : Model
        model =
            emptyModel

        json : Json.Encode.Value
        json =
            encodeModel model

        cmd : Cmd Msg
        cmd =
            podloveSubscribeButton json
    in
    ( model, cmd )



--- UPDATE --


type Msg
    = SendToJs


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SendToJs ->
            let
                json : Json.Encode.Value
                json =
                    encodeModel model
            in
            ( model, podloveSubscribeButton json )



--- VIEW ---


view : Model -> Html Msg
view _ =
    div [ id "podlove-subscribe-button-container" ] []



--- JSON ---


encodeModel : Model -> Json.Encode.Value
encodeModel model =
    object
        [ ( "title", encodeMaybeString model.title )
        , ( "subtitle", encodeMaybeString model.subtitle )
        , ( "description", encodeMaybeString model.description )
        , ( "cover", encodeMaybeString model.cover )
        , ( "feeds", list encodeFeed model.feeds )
        ]


encodeFeed : Feed -> Json.Encode.Value
encodeFeed feed =
    object
        [ ( "type", encodeMaybeString feed.type_ )
        , ( "format", encodeMaybeString feed.format )
        , ( "url", encodeMaybeString feed.url )
        , ( "variant", encodeMaybeString feed.variant )
        , ( "directory-url-itunes", encodeMaybeString feed.directoryUrlItunes )
        ]
