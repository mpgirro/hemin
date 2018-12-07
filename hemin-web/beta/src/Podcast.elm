module Podcast exposing (..)

import Json.Decode exposing (Decoder, field, string, bool, list)
import Json.Decode.Pipeline exposing (required, optional, hardcoded)

type alias Podcast = 
  { id : String
  , title : String
  , link : String
  , description : String
  , itunes : PodcastItunes
  }

type alias PodcastItunes =
  { summary : String
  , author : String
  , keywords : List String
  , categories : List String
  , explicit : Bool
  , block : Bool
  , podcastType : String
  , ownerName : String
  , ownerEmail : String
  }

emptyPodcastItunes : PodcastItunes
emptyPodcastItunes = 
  { summary = ""
  , author = ""
  , keywords = []
  , categories = []
  , explicit = False
  , block = False
  , podcastType = ""
  , ownerName = ""
  , ownerEmail = ""
  }

podcastDecoder : Decoder Podcast
podcastDecoder =
  Json.Decode.succeed Podcast
    |> required "id" string
    |> optional "title" string "" -- 2nd string is fallback
    |> optional "link" string ""
    |> optional "description" string ""
    |> optional "itunes" podcastItunesDecoder emptyPodcastItunes

podcastItunesDecoder : Decoder PodcastItunes
podcastItunesDecoder =
  Json.Decode.succeed PodcastItunes
    |> optional "summary" string ""
    |> optional "author" string "" -- 2nd string is fallback
    |> optional "keywords" (list string) []
    |> optional "categories" (list string) []
    |> optional "explicit" bool False
    |> optional "block" bool False
    |> optional "podcastType" string ""
    |> optional "ownerName" string ""
    |> optional "ownerEmail" string ""