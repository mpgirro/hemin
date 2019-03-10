module Data.DatabaseStats exposing (DatabaseStats, databaseStatsDecoder)

import Json.Decode exposing (Decoder, int)
import Json.Decode.Pipeline exposing (required)



--- MODELS ---


type alias DatabaseStats =
    { podcastCount : Int
    , episodeCount : Int
    , feedCount : Int
    , imageCount : Int
    }



--- JSON ---


databaseStatsDecoder : Decoder DatabaseStats
databaseStatsDecoder =
    Json.Decode.succeed DatabaseStats
        |> required "podcastCount" int
        |> required "episodeCount" int
        |> required "feedCount" int
        |> required "imageCount" int
