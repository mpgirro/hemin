
export default function configurePodloveSubscribeButton(oConfig) {
  "use strict";

  console.log("Received SubscribeButton config from Elm: " + JSON.stringify(oConfig));

  // the element we append the Subscribe Button to
  const oContainer = document.getElementById("podlove-subscribe-button-container");

  // the button can be null, if the Elm view has not been rendered yet
  if (oContainer) {

    // remove all child nodes, such that we don't double and triple add the script
    while (oContainer.firstChild) {
      oContainer.removeChild(oContainer.firstChild);
    }

    window.podloveSubscribeButtonData = oConfig;

    // define the actual button to the wrapper
    const oScript = document.createElement("script");
    oScript.setAttribute("class", "podlove-subscribe-button");
    //oScript.setAttribute('src', '/assets/podlove/subscribe-button/javascripts/app.js'); // TODO loading locally for some reason does not work
    oScript.setAttribute("src", "https://cdn.podlove.org/subscribe-button/javascripts/app.js");
    oScript.setAttribute("data-language", "en");
    oScript.setAttribute("data-size", "small");
    oScript.setAttribute("data-json-data", "podloveSubscribeButtonData");
    //oScript.setAttribute('data-color', this.HIGHLIGHT_COLOR);
    oScript.setAttribute("data-format", "rectangle");
    oScript.setAttribute("data-style", "outline");

    // add child element to render the Subscribe Button
    oContainer.appendChild(oScript);
  }

}
