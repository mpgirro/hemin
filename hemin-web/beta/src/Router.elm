module Router exposing (Route(..), fromUrl, parser)

import Browser.Navigation as Nav
import Html exposing (Attribute)
import Html.Attributes as Attr
import Url exposing (Url)
import Url.Parser as Parser exposing ((</>), (<?>), Parser, oneOf, s, string)
import Url.Parser.Query as Query



-- ROUTER


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
