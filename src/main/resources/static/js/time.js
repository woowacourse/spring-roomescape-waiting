const API = '/times';
let isEditing = false;

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('add-button').addEventListener('click', startAdd);
    refresh();
});

async function refresh() {
    try {
        const times = await fetchJson(API);
        render(times);
    } catch (error) {
        console.error('시간 조회 실패:', error);
    }
}

function render(times) {
    const tbody = document.getElementById('table-body');
    tbody.innerHTML = '';

    if (times.length === 0) {
        showEmptyState(tbody, 3, '등록된 시간이 없습니다. "시간 추가" 버튼으로 시작해보세요.');
        return;
    }

    times.forEach((time, index) => {
        const row = tbody.insertRow();
        row.insertCell().textContent = index + 1;
        row.insertCell().textContent = time.startAt;

        const actions = row.insertCell();
        actions.className = 'actions';
        actions.appendChild(createButton('삭제', 'btn-danger', () => deleteRow(time.id)));
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

    const timeInput = createInput('time');
    row.insertCell().appendChild(timeInput);

    const actions = row.insertCell();
    actions.className = 'actions';
    actions.appendChild(createButton('저장', 'btn-primary', async () => {
        await saveRow(timeInput.value);
    }));
    actions.appendChild(createButton('취소', 'btn-ghost', () => {
        row.remove();
        isEditing = false;
        refresh();
    }));
}

async function saveRow(startAt) {
    if (!startAt) {
        alert('시간을 선택해주세요.');
        return;
    }
    try {
        await fetchJson(API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ startAt })
        });
        isEditing = false;
        refresh();
    } catch (error) {
        console.error('시간 추가 실패:', error);
        alert(error.message);
    }
}

async function deleteRow(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
        await fetchJson(`${API}/${id}`, { method: 'DELETE' });
        refresh();
    } catch (error) {
        console.error('시간 삭제 실패:', error);
        alert(error.message);
    }
}
