module Data.Chapter exposing (Chapter, chapterDecoder)

import Json.Decode exposing (Decoder, string, maybe)
import Json.Decode.Pipeline exposing (optional)

--- MODELS ---

type alias Chapter = 
    { start : Maybe String
    , title : Maybe String
    , href : Maybe String
    , image : Maybe String
    }

--- JSON ---

chapterDecoder : Decoder Chapter
chapterDecoder =
    Json.Decode.succeed Chapter
        |> optional "start" (maybe string) Nothing
        |> optional "title" (maybe string) Nothing
        |> optional "href" (maybe string) Nothing
        |> optional "image" (maybe string) Nothing