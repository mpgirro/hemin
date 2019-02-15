var node = document.getElementById('podlove-web-player');
var app = Elm.Main.embed(node);
// receive something from Elm
app.ports.podloveWebPlayer.subscribe(function (config) {
  console.log("got from Elm:", config);
});
