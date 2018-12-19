module Router exposing (Route(..), fromUrl, parser, redirectByIndexDocType, redirectToEpisode, redirectToPodcast, redirectToParent)

import Browser.Navigation as Nav
import Data.Episode exposing (Episode)
import Data.IndexDoc exposing (IndexDoc)
import Data.Podcast exposing (Podcast)
import Html exposing (Attribute)
import Html.Attributes as Attr
import Url exposing (Url)
import Url.Parser as Parser exposing ((</>), (<?>), Parser, oneOf, s, string)
import Url.Parser.Query as Query



--- ROUTER ---


type Route
    = HomePage
    | PodcastPage String
    | EpisodePage String
    | SearchPage (Maybe String) (Maybe Int) (Maybe Int)
    | DiscoverPage
    | ProposePage


parser : Parser (Route -> a) a
parser =
    oneOf
        [ Parser.map HomePage Parser.top
        , Parser.map PodcastPage (s "p" </> string)
        , Parser.map EpisodePage (s "e" </> string)
        , Parser.map SearchPage (s "search" <?> Query.string "q" <?> Query.int "p" <?> Query.int "s")
        , Parser.map DiscoverPage (s "discover")
        , Parser.map ProposePage (s "propose")
        ]


fromUrl : Url.Url -> Route
fromUrl url =
    Maybe.withDefault HomePage (Parser.parse parser url)


--- PUBLIC HELPERS ---


redirectToEpisode : Episode -> String
redirectToEpisode episode =
    episodePagePrefix ++ episode.id


redirectToPodcast : Podcast -> String
redirectToPodcast podcast =
    podcastPagePrefix ++ podcast.id

redirectToParent : Episode -> String
redirectToParent episode =
    podcastPagePrefix ++ episode.podcastId


redirectByIndexDocType : IndexDoc -> String
redirectByIndexDocType doc =
    case doc.docType of
        "podcast" ->
            podcastPagePrefix ++ doc.id

        "episode" ->
            episodePagePrefix ++ doc.id

        _ ->
            ""

--- INTERNAL ---


episodePagePrefix : String
episodePagePrefix =
    "/e/"


podcastPagePrefix : String
podcastPagePrefix =
    "/p/"
