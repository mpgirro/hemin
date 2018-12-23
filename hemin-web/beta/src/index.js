import './main.css';
import { Elm } from './Main.elm';
import registerServiceWorker from './registerServiceWorker';
import './customElements';
//import './podloveSubscribeButton';

Elm.Main.init({
  node: document.getElementById('root')
});

registerServiceWorker();
