module SearchResult exposing (IndexDoc, ResultPage, emptyResultPage, indexDocDecoder, resultPageDecoder)

import Json.Decode exposing (Decoder, bool, field, int, list, string)
import Json.Decode.Pipeline exposing (hardcoded, optional, required)



-- TYPES


type alias IndexDoc =
    { docType : String
    , id : String
    , title : String
    , link : String
    , description : String
    , pubDate : String
    , image : String
    , itunesAuthor : String
    , itunesSummary : String
    , podcastTitle : String
    }


type alias ResultPage =
    { currPage : Int
    , maxPage : Int
    , totalHits : Int
    , results : List IndexDoc
    }



-- DEFAULTS


emptyResultPage : ResultPage
emptyResultPage =
    { currPage = 0
    , maxPage = 0
    , totalHits = 0
    , results = []
    }


indexDocDecoder : Decoder IndexDoc
indexDocDecoder =
    Json.Decode.succeed IndexDoc
        |> required "docType" string
        |> required "id" string
        |> optional "title" string ""
        |> optional "description" string ""
        |> optional "pubDate" string ""
        |> optional "pubDate" string ""
        |> optional "image" string ""
        |> optional "itunesAuthor" string ""
        |> optional "itunesSummary" string ""
        |> optional "podcastTitle" string ""


resultPageDecoder : Decoder ResultPage
resultPageDecoder =
    Json.Decode.succeed ResultPage
        |> required "currPage" int
        |> required "maxPage" int
        |> required "totalHits" int
        |> required "results" (list indexDocDecoder)
