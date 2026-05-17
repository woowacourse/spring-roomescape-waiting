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

class HttpError extends Error {
    constructor(response, problem) {
        super(problem?.detail || `요청이 실패했습니다. (HTTP ${response.status})`);
        this.name = 'HttpError';
        this.status = response.status;
        this.problem = problem;
    }
}

async function fetchJson(url, options = {}) {
    const response = await fetch(url, options);
    if (!response.ok) {
        let problem = null;
        const contentType = response.headers.get('Content-Type') || '';
        if (contentType.includes('application/problem+json')) {
            try {
                problem = await response.json();
            } catch (_) {
                // problem body 파싱 실패 시 무시하고 status만으로 진행
            }
        }
        throw new HttpError(response, problem);
    }
    if (response.status === 204) return null;
    const text = await response.text();
    return text ? JSON.parse(text) : null;
}

function getErrorMessage(error, fallback) {
    if (error instanceof HttpError) {
        const problem = error.problem;
        if (problem && Array.isArray(problem.errors) && problem.errors.length > 0) {
            const joined = problem.errors
                .map(item => item.reason)
                .filter(Boolean)
                .join('\n');
            if (joined) return joined;
        }
        if (problem && problem.detail) return problem.detail;
    }
    return fallback;
}
