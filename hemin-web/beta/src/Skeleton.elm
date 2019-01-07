module Skeleton exposing (Page, view, viewLink)

import Const
import FeatherIcons
import Html exposing (Html, a, b, div, img, li, nav, p, span, text, ul)
import Html.Attributes exposing (alt, class, height, href, src, width)
import Http
import Util exposing (emptyHtml, maybeAsString, maybeAsText)



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
            a [ href link, class "link-gray", class "font-quicksand" ] [ text stripProtocol ]

        Nothing ->
            emptyHtml



-- INTERNAL


buildPage : String -> List (Html msg) -> Page msg
buildPage title body =
    { title = title
    , body = body
    }


template : Html msg -> List (Html msg)
template content =
    [ div [ class "hemin", class "container-md" ]
        [ div
            [ class "px-2" ]
            [ navbar
            , content
            , footer
            ]
        ]
    ]


navbar : Html msg
navbar =
    nav
        [ class "UnderlineNav"
        , class "mb-4"
        ]
        [ div [ class "UnderlineNav-actions" ]
            [ a [ href "/" ]
                [ img [ src "/logo.svg", width 12, height 12, alt "" ] []
                , div [ class "hemin-brand", class "d-inline-block mx-1" ]
                    [ text Const.siteName ]
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
    div
        [ class "footer"
        , class "clearfix"

        --, class "position-absolute"
        ]
        [ footerLeft
        , footerRight
        ]


footerLeft : Html msg
footerLeft =
    div
        [ class "col-9"
        , class "float-left"
        ]
        [ p [ class "mt-5" ] [ b [] [ text ("About " ++ Const.siteName) ] ]
        , p [ class "note" ]
            [ text Const.siteName
            , text " is an open source podcast catalog & search engine. Check out the code on "
            , a [ href "https://github.com/mpgirro/hemin" ] [ text "GitHub" ]
            , text ". This site is written in "
            , a [ href "https://elm-lang.org" ] [ text "Elm" ]
            , text ". The podcasts and artworks embedded on this page are properties of their owners, and all audio is streamed directly from their servers."
            ]
        ]


footerRight : Html msg
footerRight =
    div
        [ class "col-2"
        , class "float-right"
        ]
        [ p [ class "mt-5" ] [ b [] [ text "Links" ] ]
        , p [ class "note" ]
            [ ul
                [ class "list-style-none" ]
                [ li [] [ a [ href "https://blog.hemin.io" ] [ text "Blog" ] ]
                , li [] [ a [ href "https://github.com/mpgirro/hemin/blob/master/CHANGELOG.md" ] [ text "Changelog" ] ]
                , li [] [ a [ href "https://twitter.com/hemin_io" ] [ text "Twitter" ] ]
                ]
            ]
        ]
