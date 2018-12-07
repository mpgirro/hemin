module Episode exposing (..)

import Json.Decode exposing (Decoder, field, string, int, nullable)

type alias Episode =
  {
    id : String,
    title : String,
    link : String,
    description : String,
    itunes : EpisodeItunes
  }

type alias EpisodeItunes =
  {
    duration : String,
    subtitle : String,
    author : String,
    summary : String,
    season : Int,
    episode : Int,
    episodeType : String
  }

episodeDecoder : Decoder Episode
episodeDecoder =
  Json.Decode.map5 Episode
    (field "id" string)
    (field "title" string)
    (field "link" string)
    (field "description" string)
    (field "itunes" episodeItunesDecoder)

episodeItunesDecoder : Decoder EpisodeItunes
episodeItunesDecoder = 
  Json.Decode.map7 EpisodeItunes
    (field "duration" string)
    (field "subtitle" string)
    (field "author" string)
    (field "summary" string)
    (field "season" int)
    (field "episode" int)
    (field "episodeType" string)