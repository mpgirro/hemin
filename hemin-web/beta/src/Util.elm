module Util exposing (emptyHtml, maybeAsString, maybeAsText, maybePageNumberParam, maybePageSizeParam, maybeQueryParam, prettyDateHtml, prettyDateString, viewInnerHtml)

import DateFormat
import Html exposing (Html, div, span, text)
import Html.Attributes exposing (class, property)
import Iso8601
import Json.Encode
import Time exposing (Posix, Zone)


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


prettyDateString : String -> Maybe String
prettyDateString timestamp =
    let
        formatter : Zone -> Posix -> String
        formatter =
            DateFormat.format
                [ DateFormat.monthNameFull
                , DateFormat.text " "
                , DateFormat.dayOfMonthSuffix
                , DateFormat.text ", "
                , DateFormat.yearNumber
                ]

        timezone : Zone
        timezone =
            Time.utc
    in
    case Iso8601.toTime timestamp of
        Ok posix ->
            Just (formatter timezone posix)

        Err _ ->
            Nothing


prettyDateHtml : String -> Html msg
prettyDateHtml timestamp =
    case prettyDateString timestamp of
        Just pretty ->
            text pretty

        Nothing ->
            span [ class "text-red" ] [ text "DATE_PARSING_ERROR" ]
