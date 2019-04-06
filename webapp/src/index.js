import './main.css';
import { Elm } from './Main.elm';
import registerServiceWorker from './registerServiceWorker';
import './customElements';
import configurePodloveSubscribeButton from './podloveSubscribeButton';
import configurePodloveWebPlayer from './podloveWebPlayer';

const app = Elm.Main.init({
  node: document.getElementById('root')
});


// register callback function for data received from Elm
app.ports.podloveSubscribeButton.subscribe(configurePodloveSubscribeButton);
app.ports.podloveWebPlayer.subscribe(configurePodloveWebPlayer);


registerServiceWorker();
