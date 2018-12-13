module Data.Podcast exposing (Podcast, PodcastItunes, emptyPodcastItunes, podcastDecoder, podcastItunesDecoder)

import Json.Decode exposing (Decoder, bool, field, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)



-- TYPES


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



-- DEFAULTS


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



-- JSON


podcastDecoder : Decoder Podcast
podcastDecoder =
    Json.Decode.succeed Podcast
        |> required "id" string
        |> optional "title" string ""
        |> optional "link" string ""
        |> optional "description" string ""
        |> optional "itunes" podcastItunesDecoder emptyPodcastItunes


podcastItunesDecoder : Decoder PodcastItunes
podcastItunesDecoder =
    Json.Decode.succeed PodcastItunes
        |> optional "summary" string ""
        |> optional "author" string ""
        |> optional "keywords" (list string) []
        |> optional "categories" (list string) []
        |> optional "explicit" bool False
        |> optional "block" bool False
        |> optional "podcastType" string ""
        |> optional "ownerName" string ""
        |> optional "ownerEmail" string ""
