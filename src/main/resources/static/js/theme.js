const API = '/themes';
let isEditing = false;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('add-button').addEventListener('click', startAdd);
    refresh();
});

async function refresh() {
    try {
        const data = await fetchJson(API);
        render(data.themes);
    } catch (error) {
        console.error('테마 조회 실패:', error);
    }
}

function render(themes) {
    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';

    if (themes.length === 0) {
        showEmptyState(tbody, 6, '등록된 테마가 없습니다. "테마 추가" 버튼으로 시작해보세요.');
        return;
    }

    themes.forEach((theme, index) => {
        const row = tbody.insertRow();
        row.insertCell().textContent = index + 1;

        const thumbCell = row.insertCell();
        if (theme.thumbnailImageUrl) {
            const img = document.createElement('img');
            img.src = theme.thumbnailImageUrl;
            img.alt = theme.name;
            img.className = 'thumb';
            img.onerror = () => { img.style.display = 'none'; };
            thumbCell.appendChild(img);
        }

        row.insertCell().textContent = theme.name;
        row.insertCell().textContent = theme.description;

        const urlCell = row.insertCell();
        urlCell.className = 'muted';
        urlCell.textContent = theme.thumbnailImageUrl;

        const actions = row.insertCell();
        actions.className = 'actions';
        actions.appendChild(createButton('삭제', 'btn-danger', () => deleteRow(theme.id)));
    });
}

function startAdd() {
    if (isEditing) return;
    isEditing = true;

    const tbody = document.getElementById('table-body');
    const emptyRow = tbody.querySelector('.empty-state');
    if (emptyRow) emptyRow.closest('tr').remove();

    const row = tbody.insertRow(0);
    row.insertCell().textContent = '';
    row.insertCell().textContent = '';

    const nameInput = createInput('text', '예: 공포의 방');
    const descInput = createInput('text', '예: 어두운 방을 탈출하라');
    const urlInput = createInput('url', 'https://...');

    row.insertCell().appendChild(nameInput);
    row.insertCell().appendChild(descInput);
    row.insertCell().appendChild(urlInput);

    const actions = row.insertCell();
    actions.className = 'actions';
    actions.appendChild(createButton('저장', 'btn-primary', async () => {
        await saveRow(nameInput.value, descInput.value, urlInput.value);
    }));
    actions.appendChild(createButton('취소', 'btn-ghost', () => {
        row.remove();
        isEditing = false;
        refresh();
    }));
}

async function saveRow(name, description, thumbnailImageUrl) {
    if (!name.trim() || !description.trim() || !thumbnailImageUrl.trim()) {
        alert('모든 항목을 입력해주세요.');
        return;
    }
    try {
        await fetchJson(API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, description, thumbnailImageUrl })
        });
        isEditing = false;
        refresh();
    } catch (error) {
        console.error('테마 추가 실패:', error);
        alert(getErrorMessage(error, '테마 추가에 실패했습니다.'));
    }
}

async function deleteRow(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
        await fetchJson(`${API}/${id}`, { method: 'DELETE' });
        refresh();
    } catch (error) {
        console.error('테마 삭제 실패:', error);
        alert(getErrorMessage(error, '테마 삭제에 실패했습니다.'));
    }
}
