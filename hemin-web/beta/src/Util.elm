module Util exposing (emptyHtml, maybeAsString, maybeAsText, maybePageNumberParam, maybePageSizeParam, maybeQueryParam)

import Html exposing (Html, text)


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
