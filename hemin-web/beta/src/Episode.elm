module Episode exposing (..)

import Json.Decode exposing (Decoder, field, string, int, nullable)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)

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

episodeDecoder : Decoder Episode
episodeDecoder =
  Json.Decode.succeed Episode
    |> required "id" string
    |> optional "title" string "" -- 2nd string is fallback
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