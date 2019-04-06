
export default function configurePodloveSubscribeButton(oConfig) {
  "use strict";

  // the element we append the Subscribe Button to
  const oSubscribeButtonContainer = document.getElementById("podlove-subscribe-button");

  // the button can be null, if the Elm view has not been rendered yet
  if (oSubscribeButtonContainer) {

    window.podloveSubscribeButtonData = oConfig;

    // define the actual button to the wrapper
    const oButtonScript = document.createElement("script");
    //oButtonScript.setAttribute("class", "subscribe-button");
    // el2.setAttribute('src', '/assets/podlove/subscribe-button/javascripts/app.js'); // TODO loading locally for some reason does not work
    oButtonScript.setAttribute("src", "https://cdn.podlove.org/subscribe-button/javascripts/app.js");
    oButtonScript.setAttribute("data-language", "en");
    oButtonScript.setAttribute("data-size", "small");
    oButtonScript.setAttribute("data-json-data", "podloveSubscribeButtonData");
    //oButtonScript.setAttribute('data-color', this.HIGHLIGHT_COLOR);
    oButtonScript.setAttribute("data-format", "rectangle");
    oButtonScript.setAttribute("data-style", "outline");

    // add child element to render the Subscribe Button
    oSubscribeButtonContainer.appendChild(oButtonScript);
  }

}
