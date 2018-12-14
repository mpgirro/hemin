module RestApi exposing (apiBase, getAllPodcasts, getEpisode, getPodcast, getSearchResult)

import Data.Episode exposing (Episode, episodeDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder, podcastListDecoder)
import Data.ResultPage exposing (ResultPage, resultPageDecoder)
import Http


apiBase : String
apiBase =
    -- local alternative: "http://localhost:9000/api/v1"
    -- production: "https://api.hemin.io/api/v1"
    "https://api.hemin.io/json-examples"


getEpisode : (Result Http.Error Episode -> msg) -> String -> Cmd msg
getEpisode resultWrapper id =
    Http.get
        { url = apiBase ++ "/episode.json"
        , expect = Http.expectJson resultWrapper episodeDecoder
        }


getPodcast : (Result Http.Error Podcast -> msg) -> String -> Cmd msg
getPodcast resultWrapper id =
    Http.get
        { url = apiBase ++ "/podcast.json"
        , expect = Http.expectJson resultWrapper podcastDecoder
        }


getAllPodcasts : (Result Http.Error (List Podcast) -> msg) -> Int -> Int -> Cmd msg
getAllPodcasts resultWrapper pageNumber pageSize =
    Http.get
        { url = apiBase ++ "/podcast-list.json"
        , expect = Http.expectJson resultWrapper podcastListDecoder
        }


getSearchResult : (Result Http.Error ResultPage -> msg) -> Maybe String -> Maybe Int -> Maybe Int -> Cmd msg
getSearchResult resultWrapper query pageNumber pageSize =
    Http.get
        { url = apiBase ++ "/search.json"
        , expect = Http.expectJson resultWrapper resultPageDecoder
        }
