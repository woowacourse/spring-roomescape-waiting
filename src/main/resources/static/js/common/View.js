import { emit, on } from "./helpers.js";

export default class View {
  constructor(element) {
    this.element = element;
  }

  on(eventName, handler) {
    on(this.element, eventName, handler);
    return this;
  }

  emit(eventName, detail) {
    emit(this.element, eventName, detail);
  }
}
