import './main.css';
import { Elm } from './Main.elm';
import registerServiceWorker from './registerServiceWorker';
import './customElements';
import configurePodloveSubscribeButton from './podloveSubscribeButton';

var app = Elm.Main.init({
  node: document.getElementById('root')
});

app.ports.podloveSubscribeButton.subscribe(configurePodloveSubscribeButton);

registerServiceWorker();
