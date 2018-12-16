module Util exposing (emptyHtml, maybeAsString, maybeAsText)

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


emptyHtml : Html msg
emptyHtml =
    text ""
