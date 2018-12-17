module Page.Episode exposing (Model(..), Msg(..), getEpisode, init, update, view)

import Browser
import Data.Episode exposing (Episode)
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import RestApi
import Skeleton exposing (Page)
import Util exposing (emptyHtml, maybeAsString, maybeAsText)



-- MAIN


main =
    Browser.element
        { init = init
        , update = update
        , subscriptions = subscriptions
        , view = view
        }



---- MODEL ----


type Model
    = Failure Http.Error
    | Loading
    | Content Episode


init : () -> ( Model, Cmd Msg )
init _ =
    ( Loading, getEpisode "" )



---- UPDATE ----


type Msg
    = LoadEpisode String
    | LoadedEpisode (Result Http.Error Episode)


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



---- VIEW ----


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
    div [ class "col-sm-8", class "col-md-6", class "col-lg-6", class "p-2", class "mx-auto" ]
        [ viewCoverImage episode
        , h1 [] [ maybeAsText episode.title ]
        , Skeleton.viewLink episode.link
        , p [] [ maybeAsText episode.description ]
        ]

viewCoverImage : Episode -> Html msg
viewCoverImage episode =
    case episode.image of
        Just image ->
            let
                altText = "cover image of " ++ (maybeAsString episode.title)
            in
            div [] [ img [ src image, alt altText, class "width-full" ] [] ]

        Nothing ->
            emptyHtml

--- HTTP ---


getEpisode : String -> Cmd Msg
getEpisode id =
    RestApi.getEpisode LoadedEpisode id
