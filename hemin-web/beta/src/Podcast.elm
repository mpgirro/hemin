module Podcast exposing (..)

import Json.Decode exposing (Decoder, field, string)

type alias Podcast = 
  {
    id : String,
    title : String,
    link : String,
    description : String,
    itunes : PodcastItunes
  }

type alias PodcastItunes =
  {
    summary : String,
    author : String
  }

podcastDecoder : Decoder Podcast
podcastDecoder =
  Json.Decode.map5 Podcast
    (field "id" string)
    (field "title" string)
    (field "link" string)
    (field "description" string)
    (field "itunes" podcastItunesDecoder)

podcastItunesDecoder : Decoder PodcastItunes
podcastItunesDecoder = 
  Json.Decode.map2 PodcastItunes
    (field "summary" string)
    (field "author" string)