port module Podlove.WebPlayer exposing
    ( Model
    , Msg(..)
    , emptyModel
    , init
    , sendPodloveWebPlayerModel
    , update
    , view
    )

import Html exposing (Html, div)
import Html.Attributes exposing (id)
import Json.Encode exposing (encode, list, object, string)
import Util exposing (encodeMaybeBool, encodeMaybeString)


---- OUTBOUND PORTS ----


port podloveWebPlayer : Json.Encode.Value -> Cmd msg


sendPodloveWebPlayerModel : Model -> Cmd msg
sendPodloveWebPlayerModel model =
    let
        json : Json.Encode.Value
        json =
            encodeModel model
    in
    podloveWebPlayer json



---- MODEL ----


type alias Model =
    { duration : Maybe String
    , audio : Audio
    , chapters : List Chapter
    , theme : Theme
    , tabs : Tabs
    , visibleComponents : List String
    }


emptyModel : Model
emptyModel =
    { duration = Nothing
    , audio =
        { url = Nothing
        , size = Nothing
        , mimeType = Nothing
        }
    , chapters = []
    , theme =
        { main = Nothing
        , highlight = Nothing
        }
    , tabs =
        { chapters = Nothing
        }
    , visibleComponents = []
    }


type alias Audio =
    { url : Maybe String
    , size : Maybe String
    , mimeType : Maybe String
    }


type alias Chapter =
    { start : Maybe String
    , title : Maybe String
    , href : Maybe String
    , image : Maybe String
    }


type alias Theme =
    { main : Maybe String
    , highlight : Maybe String
    }


type alias Tabs =
    { chapters : Maybe Bool
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
            podloveWebPlayer json
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
            ( model, podloveWebPlayer json )



--- VIEW ---


view : Model -> Html Msg
view _ =
    div [ id "podlove-web-player" ] []



--- JSON ---


encodeModel : Model -> Json.Encode.Value
encodeModel model =
    object
        [ ( "duration", encodeMaybeString model.duration )
        , ( "audio", encodeAudio model.audio )
        , ( "chapters", list encodeChapter model.chapters )
        , ( "theme", encodeTheme model.theme )
        , ( "tabs", encodeTabs model.tabs )
        , ( "visibleComponents", list string [] )
        ]


encodeAudio : Audio -> Json.Encode.Value
encodeAudio audio =
    object
        [ ( "url", encodeMaybeString audio.url )
        , ( "size", encodeMaybeString audio.size )
        , ( "mimeType", encodeMaybeString audio.mimeType )
        ]


encodeChapter : Chapter -> Json.Encode.Value
encodeChapter chapter =
    object
        [ ( "start", encodeMaybeString chapter.start )
        , ( "title", encodeMaybeString chapter.title )
        , ( "href", encodeMaybeString chapter.href )
        , ( "image", encodeMaybeString chapter.image )
        ]


encodeTheme : Theme -> Json.Encode.Value
encodeTheme theme =
    object
        [ ( "main", encodeMaybeString theme.main )
        , ( "title", encodeMaybeString theme.highlight )
        ]


encodeTabs : Tabs -> Json.Encode.Value
encodeTabs tabs =
    object
        [ ( "chapters", encodeMaybeBool tabs.chapters )
        ]
