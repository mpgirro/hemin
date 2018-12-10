module App exposing (..)

import Browser
import Browser.Navigation
import Html exposing (..)
import Html.Attributes exposing (..)
import Html
import Url

import Router exposing (..)
import Podcast
import Episode

import PodcastPage
import EpisodePage
import SearchPage

-- MAIN

main : Program () Model Msg
main =
  Browser.application
    { init = init
    , view = view
    , update = update
    , subscriptions = subscriptions
    , onUrlRequest = LinkClicked
    , onUrlChange = UrlChanged
    }



-- MODEL


type alias Model =
    { key : Browser.Navigation.Key
    , url : Url.Url
    , route : Route
    }

init : () -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init flags url key =
    ( Model key url Router.NotFound, Cmd.none )

-- UPDATE



type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url



update : Msg -> Model -> ( Model, Cmd Msg )
update msg model = 
    case msg of
        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( model, Browser.Navigation.pushUrl model.key (Url.toString url) )

                Browser.External href ->
                    ( model, Browser.Navigation.load href )
        
        UrlChanged url -> 
            ( { model | route = fromUrl url }, Cmd.none )



-- SUBSCRIPTIONS



subscriptions : Model -> Sub Msg
subscriptions _ = 
    Sub.none



-- VIEW



view : Model -> Browser.Document Msg
view model = 
    case model.route of
        Router.NotFound -> 
            viewNotFound model
        Router.HomePage -> 
            viewHomePage model
        Router.RootPage -> 
            viewHomePage model
        Router.PodcastPage id ->
            viewPodcastPage id
        Router.EpisodePage id -> 
            viewEpisodePage id
        Router.SearchPage query pageNumber pageSize ->
          viewSearchPage query pageNumber pageSize
        



type alias Page msg = 
    { title: String
    , body: List (Html msg)
    }

buildPage : String -> List (Html msg) -> Page msg
buildPage title body =
    { title = title
    , body = body
    }


header : Html msg
header = div [] 
    [ p [] [ text "Header" ]
    , ul []
        [ viewLink "/p/abc"
        , viewLink "/e/abc"
        , viewLink "/discover" 
        , viewLink "/search?q=abc&p=1&s=1" 
        ]
    ]

footer : Html msg
footer = div [] 
    [ p [] [ text "Footer" ]
    ]

template : Html msg -> List (Html msg)
template content =
    [ header
    , content
    , footer]

viewNotFound : Model -> Page msg
viewNotFound model = 
    buildPage "Not Found"
        (template (div []
            [ p [] [ text "Not Found" ] ]))

viewHomePage : Model -> Page msg
viewHomePage model = 
    buildPage "Home Page"
        (template (div [] 
            [ p [] [ text "Homepage" ] ]))

viewPodcastPage : String -> Page msg
viewPodcastPage id =
    buildPage "Podcast"
        (template (div []
            [ p [] [ text "Podcast Page" ] ]))

viewEpisodePage : String -> Page msg
viewEpisodePage id =
    buildPage "Episode"
        (template (div []
            [ p [] [ text "Episode Page" ] ]))

viewSearchPage : Maybe String -> Maybe Int -> Maybe Int -> Page msg
viewSearchPage query pageNumber pageSize =
  buildPage "Search"
        (template (div []
            [ p [] [ text "Search Results Page" ] ]))

viewLink : String -> Html msg
viewLink path =
    li [] 
        [ a [ href path ] [ text path ] 
    ]