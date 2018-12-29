module Util exposing
    ( decodePosix
    , emptyHtml
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
import Json.Decode exposing (Decoder, int, maybe)
import Json.Encode
import Time exposing (Month, Posix, millisToPosix, toDay, toMonth, toYear)


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
            Just ("q=" ++ q)

        Nothing ->
            Nothing


maybePageNumberParam : Maybe Int -> Maybe String
maybePageNumberParam pageNumber =
    case pageNumber of
        Just page ->
            Just ("p=" ++ String.fromInt page)

        Nothing ->
            Nothing


maybePageSizeParam : Maybe Int -> Maybe String
maybePageSizeParam pageSize =
    case pageSize of
        Just size ->
            Just ("s=" ++ String.fromInt size)

        Nothing ->
            Nothing


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
