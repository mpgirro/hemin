module Skeleton exposing (Page, siteName, view, viewHttpFailure, viewLink, viewLoadingPage)

import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Util exposing (maybeAsString, maybeAsText, emptyHtml)


-- MODEL


type alias Page msg =
    { title : String
    , body : List (Html msg)
    }



-- VIEW


view : String -> Html msg -> Page msg
view title body =
    buildPage title (template body)

viewLink : Maybe String -> Html msg
viewLink externalLink =
    case externalLink of
        Just link ->
            let
                stripProtocol =
                    if String.startsWith "https://" link then
                        String.dropLeft 8 link
                    else if String.startsWith "http://" link then
                        String.dropLeft 7 link
                    else
                        link
            in
            a [ href link, class "website-link", class "text-gray" ] [ text stripProtocol ]

        Nothing ->
            emptyHtml



viewHttpFailure : Http.Error -> Html msg
viewHttpFailure cause =
    case cause of
        Http.BadUrl msg ->
            text ("Unable to load the data; reason: " ++ msg)

        Http.Timeout ->
            text "Unable to load the data; reason: timeout"

        Http.NetworkError ->
            text "Unable to load the data; reason: network error"

        Http.BadStatus status ->
            text ("Unable to load the data; reason: status " ++ String.fromInt status)

        Http.BadBody msg ->
            text ("Unable to load the data; reason: " ++ msg)


viewLoadingPage : Page msg
viewLoadingPage =
    let
        body =
            div [] [ p [] [ text "Loading..." ] ]
    in
    view "Loading" body



--- CONSTANTS ---


siteName : String
siteName =
    "HEMIN"



-- INTERNAL


buildPage : String -> List (Html msg) -> Page msg
buildPage title body =
    { title = title
    , body = body
    }


template : Html msg -> List (Html msg)
template content =
    [ div [ class "hemin", class "container-md" ]
        [ navbar
        , content
        , footer
        ]
    ]


navbar : Html msg
navbar =
    nav [ class "UnderlineNav", class "mb-4" ]
        [ div [ class "UnderlineNav-actions" ]
            [ a [ href "/" ]
                [ img [ src "/logo.svg", width 16, height 16, alt "" ] []
                , div [ class "hemin-brand", class "d-inline-block mx-1" ]
                    [ text siteName ]
                ]
            ]
        , div [ class "UnderlineNav-body" ]
            [ a [ class "UnderlineNav-item", href "/search" ] [ text "search" ]
            , a [ class "UnderlineNav-item", href "/discover" ] [ text "discover" ]
            , a [ class "UnderlineNav-item", href "/propose" ] [ text "+feed" ]
            ]
        ]

footer : Html msg
footer =
    div []
        [ p [] [ text "Footer" ]
        ]
