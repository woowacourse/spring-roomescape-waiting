export function qs(selector, scope = document) {
    if (!selector) {
        throw new Error("selector is required");
    }

    return scope.querySelector(selector);
}

export function qsAll(selector, scope = document) {
    if (!selector) {
        throw new Error("selector is required");
    }

    return Array.from(scope.querySelectorAll(selector));
}

export function on(target, eventName, handler) {
    target.addEventListener(eventName, handler);
}

export function delegate(target, eventName, selector, handler) {
    on(target, eventName, (event) => {
        const matched = event.target.closest(selector);

        if (matched && target.contains(matched)) {
            handler.call(matched, event);
        }
    });
}

export function emit(target, eventName, detail) {
    const event = new CustomEvent(eventName, {detail});
    target.dispatchEvent(event);
}

export function clearElement(element) {
    element.replaceChildren();
}

export function createElement(tagName, className, textContent) {
    const element = document.createElement(tagName);

    if (className) {
        element.className = className;
    }

    if (textContent !== undefined) {
        element.textContent = textContent;
    }

    return element;
}

export function formatDateInputValue(date = new Date()) {
    return date.toISOString().split("T")[0];
}

export function formatTime(startAt) {
    return startAt.substring(0, 5);
}

export function formatCurrency(value) {
    return new Intl.NumberFormat("ko-KR", {
        style: "currency",
        currency: "KRW"
    }).format(value);
}

export function getSearchParam(name) {
    return new URLSearchParams(window.location.search).get(name);
}
