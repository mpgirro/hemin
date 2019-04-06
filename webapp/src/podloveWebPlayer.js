
export default function configurePodloveWebPlayer(oConfig) {
  "use strict";

  console.log("Received WebPlayer config from Elm: " + oConfig);

  window.podloveSubscribeButtonData = oConfig;

  // the element we append the Subscribe Button to
  const oSubscribeButtonContainer = document.getElementById("podlove-web-player");

}
