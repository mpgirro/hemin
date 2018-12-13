module Data.IndexDoc exposing (IndexDoc, indexDocDecoder)

import Json.Decode exposing (Decoder, bool, field, int, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)

--- MODELS ---

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

--- DEFAULTS ---




--- JSON ---

indexDocDecoder : Decoder IndexDoc
indexDocDecoder =
    Json.Decode.succeed IndexDoc
        |> required "docType" string
        |> required "id" string
        |> optional "title" string ""
        |> optional "link" string ""
        |> optional "description" string ""
        |> optional "pubDate" string ""
        |> optional "image" string ""
        |> optional "itunesAuthor" string ""
        |> optional "itunesSummary" string ""
        |> optional "podcastTitle" string ""