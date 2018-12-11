module Skeleton exposing (Page, view)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)



-- MODEL


type alias Page msg =
    { title : String
    , body : List (Html msg)
    }



-- VIEW


view : String -> Html msg -> Page msg
view title body =
    buildPage title (template body)



-- INTERNAL


buildPage : String -> List (Html msg) -> Page msg
buildPage title body =
    { title = title
    , body = body
    }


template : Html msg -> List (Html msg)
template content =
    [ div [ class "container" ]
        [ navbar
        , header
        , content
        , footer
        ]
    ]


navbar : Html msg
navbar =
    -- the model is currently ignored! (we neither hold state nor provide functionality)
    nav [ class "navbar", class "navbar-expand-lg" ]
        [ a [ class "navbar-brand", href "/" ] [ text "HEMIN" ]
        , ul [ class "navbar-nav" ]
            [ li [ class "nav-item" ]
                [ a [ class "nav-link", href "/search" ] [ text "search" ]
                ]
            , li [ class "nav-item" ]
                [ a [ class "nav-link", href "/discover" ] [ text "discover" ]
                ]
            , li [ class "nav-item" ]
                [ a [ class "nav-link", href "/propose" ] [ text "+feed" ]
                ]
            ]
        ]


header : Html msg
header =
    div []
        [ p [] [ text "Header" ]
        , ul []
            [ viewLink "/p/abc"
            , viewLink "/e/abc"
            , viewLink "/discover"
            , viewLink "/search?q=abc&p=1&s=1"
            ]
        ]


footer : Html msg
footer =
    div []
        [ p [] [ text "Footer" ]
        ]


viewLink : String -> Html msg
viewLink path =
    li []
        [ a [ href path ] [ text path ]
        ]
