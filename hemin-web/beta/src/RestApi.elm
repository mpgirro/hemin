module RestApi exposing (apiBase, getEpisode, getPodcast)

import Episode exposing (Episode, episodeDecoder)
import Http
import Podcast exposing (Podcast, podcastDecoder)


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
