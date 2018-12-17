module Page.Podcast exposing (Model(..), Msg(..), getPodcast, init, update, view)

import Browser
import Data.Podcast exposing (Podcast)
import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import RestApi
import Skeleton exposing (Page, viewHttpFailure)
import Util exposing (emptyHtml, maybeAsString, maybeAsText)



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
        [ viewCoverImage podcast
        , viewTitle podcast
        , viewLink podcast
        , viewDecription podcast
        , viewCategories podcast
        ]


viewCoverImage : Podcast -> Html msg
viewCoverImage podcast =
    case podcast.image of
        Just image ->
            div []
                [ img
                    [ src image
                    , alt "cover image"
                    , class "width-full"
                    ]
                    [] ]

        Nothing ->
            emptyHtml


viewTitle : Podcast -> Html msg
viewTitle podcast =
    case podcast.title of
        Just title ->
            h1 [ class "mt-4", class "mb-0" ] [ maybeAsText podcast.title ]

        Nothing ->
            emptyHtml


viewLink : Podcast -> Html msg
viewLink podcast =
    case podcast.link of
        Just link ->
            a [ href link ] [ text link ]

        Nothing ->
            emptyHtml


viewDecription : Podcast -> Html msg
viewDecription podcast =
    case podcast.itunes.summary of
        Just summary ->
            viewDescriptionParagraph summary

        Nothing ->
            case podcast.description of
                Just description ->
                    viewDescriptionParagraph description

                Nothing ->
                    emptyHtml


viewDescriptionParagraph : String -> Html msg
viewDescriptionParagraph description =
    p [ class "mt-4" ] [ text description ]


viewCategories : Podcast -> Html msg
viewCategories podcast =
    div [ class "mt-3" ] <|
        List.map viewCategory podcast.itunes.categories


viewCategory : String -> Html msg
viewCategory category =
    -- TODO add the OpenIconic stuff
    span [ class "Label", class "Label--gray", class "p-1", class "mr-1" ]
        [ span [ class "oi", class "oi-tag" ] []
        , text category
        ]



-- HTTP


getPodcast : String -> Cmd Msg
getPodcast id =
    RestApi.getPodcast LoadedPodcast id
