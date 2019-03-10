module Data.Image exposing (Image, imageDecoder)

import Json.Decode exposing (Decoder, int, maybe, string)
import Json.Decode.Pipeline exposing (optional, required)
import Time exposing (Posix)
import Util exposing (decodePosix)


type alias Image =
    { id : String
    , url : Maybe String
    , data : Maybe String
    , hash : Maybe String
    , name : Maybe String
    , contentType : Maybe String
    , size : Maybe Int
    , createdAt : Maybe Posix
    }


imageDecoder : Decoder Image
imageDecoder =
    Json.Decode.succeed Image
        |> required "id" string
        |> required "url" (maybe string) Nothing
        |> optional "data" (maybe string) Nothing
        |> optional "hash" (maybe string) Nothing
        |> optional "name" (maybe string) Nothing
        |> optional "contentType" (maybe string) Nothing
        |> optional "size" (maybe int) Nothing
        |> optional "createdAt" (maybe decodePosix) Nothing
