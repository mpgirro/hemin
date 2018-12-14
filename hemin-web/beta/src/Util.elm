module Util exposing (maybeAsString, maybeAsText)

import Html exposing (Html, text)

maybeAsText : Maybe String -> Html msg
maybeAsText maybe =
    case maybe of
        Just txt ->
            text txt

        Nothing ->
            text ""


maybeAsString : Maybe String -> String
maybeAsString maybe =
    case maybe of
        Just txt ->
            txt

        Nothing ->
            ""
