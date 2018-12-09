module Route exposing (Route(..), fromUrl, href, replaceUrl)

import Article.Slug as Slug exposing (Slug)
import Browser.Navigation as Nav
import Html exposing (Attribute)
import Html.Attributes as Attr
import Profile exposing (Profile)
import Url exposing (Url)
import Url.Parser as Parser exposing ((</>), Parser, oneOf, s, string)
import Username exposing (Username)

import Podcast exposing (Podcast)
import Episode exposing (Episode)
import Search


-- ROUTING


type Route
    = HomePage
    | RootPage
    | PodcastDetailPage Podcast
    | EpisodeDetailPage Episode
    | SearchPage String Int Int


parser : Parser (Route -> a) a
parser =
    oneOf
        [ Parser.map HomePage Parser.top
        , Parser.map PodcastDetailPage (s "p" </> string)
        , Parser.map EpisodeDetailPage (s "e" </> string)
        , Parser.map SearchPage (s "search" <?> Query.string "q" <?> Query.int "p" <?> Query.int "s")
        ]



-- PUBLIC HELPERS


href : Route -> Attribute msg
href targetRoute =
    Attr.href (routeToString targetRoute)


replaceUrl : Nav.Key -> Route -> Cmd msg
replaceUrl key route =
    Nav.replaceUrl key (routeToString route)


fromUrl : Url -> Maybe Route
fromUrl url =
    -- The RealWorld spec treats the fragment like a path.
    -- This makes it *literally* the path, so we can proceed
    -- with parsing as if it had been a normal path all along.
    { url | path = Maybe.withDefault "" url.fragment, fragment = Nothing }
        |> Parser.parse parser



-- INTERNAL


routeToString : Route -> String
routeToString page =
    let
        pieces =
            case page of
                HomePage ->
                    []

                RootPage ->
                    []

                PodcastDetailPage ->
                    [ "p", Podcast.id ]

                EpisodeDetailPage ->
                    [ "e", Episode.id ]

                SearchPage ->
                    [ "search" ] -- TODO add q/p/s as ? params (not in array, or they'll get a / )
    in
        String.join "/" pieces