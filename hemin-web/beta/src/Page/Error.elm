module Page.Error exposing (Model(..), Msg(..), update, view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Http
import Skeleton exposing (Page)
import Util exposing (emptyHtml)



-- MODEL


type Model
    = GenericFailure
    | HttpFailure Http.Error


type Msg
    = HttpFailed Http.Error


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        HttpFailed error ->
            ( HttpFailure error, Cmd.none )


view : Model -> Html msg
view model =
    case model of
        GenericFailure ->
            viewGenericFailure

        HttpFailure error ->
            viewHttpFailure error


viewGenericFailure : Html msg
viewGenericFailure =
    viewErrorExplanation "Error" "reason: no details about the error's nature is available" Nothing


viewHttpFailure : Http.Error -> Html msg
viewHttpFailure error =
    case error of
        Http.BadUrl msg ->
            viewErrorExplanation "Unable to load data" msg Nothing

        Http.Timeout ->
            viewErrorExplanation "Unable to load data" "timeout" Nothing

        Http.NetworkError ->
            viewErrorExplanation "Unable to load data" "network error" Nothing

        Http.BadStatus status ->
            viewErrorExplanation "Unable to load data" ("HTTP status " ++ String.fromInt status) Nothing

        Http.BadBody msg ->
            viewErrorExplanation "Unable to load data" "bad body" (Just msg)


viewErrorExplanation : String -> String -> Maybe String -> Html msg
viewErrorExplanation info reason body =
    div []
        [ h1 [ class "f1-light", class "mb-3" ] [ text "Error" ]
        , viewErrorInfo info
        , viewErrorReason reason
        , viewErrorBody body
        ]


viewErrorInfo : String -> Html msg
viewErrorInfo info =
    p [] [ text info ]


viewErrorReason : String -> Html msg
viewErrorReason reason =
    p []
        [ text "Reason: "
        , b [] [ text reason ]
        ]


viewErrorBody : Maybe String -> Html msg
viewErrorBody body =
    case body of
        Just txt ->
            div []
                [ p [] [ text "We received this body to the failure response:" ]
                , pre [] [ text txt ]
                ]

        Nothing ->
            emptyHtml
