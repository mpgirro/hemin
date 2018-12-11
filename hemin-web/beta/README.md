# Beta (Hemin WebApp in Elm)

Try out:

    elm reactor

Compile the webapp with:

    elm make src/Main.elm --output=public/elm.js

Test in a local HTTP server like [http-server](https://www.npmjs.com/package/http-server):

    http-server -P http://localhost:8000/ -p 8000

Or use [elm-live](https://github.com/wking-io/elm-live):

    elm-live src/Main.elm --start-page index.html --pushstate -- --output=elm.js

Using [live-server](https://github.com/tapio/live-server)

    live-server --port=8000 --entry-file=./index.html

Note that `--pushstate` always serves the `index.html` page on every request. Thus Elm handles routing client-side. __Every production deployment setup also requires a configuration where the webserver always serves the `index.html` page__!