function createInput(type = 'text', placeholder = '') {
    const input = document.createElement('input');
    input.type = type;
    input.className = 'input';
    if (placeholder) input.placeholder = placeholder;
    return input;
}

function createSelect() {
    const select = document.createElement('select');
    select.className = 'select';
    return select;
}

function createButton(label, variant, onClick) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = `btn ${variant}`;
    button.textContent = label;
    button.addEventListener('click', onClick);
    return button;
}

function showEmptyState(tbody, colspan, message) {
    const tr = tbody.insertRow();
    const td = tr.insertCell();
    td.colSpan = colspan;
    td.className = 'empty-state';
    td.textContent = message;
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        throw new Error(`Request failed: ${response.status}`);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}
