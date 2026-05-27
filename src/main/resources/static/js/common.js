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
         const text = await response.text();
         let message = `요청에 실패했습니다. 상태 코드: ${response.status}`;

         if (text) {
            try {
                const errorBody = JSON.parse(text);
                message = errorBody.message || message;
            } catch (e) {
                message = text;
            }
         }

        throw new Error(message);
    }

    if (response.status === 204) return null;

    const text = await response.text();
    return text ? JSON.parse(text) : null;
}
