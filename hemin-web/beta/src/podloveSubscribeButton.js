import { Elm } from './Main.elm';

var button = document.getElementById('podlove-subscribe-button');
var app = Elm.Main.embed(button);
// receive something from Elm
app.ports.podloveSubscribeButton.subscribe(function (config) {
  console.log("got from Elm:", config);
  //window.podcastData = config;

  const child1 = document.createElement('script');

  const podloveSubscribeButtonJS = `window.podcastData = ${config}`;

  child1.append(document.createTextNode(podloveSubscribeButtonJS));
  button.appendChild(child1);

  const child2 = document.createElement('script');
  child2.setAttribute('class', 'podlove-subscribe-button');
  // el2.setAttribute('src', '/assets/podlove/subscribe-button/javascripts/app.js'); // TODO loading locally for some reason does not work
  child2.setAttribute('src', 'https://cdn.podlove.org/subscribe-button/javascripts/app.js');
  child2.setAttribute('data-language', 'en');
  child2.setAttribute('data-size', 'small');
  child2.setAttribute('data-json-data', 'podcastData');
  //child2.setAttribute('data-color', this.HIGHLIGHT_COLOR);
  child2.setAttribute('data-format', 'rectangle');
  child2.setAttribute('data-style', 'outline');
  button.appendChild(child2);

  //const child3 = document.createElement('noscript');
  //child3.appendChild(document.createTextNode(`<a href="${this.podcast.link}">Subscribe to feed</a>`));
  //button.appendChild(child3);
});
