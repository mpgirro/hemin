# Beta (Hemin WebApp in Elm)

Try out:

    elm reactor

Compile the webapp with:

    elm make src/Main.elm --output=public/elm.js

Test in a local HTTP server like [http-server](https://www.npmjs.com/package/http-server):

    http-server

Or use [elm-live](https://github.com/wking-io/elm-live):

    elm-live --start-page index.html src/Main.elm -- --output=elm.js