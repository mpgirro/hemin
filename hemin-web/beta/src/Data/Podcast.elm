module Data.Podcast exposing (Podcast, podcastDecoder, podcastListDecoder)

import Json.Decode exposing (Decoder, bool, field, list, maybe, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)



-- TYPES


type alias Podcast =
    { id : String
    , title : Maybe String
    , link : Maybe String
    , description : Maybe String
    , pubDate : Maybe String
    , lastBuildDate : Maybe String
    , language : Maybe String
    , generator : Maybe String
    , copyright : Maybe String
    , docs : Maybe String
    , managingEditor : Maybe String
    , image : Maybe String
    , itunes : PodcastItunes
    }


type alias PodcastItunes =
    { summary : Maybe String
    , author : Maybe String
    , keywords : List String
    , categories : List String
    , explicit : Maybe Bool
    , block : Maybe Bool
    , podcastType : Maybe String
    , ownerName : Maybe String
    , ownerEmail : Maybe String
    }



-- DEFAULTS


emptyPodcastItunes : PodcastItunes
emptyPodcastItunes =
    { summary = Nothing
    , author = Nothing
    , keywords = []
    , categories = []
    , explicit = Nothing
    , block = Nothing
    , podcastType = Nothing
    , ownerName = Nothing
    , ownerEmail = Nothing
    }



-- JSON


podcastDecoder : Decoder Podcast
podcastDecoder =
    Json.Decode.succeed Podcast
        |> required "id" string
        |> optional "title" (maybe string) Nothing
        |> optional "link" (maybe string) Nothing
        |> optional "description" (maybe string) Nothing
        |> optional "pubDate" (maybe string) Nothing
        |> optional "lastBuildDate" (maybe string) Nothing
        |> optional "language" (maybe string) Nothing
        |> optional "generator" (maybe string) Nothing
        |> optional "copyright" (maybe string) Nothing
        |> optional "docs" (maybe string) Nothing
        |> optional "managingEditor" (maybe string) Nothing
        |> optional "image" (maybe string) Nothing
        |> optional "itunes" podcastItunesDecoder emptyPodcastItunes


podcastListDecoder : Decoder (List Podcast)
podcastListDecoder =
    field "results" (list podcastDecoder)


podcastItunesDecoder : Decoder PodcastItunes
podcastItunesDecoder =
    Json.Decode.succeed PodcastItunes
        |> optional "summary" (maybe string) Nothing
        |> optional "author" (maybe string) Nothing
        |> optional "keywords" (list string) []
        |> optional "categories" (list string) []
        |> optional "explicit" (maybe bool) Nothing
        |> optional "block" (maybe bool) Nothing
        |> optional "podcastType" (maybe string) Nothing
        |> optional "ownerName" (maybe string) Nothing
        |> optional "ownerEmail" (maybe string) Nothing
