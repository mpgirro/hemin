module RestApi exposing (apiBase, getEpisode, getPodcast, getSearchResult)

import Episode exposing (Episode, episodeDecoder)
import Http
import Podcast exposing (Podcast, podcastDecoder)
import SearchResult exposing (ResultPage, resultPageDecoder)


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


getSearchResult : (Result Http.Error ResultPage -> msg) -> Maybe String -> Maybe Int -> Maybe Int -> Cmd msg
getSearchResult resultWrapper query pageNumber pageSize =
    Http.get
        { url = apiBase ++ "/search.json"
        , expect = Http.expectJson resultWrapper resultPageDecoder
        }