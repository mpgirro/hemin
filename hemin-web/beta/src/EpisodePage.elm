module EpisodePage exposing (Model(..), Msg(..), getEpisode, init, update, view)

import Browser
import Episode exposing (Episode)
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import RestApi
import Skeleton exposing (Page, viewHttpFailure)



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
    | Content Episode


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getEpisode "" )



-- UPDATE


type Msg
    = LoadEpisode String
    | LoadedEpisode (Result Http.Error Episode)



--  | GotChapters (Result Http.Error String)


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadEpisode id ->
            ( model, getEpisode id )

        LoadedEpisode result ->
            case result of
                Ok episode ->
                    ( Content episode, Cmd.none )

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

        Content episode ->
            viewEpisode episode


viewEpisode : Episode -> Html msg
viewEpisode episode =
    div []
        [ h1 [] [ text episode.title ]
        , a [ href episode.link ] [ text episode.link ]
        , p [] [ text episode.description ]
        ]



-- HTTP


getEpisode : String -> Cmd Msg
getEpisode id =
    RestApi.getEpisode LoadedEpisode id
