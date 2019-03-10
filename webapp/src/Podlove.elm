module Podlove exposing (SubscribeButtonConfig, WebPlayerConfig)

import Data.Episode exposing (Episode)
import Data.Feed exposing (Feed)
import Html exposing (Html)
import Html.Attributes
import Json.Encode exposing (bool, encode, float, int, list, object, string)
import Util exposing (emptyHtml)



--- PORTS ---


port podloveWebPlayer : String -> Cmd msg


port podloveSubscribeButton : String -> Cmd msg



---- MODEL ----


type Model
    = WebPlayerConfig
    | SubscribeButtonConfig


type alias WebPlayerConfig =
    {}


type alias SubscribeButtonConfig =
    { title : Maybe String
    , subtitle : Maybe String
    , description : Maybe String
    , cover : Maybe String
    , feeds : List SubscribeButtonFeed
    }


type alias SubscribeButtonFeed =
    { type_ : Maybe String
    , format : Maybe String
    , url : Maybe String
    , variant : Maybe String
    , directoryUrlItunes : Maybe String
    }



--- JSON ---


encodeWebPlayerConfig : WebPlayerConfig -> Json.Encode.Value
encodeWebPlayerConfig config =
    -- TODO implement
    Json.Encode.null


encodeSubscribeButtonConfig : SubscribeButtonConfig -> Json.Encode.Value
encodeSubscribeButtonConfig config =
    object
        [ ( "title", encodeMaybeString config.title )
        , ( "subtitle", encodeMaybeString config.subtitle )
        , ( "description", encodeMaybeString config.description )
        , ( "cover", encodeMaybeString config.cover )
        , ( "feeds", list encodeSubscribeButtonFeed config.feeds )
        ]


encodeSubscribeButtonFeed : SubscribeButtonFeed -> Json.Encode.Value
encodeSubscribeButtonFeed config =
    object
        [ ( "type", encodeMaybeString config.type_ )
        , ( "format", encodeMaybeString config.format )
        , ( "url", encodeMaybeString config.url )
        , ( "variant", encodeMaybeString config.variant )
        , ( "directory-url-itunes", encodeMaybeString config.directoryUrlItunes )
        ]


encodeMaybeString : Maybe String -> Json.Encode.Value
encodeMaybeString maybeString =
    case maybeString of
        Just str ->
            string str

        Nothing ->
            Json.Encode.null



---- UPDATE ----


type Msg
    = SendToJs


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        SendToJs ->
            ( model, toJs str )



---- VIEW ----


viewWebPlayer : Episode -> Html msg
viewWebPlayer episode =
    -- TODO implement
    emptyHtml


viewSubscribeButton : Feed -> Html msg
viewSubscribeButton feed =
    -- TODO implement
    emptyHtml
