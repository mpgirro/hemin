module Data.Episode exposing (Episode, episodeDecoder, episodeListDecoder)

import Data.Chapter exposing (Chapter, chapterDecoder)
import Json.Decode exposing (Decoder, bool, field, int, list, maybe, string)
import Json.Decode.Pipeline exposing (optional, required)



--- MODELS ---


type alias Episode =
    { id : String
    , podcastId : String
    , podcastTitle : Maybe String
    , title : Maybe String
    , link : Maybe String
    , description : Maybe String
    , pubDate : Maybe String
    , guid : Maybe String
    , guidIsPermalink : Maybe Bool
    , image : Maybe String
    , contentEncoded : Maybe String
    , chapters : List Chapter
    , itunes : EpisodeItunes
    , enclosure : EpisodeEnclosure
    }


type alias EpisodeItunes =
    { duration : Maybe String
    , subtitle : Maybe String
    , author : Maybe String
    , summary : Maybe String
    , season : Maybe Int
    , episode : Maybe Int
    , episodeType : Maybe String
    }


type alias EpisodeEnclosure =
    { url : Maybe String
    , length : Maybe Int
    , typ : Maybe String
    }



--- DEFAULTS ---


emptyEpisodeItunes : EpisodeItunes
emptyEpisodeItunes =
    { duration = Nothing
    , subtitle = Nothing
    , author = Nothing
    , summary = Nothing
    , season = Nothing
    , episode = Nothing
    , episodeType = Nothing
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
        |> required "podcastId" string
        |> optional "podcastTitle" (maybe string) Nothing
        |> optional "title" (maybe string) Nothing
        |> optional "link" (maybe string) Nothing
        |> optional "description" (maybe string) Nothing
        |> optional "pubDate" (maybe string) Nothing
        |> optional "guid" (maybe string) Nothing
        |> optional "guidIsPermalink" (maybe bool) Nothing
        |> optional "image" (maybe string) Nothing
        |> optional "contentEncoded" (maybe string) Nothing
        |> optional "chapters" (list chapterDecoder) []
        |> optional "itunes" episodeItunesDecoder emptyEpisodeItunes
        |> optional "enclosure" episodeEnclosureDecoder emptyEpisodeEnclosure


episodeListDecoder : Decoder (List Episode)
episodeListDecoder =
    field "results" (list episodeDecoder)


episodeItunesDecoder : Decoder EpisodeItunes
episodeItunesDecoder =
    Json.Decode.succeed EpisodeItunes
        |> optional "duration" (maybe string) Nothing
        |> optional "subtitle" (maybe string) Nothing
        |> optional "author" (maybe string) Nothing
        |> optional "summary" (maybe string) Nothing
        |> optional "season" (maybe int) Nothing
        |> optional "episode" (maybe int) Nothing
        |> optional "episodeType" (maybe string) Nothing


episodeEnclosureDecoder : Decoder EpisodeEnclosure
episodeEnclosureDecoder =
    Json.Decode.succeed EpisodeEnclosure
        |> optional "url" (maybe string) Nothing
        |> optional "length" (maybe int) Nothing
        |> optional "typ" (maybe string) Nothing
