module Data.ResultPage exposing (ResultPage, resultPageDecoder)

import Data.IndexDoc exposing (IndexDoc, indexDocDecoder)
import Json.Decode exposing (Decoder, bool, field, int, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)



--- MODELS ---


type alias ResultPage =
    { currPage : Int
    , maxPage : Int
    , totalHits : Int
    , results : List IndexDoc
    }



--- DEFAULTS ---


emptyResultPage : ResultPage
emptyResultPage =
    { currPage = 0
    , maxPage = 0
    , totalHits = 0
    , results = []
    }



--- JSON ---


resultPageDecoder : Decoder ResultPage
resultPageDecoder =
    Json.Decode.succeed ResultPage
        |> required "currPage" int
        |> required "maxPage" int
        |> required "totalHits" int
        |> required "results" (list indexDocDecoder)
