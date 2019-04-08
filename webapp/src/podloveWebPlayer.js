
export default function configurePodloveWebPlayer(oConfig) {
  "use strict";

  console.log("Received WebPlayer config from Elm: " + JSON.stringify(oConfig));

  oConfig.theme = {
    "main" : "#FFFFFF",
    "highlight" : "#000000"
  };
  oConfig.visibleComponents = [
  //  "tabChapters",
    "tabAudio",
    "progressbar",
    "controlSteppers",
    "controlChapters"
  ];

  if (oConfig.chapters.length > 0) {
    oConfig.visibleComponents.push("tabChapters");
  }

  /*
  const oPlayerConfig = {
    "duration" : oConfig.duration,
    "audio" : [{
      "url" : oConfig.audio.url,
      "size" : oConfig.audio.size,
      "mimeType" : oConfig.audio.mimeType
    }],
    "theme" : {
      "main" : oConfig.theme.main,
      "highlight" : oConfig.theme.highlight
    },
    "visibleComponents": oConfig.visibleComponents
  };
  */

  //const sPlayerFn = `podlovePlayer("#podlove-web-player", ${JSON.stringify(oPlayerConfig)});`;
  /*
  const sPlayerFn = `podlovePlayer("#podlove-web-player", {
    "duration" : "${oConfig.duration}",
    "audio" : [{
      "url" : "${oConfig.audio.url}",
      "size" : ${oConfig.audio.size},
      "mimeType" : "${oConfig.audio.mimeType}"
    }],
    "theme" : {
      "main" : "#ffffff",
      "highlight" : "#2B8AC6"
    },
    "visibleComponents": [
      "tabChapters",
      "tabAudio",
      "progressbar",
      "controlSteppers",
      "controlChapters"
    ]
  });`;
  */
  //const sPlayerFn = `podlovePlayer("#podlove-web-player", ${JSON.stringify(oConfig)});`;
  //console.log(sPlayerFn);

  const oScript = document.createElement('script');
  //oScript.appendChild(document.createTextNode(sPlayerFn));
  oScript.innerHTML = `podlovePlayer("#podlove-web-player", ${JSON.stringify(oConfig)});`;
  document.body.appendChild(oScript);

}
