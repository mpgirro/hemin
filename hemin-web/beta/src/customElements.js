// This is a workaround for Elm > 0.19, from which on innerHTML is prevented.
// We need to import this module before instantiating any custom elements in Elm.

customElements.define(
  "rendered-html",
  class RenderedHtml extends HTMLElement {
    constructor() {
      super();
      this._content = "";
    }

    set content(value) {
      if (this._content === value)
        return;
      this._content = value;
      this.innerHTML = value;
    }

    get content() {
      return this._content;
    }
  }
);
