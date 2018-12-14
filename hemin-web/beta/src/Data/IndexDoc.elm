module Data.IndexDoc exposing (IndexDoc, indexDocDecoder)

import Json.Decode exposing (Decoder, string, maybe)
import Json.Decode.Pipeline exposing (optional, required)

--- MODELS ---

type alias IndexDoc =
    { docType : String
    , id : String
    , title : Maybe String
    , link : Maybe String
    , description : Maybe String
    , pubDate : Maybe String
    , image : Maybe String
    , itunesAuthor : Maybe String
    , itunesSummary : Maybe String
    , podcastTitle : Maybe String
    }

--- DEFAULTS ---




--- JSON ---

indexDocDecoder : Decoder IndexDoc
indexDocDecoder =
    Json.Decode.succeed IndexDoc
        |> required "docType" string
        |> required "id" string
        |> optional "title" (maybe string) Nothing
        |> optional "link" (maybe string) Nothing
        |> optional "description" (maybe string) Nothing
        |> optional "pubDate" (maybe string) Nothing
        |> optional "image" (maybe string) Nothing
        |> optional "itunesAuthor" (maybe string) Nothing
        |> optional "itunesSummary" (maybe string) Nothing
        |> optional "podcastTitle" (maybe string) Nothing
