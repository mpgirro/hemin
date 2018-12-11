module App exposing (Model, Msg(..), Page, buildPage, footer, header, init, main, subscriptions, template, update, view, viewEpisodePage, viewHomePage, viewLink, viewNotFound, viewPodcastPage, viewResultPage)

import Browser
import Browser.Navigation
import Episode exposing (Episode, episodeDecoder)
import EpisodePage
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Podcast exposing (Podcast, podcastDecoder)
import PodcastPage
import Router exposing (..)
import SearchPage
import SearchResult exposing (ResultPage, resultPageDecoder)
import Url



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
    , content : Content
    }

type Content 
  = Failure Http.Error
  | Loading
  | NotFound
  | StartContent
  | PodcastContent Podcast
  | EpisodeContent Episode
  | SearchResultContent ResultPage


init : () -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init flags url key =
    ( Model key url Router.NotFound StartContent, Cmd.none )



-- UPDATE

type Msg 
    = NoOp
    | LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | LoadPodcast String
    | LoadedPodcast (Result Http.Error Podcast)
    | LoadEpisode String
    | LoadedEpisode (Result Http.Error Episode)
    | LoadResultPage (Maybe String) (Maybe Int) (Maybe Int)
    | LoadedResultPage (Result Http.Error ResultPage)


update : Msg -> Model -> ( Model, Cmd Msg )
update message model =
    case message of
        NoOp ->
            ( model, Cmd.none )

        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( model, Browser.Navigation.pushUrl model.key (Url.toString url) )

                Browser.External href ->
                    ( model, Browser.Navigation.load href )

        UrlChanged url ->
            let 
                route = fromUrl url
            in 
                ( { model | route = route }, Cmd.none ) -- (cmdFromRoute route)

        LoadPodcast id ->
          ( model, getPodcast id )

        LoadedPodcast result ->
            case result of
                Ok podcast ->
                    ( { model | content = PodcastContent podcast }, Cmd.none )

                Err cause ->
                    ( { model | content = Failure cause }, Cmd.none ) -- TODO outsource to utility func

        LoadEpisode id ->
            ( model, getEpisode id )

        LoadedEpisode result ->
            case result of
                Ok episode ->
                    ( { model | content = EpisodeContent episode }, Cmd.none )

                Err cause ->
                    ( { model | content = Failure cause }, Cmd.none ) -- TODO outsource to utility func

        LoadResultPage query pageNumber pageSize ->
            ( model, getSearchResults query pageNumber pageSize )

        LoadedResultPage result ->
            case result of
                Ok resultPage ->
                    ( { model | content = SearchResultContent resultPage }, Cmd.none )

                Err cause ->
                    ( { model | content = Failure cause }, Cmd.none ) -- TODO outsource to utility func

-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none


-- VIEW


view : Model -> Browser.Document Msg
view model =
    case model.content of
        Failure cause ->
            viewHttpFailurePage cause
            
        Loading ->
            viewLoadingPage

        NotFound ->
            viewNotFound

        StartContent ->
            viewHomePage

        PodcastContent podcast ->
            viewPodcastPage podcast

        EpisodeContent episode ->
            viewEpisodePage episode

        SearchResultContent resultPage ->
            viewResultPage resultPage


type alias Page msg =
    { title : String
    , body : List (Html msg)
    }


buildPage : String -> List (Html msg) -> Page msg
buildPage title body =
    { title = title
    , body = body
    }


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


template : Html msg -> List (Html msg)
template content =
    [ header
    , content
    , footer
    ]

viewLoadingPage : Page msg
viewLoadingPage =
    buildPage "Loading"
        (template
            (div []
                [ p [] [ text "Loading..." ] ]
            )
        )

viewNotFound : Page msg
viewNotFound =
    buildPage "Not Found"
        (template
            (div []
                [ p [] [ text "Not Found" ] ]
            )
        )


viewHomePage : Page msg
viewHomePage =
    buildPage "Home Page"
        (template
            (div []
                [ p [] [ text "Homepage" ] ]
            )
        )

viewPodcast : Podcast -> Html msg
viewPodcast podcast =
    div []
        [ h1 [] [ text podcast.title ]
        , a [ href podcast.link ] [ text podcast.link ]
        , p [] [ text podcast.description ]
        ]

viewPodcastPage : Podcast -> Page msg
viewPodcastPage podcast =
  let 
    body = viewPodcast podcast -- PodcastPage.view podcast
  in 
    buildPage "Podcast" (template body)

viewEpisode : Episode -> Html msg
viewEpisode episode =
    div []
        [ h1 [] [ text episode.title ]
        , a [ href episode.link ] [ text episode.link ]
        , p [] [ text episode.description ]
        ]

viewEpisodePage : Episode -> Page msg
viewEpisodePage episode =
  let 
    body = viewEpisode episode -- EpisodePage.view episode
  in 
    buildPage "Episode" (template body)

-- TODO replace with propper impl.
viewResultPage : ResultPage -> Page msg
viewResultPage resultPage =
    buildPage "Search"
        (template
            (div []
                [ p [] [ text "Search Results Page" ] ]
            )
        )

viewLink : String -> Html msg
viewLink path =
    li []
        [ a [ href path ] [ text path ]
        ]

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

viewHttpFailurePage : Http.Error -> Page msg
viewHttpFailurePage cause =
  let 
    body = viewHttpFailure cause 
  in 
    buildPage "Error" (template body)

-- UTILITIES

cmdFromRoute : Route -> Msg -- Cmd Msg
cmdFromRoute route =
    case route of
        Router.NotFound ->
            NoOp -- TODO 

        Router.HomePage ->
            NoOp  -- TODO 

        Router.RootPage ->
            NoOp  -- TODO 

        Router.PodcastPage id ->
            LoadPodcast id

        Router.EpisodePage id ->
            LoadEpisode id

        Router.SearchPage query pageNumber pageSize ->
            LoadResultPage query pageNumber pageSize


-- HTTP


getPodcast : String -> Cmd Msg
getPodcast id =
    -- TODO id is currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/podcast.json"
        , expect = Http.expectJson LoadedPodcast podcastDecoder
        }

getEpisode : String -> Cmd Msg
getEpisode id =
    -- TODO id is currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/episode.json" -- "http://localhost:9000/api/v1/episode/8DTKUxDwRO991"
        , expect = Http.expectJson LoadedEpisode episodeDecoder
        }

getSearchResults : (Maybe String) -> (Maybe Int) -> (Maybe Int) -> Cmd Msg
getSearchResults query pageNumber pageSize =
    -- TODO args are currently ignored
    Http.get
        { url = "https://api.hemin.io/json-examples/search.json"
        , expect = Http.expectJson LoadedResultPage resultPageDecoder
        }