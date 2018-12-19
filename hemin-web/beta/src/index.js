import './main.css';
import { Elm } from './Main.elm';
import registerServiceWorker from './registerServiceWorker';
import './customElements';

Elm.Main.init({
  node: document.getElementById('root')
});

registerServiceWorker();
