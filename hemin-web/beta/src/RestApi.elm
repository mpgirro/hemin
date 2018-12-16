module RestApi exposing (apiBase, getAllPodcasts, getEpisode, getPodcast, getSearchResult)

import Data.Episode exposing (Episode, episodeDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder, podcastListDecoder)
import Data.ResultPage exposing (ResultPage, resultPageDecoder)
import Http
import Maybe.Extra
import Util exposing (maybePageNumberParam, maybePageSizeParam, maybeQueryParam)


apiBase : String
apiBase =
    -- local alternative:
    "http://localhost:9000/api/v1"



-- production:
-- "https://api.hemin.io/api/v1"
-- JSON examples
--"https://api.hemin.io/json-examples"


getEpisode : (Result Http.Error Episode -> msg) -> String -> Cmd msg
getEpisode resultWrapper id =
    Http.get
        { --url = apiBase ++ "/episode.json"
          url = apiBase ++ "/episode/" ++ id
        , expect = Http.expectJson resultWrapper episodeDecoder
        }


getPodcast : (Result Http.Error Podcast -> msg) -> String -> Cmd msg
getPodcast resultWrapper id =
    Http.get
        { --url = apiBase ++ "/podcast.json"
          url = apiBase ++ "/podcast/" ++ id
        , expect = Http.expectJson resultWrapper podcastDecoder
        }


getAllPodcasts : (Result Http.Error (List Podcast) -> msg) -> Int -> Int -> Cmd msg
getAllPodcasts resultWrapper pageNumber pageSize =
    let
        p : String
        p =
            "p=" ++ String.fromInt pageNumber

        s : String
        s =
            "s=" ++ String.fromInt pageSize
    in
    Http.get
        { url = apiBase ++ "/podcast?" ++ p ++ "&" ++ s
        , expect = Http.expectJson resultWrapper podcastListDecoder
        }


getSearchResult : (Result Http.Error ResultPage -> msg) -> Maybe String -> Maybe Int -> Maybe Int -> Cmd msg
getSearchResult resultWrapper query pageNumber pageSize =
    let
        q =
            maybeQueryParam query

        p =
            maybePageNumberParam pageNumber

        s =
            maybePageSizeParam pageSize

        params : String
        params =
            String.join "&" (Maybe.Extra.values [ q, p, s ])

        urlQuery : String
        urlQuery =
            if params == "" then
                ""

            else
                "?" ++ params

        path : String
        path =
            "/search"
                ++ urlQuery
    in
    Http.get
        { --url = apiBase ++ "/search.json"
          url =
            apiBase
                ++ path
        , expect = Http.expectJson resultWrapper resultPageDecoder
        }
