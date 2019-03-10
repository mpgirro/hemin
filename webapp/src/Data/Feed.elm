module Data.Feed exposing (Feed, feedDecoder, feedListDecoder)

import Json.Decode exposing (Decoder, field, list, maybe, string)
import Json.Decode.Pipeline exposing (optional, required)
import Time exposing (Posix)
import Util exposing (decodePosix)


type alias Feed =
    { id : String
    , podcastId : String
    , url : Maybe String
    , lastChecked : Maybe Posix
    , lastStatus : Maybe String
    }


feedDecoder : Decoder Feed
feedDecoder =
    Json.Decode.succeed Feed
        |> required "id" string
        |> required "podcastId" string
        |> optional "url" (maybe string) Nothing
        |> optional "lastChecked" (maybe decodePosix) Nothing
        |> optional "lastStatus" (maybe string) Nothing


feedListDecoder : Decoder (List Feed)
feedListDecoder =
    field "results" (list feedDecoder)
