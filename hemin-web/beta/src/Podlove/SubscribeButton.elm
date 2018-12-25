port module Podlove.SubscribeButton exposing
    ( Feed
    , Msg
    , Model
    , emptyModel
    , init
    , update
    , view
    )

import Html exposing (Html, div)
import Html.Attributes exposing (id)
--import Json.Encode exposing (encode, string, list, object)

---- OUTBOUND PORTS ----

port podloveSubscribeButton : Model -> Cmd msg

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
    { title = Just "test title"
    , subtitle = Just "test subtitle"
    , description = Just "test description"
    , cover = Just "http://example.org/cover.jpg"
    , feeds =
        [
            { type_ = Just "audio"
            , format = Just "mp3"
            , url = Just "http://exampe.org/feed.rss"
            , variant = Nothing
            , directoryUrlItunes = Nothing
            }
        ]
    }


init : ( Model, Cmd Msg )
init =
    let
        model : Model
        model = emptyModel

        cmd : Cmd Msg
        cmd = podloveSubscribeButton model
    in
    ( model, cmd )

--- UPDATE --


type Msg
    = SendToJs


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
         SendToJs ->
            {--
            let
                value : Json.Encode.Value
                value = encodeConfig model
            in
            --}
            ( model, podloveSubscribeButton model )


--- VIEW ---

view : Model -> Html Msg
view _ =
    div [ id "podlove-subscribe-button" ] []


--- JSON ---

{--
encodeConfig : Model -> Json.Encode.Value
encodeConfig config =
  object
    [ ("title", encodeMaybeString config.title)
    , ("subtitle", encodeMaybeString config.subtitle)
    , ("description", encodeMaybeString config.description)
    , ("cover", encodeMaybeString config.cover)
    , ("feeds", (list encodeFeed) config.feeds)
    ]


encodeFeed : Feed -> Json.Encode.Value
encodeFeed config =
  object
    [ ("type", encodeMaybeString config.type_)
    , ("format", encodeMaybeString config.format)
    , ("url", encodeMaybeString config.url)
    , ("variant", encodeMaybeString config.variant)
    , ("directory-url-itunes", encodeMaybeString config.directoryUrlItunes)
    ]


encodeMaybeString : Maybe String -> Json.Encode.Value
encodeMaybeString maybeString =
    case maybeString of
        Just s ->
            string s
        Nothing ->
            Json.Encode.null
--}
