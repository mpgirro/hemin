module Data.Feed exposing (Feed, feedDecoder)

import Json.Decode exposing (Decoder, string, maybe)
import Json.Decode.Pipeline exposing (optional, required)

type alias Feed = 
    { id : String
    , podcastId : String
    , url : Maybe String
    , lastChecked : Maybe String
    , lastStatus : Maybe String
    }


feedDecoder : Decoder Feed
feedDecoder =
    Json.Decode.succeed Feed
        |> required "id" string
        |> required "podcastId" string
        |> optional "url" (maybe string) Nothing
        |> optional "lastChecked" (maybe string) Nothing
        |> optional "lastStatus" (maybe string) Nothing
