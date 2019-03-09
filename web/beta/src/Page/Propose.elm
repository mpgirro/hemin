module Page.Propose exposing
    ( Model
    , Msg
    , init
    , update
    , view
    )

import Const
import File exposing (File)
import File.Select as Select
import Html exposing (Html, button, div, form, hr, input, p, span, text)
import Html.Attributes exposing (autocomplete, class, multiple, placeholder, spellcheck, type_, value)
import Html.Attributes.Aria exposing (ariaLabel)
import Html.Events exposing (on, onClick, onInput, onSubmit)
import Http
import Page.Error as ErrorPage
import RestApi
import Skeleton exposing (Page)
import Task



-- MODEL


type Model
    = Failure Http.Error
    | FeedUrl String
    | Proposing String
    | Success String


init : ( Model, Cmd Msg )
init =
    let
        model : Model
        model =
            FeedUrl ""
    in
    ( model, Cmd.none )


type Msg
    = FeedUrlPropose
    | FeedUrlProposed (Result Http.Error ())
    | FeedUrlUpdated String
    | OpmlRequested
    | OpmlSelected File
    | OpmlTransformed String
      --    | OpmlUploadProgress Http.Progress
    | OpmlUploaded (Result Http.Error ())


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        FeedUrlUpdated feed ->
            ( FeedUrl feed, Cmd.none )

        FeedUrlPropose ->
            case model of
                FeedUrl feed ->
                    ( Proposing feed, proposeFeed feed )

                _ ->
                    -- TODO is this the error handling I want?
                    ( FeedUrl "", Cmd.none )

        FeedUrlProposed response ->
            case response of
                Ok _ ->
                    ( Success "", Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )

        OpmlRequested ->
            ( model, Select.file [ "application/xml", "text/xml", "text/x-opml" ] OpmlSelected )

        OpmlSelected file ->
            ( model, Task.perform OpmlTransformed (File.toString file) )

        -- TODO from here, we want to produce a message that uploads the XML! (POST request)
        OpmlTransformed xml ->
            --    ( model, Cmd.none )
            ( model, RestApi.uploadOpml OpmlUploaded xml )

        --OpmlUploadProgress progress ->
        --    ( model, Cmd.none )
        OpmlUploaded response ->
            ( model, Cmd.none )


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Search"

        body : Html Msg
        body =
            case model of
                FeedUrl url ->
                    div []
                        [ viewFeedUrlForm url
                        , hr [] []
                        , viewOpmlSelectInput
                        ]

                Proposing feed ->
                    div []
                        [ viewFeedUrlForm feed
                        , div [ class "Box", class "mt-3" ]
                            [ div
                                [ class "flash", class "flash-full", class "flash-warn" ]
                                [ text "Proposing..." ]
                            ]
                        , hr [] []
                        , viewOpmlSelectInput

                        --, p [ class "mt-2" ] [ text "Proposing..." ]
                        ]

                Success feed ->
                    div []
                        [ viewFeedUrlForm feed
                        , div [ class "Box", class "mt-3" ]
                            [ div
                                [ class "flash", class "flash-full", class "flash-success" ]
                                [ text "Feed successfully proposed. "
                                , text Const.siteName
                                , text " will process it shortly."
                                , text " If the feed is valid and not yet in our database, the podcast and episodes will be available soon."
                                ]
                            ]
                        , hr [] []
                        , viewOpmlSelectInput

                        --, p [ class "mt-2" ] [ text "Feed successfully proposed. HEMIN will process it shortly." ]
                        ]

                Failure error ->
                    ErrorPage.viewHttpFailure error
    in
    ( title, body )


viewFeedUrlForm : String -> Html Msg
viewFeedUrlForm url =
    div []
        [ p []
            [ text "Please submit the URL to the feed of the podcast that you want to add to "
            , text Const.siteName
            , text ":"
            ]
        , Html.form
            [ onSubmit FeedUrlPropose ]
            [ div [ class "input-group" ]
                [ viewFeedUrlInput url
                , viewFeedUrlSubmitButton
                ]
            ]
        ]


viewFeedUrlInput : String -> Html Msg
viewFeedUrlInput url =
    let
        placeholderValue =
            "Enter the feed to propose here"
    in
    input
        [ class "form-control"
        , class "input-block"
        , type_ "text"
        , value url
        , placeholder placeholderValue
        , autocomplete False
        , spellcheck False
        , ariaLabel placeholderValue
        , onInput FeedUrlUpdated
        ]
        []


viewFeedUrlSubmitButton : Html Msg
viewFeedUrlSubmitButton =
    span [ class "input-group-button" ]
        [ button
            [ class "btn"
            , class "text-normal"

            --, attribute "style" "padding: 0"
            , type_ "button"
            , ariaLabel "Submit"
            , onClick FeedUrlPropose
            ]
            [ text "Submit" ]
        ]


viewOpmlSelectInput : Html Msg
viewOpmlSelectInput =
    div []
        [ button
            [ onClick OpmlRequested ]
            [ text "Select" ]
        ]



--- HTTP ---


proposeFeed : String -> Cmd Msg
proposeFeed feed =
    RestApi.proposeFeed FeedUrlProposed feed
