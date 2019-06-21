module Util exposing
    ( buildSearchUrl
    , decodePosix
    , emptyHtml
    , encodeMaybeBool
    , encodeMaybeInt
    , encodeMaybeString
    , maybeAsString
    , maybeAsText
    , maybePageNumberParam
    , maybePageSizeParam
    , maybeQueryParam
    , prettyDateHtml
    , prettyDateString
    , viewInnerHtml
    )

import DateFormat
import Html exposing (Html, div, span, text)
import Html.Attributes exposing (class, property)
import Json.Decode exposing (Decoder, maybe)
import Json.Encode
import Maybe.Extra
import Time exposing (Month, Posix, millisToPosix, toDay, toMonth, toYear)


encodeMaybeString : Maybe String -> Json.Encode.Value
encodeMaybeString maybeString =
    case maybeString of
        Just s ->
            Json.Encode.string s

        Nothing ->
            Json.Encode.null


encodeMaybeBool : Maybe Bool -> Json.Encode.Value
encodeMaybeBool maybeBool =
    case maybeBool of
        Just b ->
            Json.Encode.bool b

        Nothing ->
            Json.Encode.null


encodeMaybeInt : Maybe Int -> Json.Encode.Value
encodeMaybeInt maybeInt =
    case maybeInt of
        Just i ->
            Json.Encode.int i

        Nothing ->
            Json.Encode.null


maybeAsText : Maybe String -> Html msg
maybeAsText maybe =
    case maybe of
        Just txt ->
            text txt

        Nothing ->
            emptyHtml


maybeAsString : Maybe String -> String
maybeAsString maybe =
    case maybe of
        Just txt ->
            txt

        Nothing ->
            ""


maybeQueryParam : Maybe String -> Maybe String
maybeQueryParam query =
    case query of
        Just q ->
            Just ("query=" ++ q)

        Nothing ->
            Nothing


maybePageNumberParam : Maybe Int -> Maybe String
maybePageNumberParam pageNumber =
    case pageNumber of
        Just page ->
            Just ("pageNumber=" ++ String.fromInt page)

        Nothing ->
            Nothing


maybePageSizeParam : Maybe Int -> Maybe String
maybePageSizeParam pageSize =
    case pageSize of
        Just size ->
            Just ("pageSize=" ++ String.fromInt size)

        Nothing ->
            Nothing


buildSearchUrl : Maybe String -> Maybe Int -> Maybe Int -> String
buildSearchUrl query pageNumber pageSize =
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
    (++) "/search" urlQuery


emptyHtml : Html msg
emptyHtml =
    text ""


viewInnerHtml : String -> Html msg
viewInnerHtml html =
    Html.node "rendered-html"
        [ property "content" (Json.Encode.string html) ]
        []


toMonthString : Month -> String
toMonthString month =
    case month of
        Time.Jan ->
            "01"

        Time.Feb ->
            "02"

        Time.Mar ->
            "03"

        Time.Apr ->
            "04"

        Time.May ->
            "05"

        Time.Jun ->
            "06"

        Time.Jul ->
            "07"

        Time.Aug ->
            "08"

        Time.Sep ->
            "09"

        Time.Oct ->
            "10"

        Time.Nov ->
            "11"

        Time.Dec ->
            "12"


prettyDateString : Posix -> String
prettyDateString posix =
    let
        year : String
        year =
            String.fromInt (toYear Time.utc posix)

        month : String
        month =
            toMonthString (toMonth Time.utc posix)

        day : String
        day =
            String.fromInt (toDay Time.utc posix)
    in
    year ++ "-" ++ month ++ "-" ++ day


prettyDateHtml : Posix -> Html msg
prettyDateHtml posix =
    let
        value : String
        value =
            prettyDateString posix
    in
    text value



-- span [ class "text-red" ] [ text "DATE_PARSING_ERROR" ]


decodePosix : Decoder Posix
decodePosix =
    Json.Decode.andThen
        (\value ->
            Json.Decode.succeed (millisToPosix value)
        )
        Json.Decode.int
