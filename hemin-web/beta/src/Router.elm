module Router exposing (Route(..), fromUrl)

import Browser.Navigation as Nav
import Episode exposing (Episode)
import Html exposing (Attribute)
import Html.Attributes as Attr
import Podcast exposing (Podcast)
import Url exposing (Url)
import Url.Parser as Parser exposing ((</>), (<?>), Parser, oneOf, s, string)
import Url.Parser.Query as Query



--import Search
-- ROUTING


type Route
    = NotFound
    | HomePage
    | RootPage
    | PodcastPage String
    | EpisodePage String
    | SearchPage (Maybe String) (Maybe Int) (Maybe Int)


parser : Parser (Route -> a) a
parser =
    oneOf
        [ Parser.map HomePage Parser.top
        , Parser.map PodcastPage (s "p" </> string)
        , Parser.map EpisodePage (s "e" </> string)
        , Parser.map SearchPage (s "search" <?> Query.string "q" <?> Query.int "p" <?> Query.int "s")
        ]



-- PUBLIC HELPERS
-- Create a helper function that converts a url to a route using the parser


fromUrl : Url.Url -> Route
fromUrl url =
    Maybe.withDefault NotFound (Parser.parse parser url)
