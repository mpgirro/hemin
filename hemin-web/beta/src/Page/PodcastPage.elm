module Page.PodcastPage exposing (Model(..), Msg(..), getPodcast, init, update, view)

import Browser
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Data.Podcast exposing (Podcast)
import RestApi
import Skeleton exposing (Page, viewHttpFailure)
import Util exposing (maybeAsText, maybeAsString)

-- MAIN


main =
    Browser.element
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        }



-- MODEL


type Model
    = Failure Http.Error
    | Loading
    | Content Podcast


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getPodcast "" )


initWithId : String -> ( Model, Cmd Msg )
initWithId id =
    ( Loading, getPodcast id )



-- UPDATE


type Msg
    = LoadPodcast String
    | LoadedPodcast (Result Http.Error Podcast)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadPodcast id ->
            ( model, getPodcast id )

        LoadedPodcast result ->
            case result of
                Ok podcast ->
                    ( Content podcast, Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )



-- SUBSCRIPTIONS


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none



-- VIEW


view : Model -> Html msg
view model =
    case model of
        Failure cause ->
            Skeleton.viewHttpFailure cause

        Loading ->
            text "Loading..."

        Content podcast ->
            viewPodcast podcast


viewPodcast : Podcast -> Html msg
viewPodcast podcast =
    div [ class "col-sm-8", class "col-md-6", class "col-lg-6", class "p-2", class "mx-auto" ]
        [ h1 [] [ maybeAsText podcast.title ]
        , Skeleton.viewLink (maybeAsString podcast.link)
        , p [] [ maybeAsText podcast.description ]
        ]



-- HTTP


getPodcast : String -> Cmd Msg
getPodcast id =
    RestApi.getPodcast LoadedPodcast id
