module Data.SearchResult exposing (SearchResult, searchResultDecoder)

import Data.IndexDoc exposing (IndexDoc, indexDocDecoder)
import Json.Decode exposing (Decoder, int, list)
import Json.Decode.Pipeline exposing (required)



--- MODELS ---


type alias SearchResult =
    { currPage : Int
    , maxPage : Int
    , totalHits : Int
    , results : List IndexDoc
    }



--- DEFAULTS ---


emptySearchResult : SearchResult
emptySearchResult =
    { currPage = 0
    , maxPage = 0
    , totalHits = 0
    , results = []
    }



--- JSON ---


searchResultDecoder : Decoder SearchResult
searchResultDecoder =
    Json.Decode.succeed SearchResult
        |> required "currPage" int
        |> required "maxPage" int
        |> required "totalHits" int
        |> required "results" (list indexDocDecoder)
