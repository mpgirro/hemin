module App exposing (Model, Msg(..), Page, buildPage, footer, header, init, main, subscriptions, template, update, view, viewEpisodePage, viewHomePage, viewLink, viewNotFound, viewPodcastPage, viewSearchPage)

import Browser
import Browser.Navigation
import Episode
import EpisodePage
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Podcast
import PodcastPage
import Router exposing (..)
import SearchPage
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
  = StartContent
  | PodcastContent PodcastPage.Model
  | EpisodeContent EpisodePage.Model


init : () -> Url.Url -> Browser.Navigation.Key -> ( Model, Cmd Msg )
init flags url key =
    ( Model key url Router.NotFound StartContent, Cmd.none )



-- UPDATE


type Msg
    = LinkClicked Browser.UrlRequest
    | UrlChanged Url.Url
    | PodcastMsg PodcastPage.Msg
    | EpisodeMsg EpisodePage.Msg
--    | SearchMsg SearchPage.Msg


update : Msg -> Model -> ( Model, Cmd Msg )
update message model =
    case message of
        LinkClicked urlRequest ->
            case urlRequest of
                Browser.Internal url ->
                    ( model, Browser.Navigation.pushUrl model.key (Url.toString url) )

                Browser.External href ->
                    ( model, Browser.Navigation.load href )

        UrlChanged url ->
            ( { model | route = fromUrl url }, Cmd.none )

        PodcastMsg msg ->
          case model.content of
            PodcastContent content -> stepPodcast model (PodcastPage.update msg content)
            _                      -> ( model, Cmd.none )

        EpisodeMsg msg ->
          case model.content of
            EpisodeContent content -> stepEpisode model (EpisodePage.update msg content)
            _                      -> ( model, Cmd.none )


stepPodcast : Model -> ( PodcastPage.Model, Cmd PodcastPage.Msg ) -> ( Model, Cmd Msg )
stepPodcast model (content, cmd) =
  ( { model | content = PodcastContent content }, Cmd.map PodcastMsg cmd )


stepEpisode : Model -> ( EpisodePage.Model, Cmd EpisodePage.Msg ) -> ( Model, Cmd Msg )
stepEpisode model (content, cmd) =
  ( { model | content = EpisodeContent content }, Cmd.map EpisodeMsg cmd )


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

        Router.PodcastPage _ ->
            viewPodcastPage model

        Router.EpisodePage _ ->
            viewEpisodePage model

        Router.SearchPage query pageNumber pageSize ->
            viewSearchPage model




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

viewNotFound : Model -> Page msg
viewNotFound model =
    buildPage "Not Found"
        (template
            (div []
                [ p [] [ text "Not Found" ] ]
            )
        )


viewHomePage : Model -> Page msg
viewHomePage model =
    buildPage "Home Page"
        (template
            (div []
                [ p [] [ text "Homepage" ] ]
            )
        )

viewPodcastPage : Model -> Page msg
viewPodcastPage model =
  case model.content of
    PodcastContent content ->
      let 
        body = PodcastPage.view content
      in 
        buildPage "Podcast" (template body)

    _ ->
      viewNotFound model


viewEpisodePage : Model -> Page msg
viewEpisodePage model =
  case model.content of
    EpisodeContent content ->
      buildPage "Episode" (template (EpisodePage.view content) )

    _ ->
      viewNotFound model


viewSearchPage : Model -> Page msg
viewSearchPage model =
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

viewHttpFailure : Http.Error -> Html Msg
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