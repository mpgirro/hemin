module RestApi exposing
    ( apiBase
    , getAllPodcasts
    , getEpisode
    , getEpisodesByPodcast
    , getFeedsByPodcast
    , getPodcast
    , getSearchResult
    , proposeFeed
    )

import Data.Episode exposing (Episode, episodeDecoder, episodeListDecoder)
import Data.Feed exposing (Feed, feedDecoder, feedListDecoder)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast, podcastDecoder, podcastListDecoder)
import Data.ResultPage exposing (ResultPage, resultPageDecoder)
import Http
import Maybe.Extra
import Util exposing (maybePageNumberParam, maybePageSizeParam, maybeQueryParam)


apiBase : String
apiBase =
    -- "https://api.hemin.io/api/v1"
    "http://localhost:9000/api/v1"


getEpisode : (Result Http.Error Episode -> msg) -> String -> Cmd msg
getEpisode resultWrapper id =
    Http.get
        { url = apiBase ++ "/episode/" ++ id
        , expect = Http.expectJson resultWrapper episodeDecoder
        }


getEpisodesByPodcast : (Result Http.Error (List Episode) -> msg) -> String -> Cmd msg
getEpisodesByPodcast resultWrapper id =
    Http.get
        { url = apiBase ++ "/podcast/" ++ id ++ "/episodes"
        , expect = Http.expectJson resultWrapper episodeListDecoder
        }


getFeedsByPodcast : (Result Http.Error (List Feed) -> msg) -> String -> Cmd msg
getFeedsByPodcast resultWrapper id =
    Http.get
        { url = apiBase ++ "/podcast/" ++ id ++ "/feeds"
        , expect = Http.expectJson resultWrapper feedListDecoder
        }


getPodcast : (Result Http.Error Podcast -> msg) -> String -> Cmd msg
getPodcast resultWrapper id =
    Http.get
        { url = apiBase ++ "/podcast/" ++ id
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
        { url = apiBase ++ "/podcast/teaser?" ++ p ++ "&" ++ s
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
    in
    Http.get
        { url = apiBase ++ "/search" ++ urlQuery
        , expect = Http.expectJson resultWrapper resultPageDecoder
        }


proposeFeed : (Result Http.Error () -> msg) -> String -> Cmd msg
proposeFeed resultWrapper feed =
    Http.post
        { url = apiBase ++ "/feed/propose"
        , body = Http.stringBody "text/plain" feed
        , expect = Http.expectWhatever resultWrapper
        }
