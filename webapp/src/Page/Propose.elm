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
import Html exposing (Html, button, div, form, h1, h2, h3, h4, h5, h6, hr, input, label, p, span, text)
import Html.Attributes exposing (autocomplete, class, disabled, for, id, multiple, placeholder, spellcheck, type_, value)
import Html.Attributes.Aria exposing (ariaDescribedby, ariaLabel)
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
    | OpmlFile File


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
    | OpmlSelectRequested
    | OpmlSelected File
    | OpmlUploadRequested
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

        OpmlSelectRequested ->
            ( model, Select.file [ "application/xml", "text/xml", "text/x-opml" ] OpmlSelected )

        OpmlSelected file ->
            ( OpmlFile file, Cmd.none )

        OpmlUploadRequested ->
            case model of
                OpmlFile file ->
                    ( model, Task.perform OpmlTransformed (File.toString file) )

                _ ->
                    ( model, Cmd.none )

        -- TODO from here, we want to produce a message that uploads the XML! (POST request)
        OpmlTransformed xml ->
            --    ( model, Cmd.none )
            ( model, RestApi.uploadOpml OpmlUploaded xml )

        --OpmlUploadProgress progress ->
        --    ( model, Cmd.none )
        OpmlUploaded response ->
            case response of
                Ok _ ->
                    ( Success "", Cmd.none )

                Err cause ->
                    ( Failure cause, Cmd.none )


view : Model -> ( String, Html Msg )
view model =
    let
        title =
            "Add Feeds"

        body : Html Msg
        body =
            case model of
                FeedUrl url ->
                    div []
                        [ viewFeedUrlForm url
                        , hr [] []
                        , viewOpmlFileForm ""
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
                        , viewOpmlFileForm ""

                        --, p [ class "mt-2" ] [ text "Proposing..." ]
                        ]

                OpmlFile file ->
                    div []
                        [ viewFeedUrlForm ""
                        , hr [] []
                        , viewOpmlFileForm (File.name file)
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
                        , viewOpmlFileForm ""

                        --, p [ class "mt-2" ] [ text "Feed successfully proposed. HEMIN will process it shortly." ]
                        ]

                Failure error ->
                    ErrorPage.viewHttpFailure error
    in
    ( title, body )


viewFeedUrlForm : String -> Html Msg
viewFeedUrlForm url =
    div []
        [ div [ class "subhead" ]
            [ h2 [ class "subhead-heading" ]
                [ text "Feed URL" ]
            , p [ class "subhead-description" ]
                [ text "Please submit the URL to the feed of the podcast that you want to add to "
                , text Const.siteName
                , text ":"
                ]
            ]
        , Html.form
            [ onSubmit FeedUrlPropose ]
            [ div [ class "input-group" ]
                [ viewFeedUrlInput url
                , div [ class "input-group-append" ]
                    [ viewFeedUrlSubmitButton
                    ]
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
    button
        [ class "btn"
        , class "btn-outline-primary"
        , type_ "button"
        , ariaLabel "Submit"
        , onClick FeedUrlPropose
        ]
        [ text "Submit" ]


viewOpmlFileForm : String -> Html Msg
viewOpmlFileForm fileName =
    div []
        [ p []
            [ text "Upload an OPML file to add podcasts"
            ]
        , Html.form
            []
            [ div [ class "input-group" ]
                [ viewOpmlFileInput fileName
                , div [ class "input-group-append" ]
                    [ viewOpmlSelectButton
                    , viewOpmlUploadButton
                    ]
                ]
            ]
        ]


viewOpmlFileInput : String -> Html Msg
viewOpmlFileInput fileName =
    input
        [ class "form-control"
        , type_ "text"
        , value fileName
        , autocomplete False
        , spellcheck False
        , disabled True
        ]
        []


viewOpmlSelectButton : Html Msg
viewOpmlSelectButton =
    button
        [ class "btn"
        , class "btn-outline-primary"
        , type_ "button"
        , ariaLabel "Select"
        , onClick OpmlSelectRequested
        ]
        [ text "Select" ]


viewOpmlUploadButton : Html Msg
viewOpmlUploadButton =
    button
        [ class "btn"
        , class "btn-outline-primary"
        , type_ "button"
        , ariaLabel "Upload"
        , onClick OpmlUploadRequested
        ]
        [ text "Upload" ]



--- HTTP ---


proposeFeed : String -> Cmd Msg
proposeFeed feed =
    RestApi.proposeFeed FeedUrlProposed feed
