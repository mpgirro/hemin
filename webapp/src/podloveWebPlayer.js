
export default function configurePodloveWebPlayer(oConfig) {
  "use strict";

  console.log("Received WebPlayer config from Elm: " + JSON.stringify(oConfig));

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
    "tabs": {
      "chapters" : oConfig.tabs.chapters
    },
    "visibleComponents": [
      "tabChapters",
      "tabAudio",
      "progressbar",
      "controlSteppers",
      "controlChapters"
    ]
  };

  const sPlayerFn = `podlovePlayer("#podlove-web-player", ${JSON.stringify(oPlayerConfig)});`;
  //const sPlayerFn = `podlovePlayer("#podlove-web-player", ${JSON.stringify(oConfig)});`;

  const oPlayerScript = document.createElement('script');
  oPlayerScript.appendChild(document.createTextNode(sPlayerFn));
  document.body.appendChild(oPlayerScript);

}
