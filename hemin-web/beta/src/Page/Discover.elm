module Page.Discover exposing (Model(..), Msg(..), getAllPodcast, update, view)

import Data.Podcast exposing (Podcast)
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import RestApi
import Skeleton exposing (Page)
import Util exposing (maybeAsString, maybeAsText)



---- MODEL ----


type Model
    = Failure Http.Error
    | Loading
    | Content (List Podcast)



---- UPDATE ----


type Msg
    = LoadDiscover Int Int
    | LoadedDiscover (Result Http.Error (List Podcast))


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        LoadDiscover pageNumber pageSize ->
            ( model, getAllPodcast pageNumber pageSize )

        LoadedDiscover result ->
            case result of
                Ok podcasts ->
                    ( Content podcasts, Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )



---- VIEW ----


view : Model -> Html msg
view model =
    case model of
        Failure cause ->
            Skeleton.viewHttpFailure cause

        Loading ->
            text "Loading..."

        Content podcasts ->
            viewDiscover podcasts


viewDiscover : List Podcast -> Html msg
viewDiscover podcasts =
    div []
        [ div [ class "Subhead" ]
            [ div [ class "Subhead-heading" ]
                [ text "Discover Podcasts" ]
            , div [ class "Subhead-description" ]
                [ text "Discover new podcasts in this grid of cover images" ]
            ]
        , ul [ class "list-style-none" ] <|
            List.map viewPodcastCover podcasts
        ]


viewPodcastCover : Podcast -> Html msg
viewPodcastCover podcast =
    li [ class "d-inline-block", class "col-2", class "p-2" ]

        [ a [ href ("/p/" ++ podcast.id) ]
          [ img
                      [ class "width-full"
                      , class "avatar"
                      , src (maybeAsString podcast.image)
                      , alt (maybeAsString podcast.title)
                      ] []
          ]
        ]



--- HTTP ---


getAllPodcast : Int -> Int -> Cmd Msg
getAllPodcast pageNumber pageSize =
    RestApi.getAllPodcasts LoadedDiscover pageNumber pageSize
