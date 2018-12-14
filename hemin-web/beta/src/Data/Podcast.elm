module Data.Podcast exposing (Podcast, PodcastItunes, emptyPodcastItunes, podcastDecoder, podcastItunesDecoder)

import Json.Decode exposing (Decoder, bool, field, list, string, maybe)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)



-- TYPES


type alias Podcast =
    { id : String
    , title : String
    , link : String
    , description : String
    , pubDate : Maybe String
    , lastBuildDate : Maybe String
    , language : Maybe String
    , generator : Maybe String
    , copyright : Maybe String
    , docs : Maybe String
    , managingEditor : Maybe String
    , image : Maybe String
    --, atomLinks : List AtomLink
    , itunes : PodcastItunes
    --,registration : PodcastRegistration
    --,feedpress : PodcastFeedpress
    --,fyyd : PodcastFyyd
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
        |> optional "pubDate" (maybe string) Nothing
        |> optional "lastBuildDate" (maybe string) Nothing
        |> optional "language" (maybe string) Nothing
        |> optional "generator" (maybe string) Nothing
        |> optional "copyright" (maybe string) Nothing
        |> optional "docs" (maybe string) Nothing
        |> optional "managingEditor" (maybe string) Nothing
        |> optional "image" (maybe string) Nothing
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
