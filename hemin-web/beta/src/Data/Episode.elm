module Data.Episode exposing (Episode, episodeDecoder)

import Data.Chapter exposing (Chapter, chapterDecoder)
import Json.Decode exposing (Decoder, field, int, nullable, string, list, maybe, bool)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)



--- MODELS ---


type alias Episode =
    { id : String
    , podcastId : Maybe String
    , podcastTitle : Maybe String
    , title : String
    , link : String
    , description : String
    , pubDate : Maybe String
    , guid : Maybe String
    , guidIsPermalink : Maybe Bool
    , image : Maybe String
    , contentEncoded : Maybe String
    --, atomLinks : List AtomLink
    , chapters : List Chapter
    , itunes : EpisodeItunes
    , enclosure : EpisodeEnclosure
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

type alias EpisodeEnclosure =
    { url : Maybe String
    , length : Maybe Int
    , typ : Maybe String
    }


--- DEFAULTS ---


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

emptyEpisodeEnclosure : EpisodeEnclosure
emptyEpisodeEnclosure =
    { url = Nothing
    , length = Nothing
    , typ = Nothing
    }

--- JSON ---


episodeDecoder : Decoder Episode
episodeDecoder =
    Json.Decode.succeed Episode
        |> required "id" string
        |> optional "podcastId" (maybe string) Nothing
        |> optional "podcastTitle" (maybe string) Nothing
        |> optional "title" string ""
        |> optional "link" string ""
        |> optional "description" string ""
        |> optional "pubDate" (maybe string) Nothing
        |> optional "guid" (maybe string) Nothing
        |> optional "guidIsPermalink" (maybe bool) Nothing
        |> optional "image" (maybe string) Nothing
        |> optional "contentEncoded" (maybe string) Nothing
        |> optional "chapters" (list chapterDecoder) []
        |> optional "itunes" episodeItunesDecoder emptyEpisodeItunes
        |> optional "enclosure" episodeEnclosureDecoder emptyEpisodeEnclosure


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

episodeEnclosureDecoder : Decoder EpisodeEnclosure
episodeEnclosureDecoder =
    Json.Decode.succeed EpisodeEnclosure
        |> optional "url" (maybe string) Nothing
        |> optional "length" (maybe int) Nothing
        |> optional "typ" (maybe string) Nothing