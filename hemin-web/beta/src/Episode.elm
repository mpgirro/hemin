module Episode exposing (Episode, EpisodeItunes, emptyEpisodeItunes, episodeDecoder, episodeItunesDecoder)

import Json.Decode exposing (Decoder, field, int, nullable, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)



-- TYPES


type alias Episode =
    { id : String
    , title : String
    , link : String
    , description : String
    , itunes : EpisodeItunes
    }


type alias EpisodeItunes =
    { duration : String
    , subtitle : String
    , author : String
    , summary : String
    , season : Int
    , episode : Int
    , episodeType : String
    }



-- DEFAULTS


emptyEpisodeItunes : EpisodeItunes
emptyEpisodeItunes =
    { duration = ""
    , subtitle = ""
    , author = ""
    , summary = ""
    , season = 0
    , episode = 0
    , episodeType = ""
    }



-- JSON


episodeDecoder : Decoder Episode
episodeDecoder =
    Json.Decode.succeed Episode
        |> required "id" string
        |> optional "title" string ""
        |> optional "link" string ""
        |> optional "description" string ""
        |> optional "itunes" episodeItunesDecoder emptyEpisodeItunes


episodeItunesDecoder : Decoder EpisodeItunes
episodeItunesDecoder =
    Json.Decode.succeed EpisodeItunes
        |> optional "duration" string ""
        |> optional "subtitle" string ""
        |> optional "author" string ""
        |> optional "summary" string ""
        |> optional "season" int 0
        |> optional "episode" int 0
        |> optional "episodeType" string ""
