let isEditing = false;
const RESERVATION_API_ENDPOINT = '/reservations';
const TIME_API_ENDPOINT = '/times';
const THEME_API_ENDPOINT = '/themes';
const timesOptions = [];
const themesOptions = [];

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('add-button').addEventListener('click', addInputRow);

    requestRead(RESERVATION_API_ENDPOINT)
        .then(render)
        .catch(error => console.error('Error fetching reservations:', error));

    fetchTimes();
    fetchThemes();
});

function render(data) {
    const tableBody = document.getElementById('table-body');
    tableBody.innerHTML = '';

    data.forEach(item => {
        const row = tableBody.insertRow();

        row.insertCell(0).textContent = item.id;            // 예약 id
        row.insertCell(1).textContent = item.name;          // 예약자명
        row.insertCell(2).textContent = item.theme.name;    // 테마명
        row.insertCell(3).textContent = item.date;          // 예약 날짜
        row.insertCell(4).textContent = item.time.startAt;  // 시작 시간

        const actionCell = row.insertCell(row.cells.length);
        actionCell.appendChild(createActionButton('삭제', 'btn-danger', deleteRow));
    });
}

function fetchTimes() {
    requestRead(TIME_API_ENDPOINT)
        .then(data => {
            timesOptions.push(...data);
        })
        .catch(error => console.error('Error fetching time:', error));
}

function fetchThemes() {
    requestRead(THEME_API_ENDPOINT)
        .then(data => {
            themesOptions.push(...data);
        })
        .catch(error => console.error('Error fetching theme:', error));
}

function createSelect(options, defaultText, selectId, textProperty) {
    const select = document.createElement('select');
    select.className = 'form-control';
    select.id = selectId;

    // 기본 옵션 추가
    const defaultOption = document.createElement('option');
    defaultOption.textContent = defaultText;
    select.appendChild(defaultOption);

    // 넘겨받은 옵션을 바탕으로 드롭다운 메뉴 아이템 생성
    options.forEach(optionData => {
        const option = document.createElement('option');
        option.value = optionData.id;
        option.textContent = optionData[textProperty]; // 동적 속성 접근
        select.appendChild(option);
    });

    return select;
}

function createActionButton(label, className, eventListener) {
    const button = document.createElement('button');
    button.textContent = label;
    button.classList.add('btn', className, 'mr-2');
    button.addEventListener('click', eventListener);
    return button;
}

function addInputRow() {
    if (isEditing) return;  // 이미 편집 중인 경우 추가하지 않음

    const tableBody = document.getElementById('table-body');
    const row = tableBody.insertRow();
    isEditing = true;

    const nameInput = createInput('text');
    const dateInput = createInput('date');
    const timeDropdown = createSelect(timesOptions, "시간 선택", 'time-select', 'startAt');
    const themeDropdown = createSelect(themesOptions, "테마 선택", 'theme-select', 'name');

    const cellFieldsToCreate = ['', nameInput, themeDropdown, dateInput, timeDropdown];

    cellFieldsToCreate.forEach((field, index) => {
        const cell = row.insertCell(index);
        if (typeof field === 'string') {
            cell.textContent = field;
        } else {
            cell.appendChild(field);
        }
    });

    const actionCell = row.insertCell(row.cells.length);
    actionCell.appendChild(createActionButton('확인', 'btn-custom', saveRow));
    actionCell.appendChild(createActionButton('취소', 'btn-secondary', () => {
        row.remove();
        isEditing = false;
    }));
}

function createInput(type) {
    const input = document.createElement('input');
    input.type = type;
    input.className = 'form-control';
    return input;
}

function createActionButton(label, className, eventListener) {
    const button = document.createElement('button');
    button.textContent = label;
    button.classList.add('btn', className, 'mr-2');
    button.addEventListener('click', eventListener);
    return button;
}

function saveRow(event) {
    // 이벤트 전파를 막는다
    event.stopPropagation();

    const row = event.target.parentNode.parentNode;
    const nameInput = row.querySelector('input[type="text"]');
    const themeSelect = row.querySelector('#theme-select');
    const dateInput = row.querySelector('input[type="date"]');
    const timeSelect = row.querySelector('#time-select');

    const reservation = {
        name: nameInput.value,
        themeId: themeSelect.value,
        date: dateInput.value,
        timeId: timeSelect.value
    };

    requestCreate(reservation)
        .then(() => {
            location.reload();
        })
        .catch(error => console.error('Error:', error));

    isEditing = false;  // isEditing 값을 false로 설정
}

function deleteRow(event) {
    console.log('💥 deleteRow called');
    const row = event.target.closest('tr');
    const reservationId = row.cells[0].textContent;

    requestDelete(reservationId)
        .then(() => {
            return window.confirm('삭제가 완료되었습니다.\n목록을 새로고침하시겠습니까?')
                ? requestRead(RESERVATION_API_ENDPOINT)
                : null;
        })
        .then(data => {
            if (data) render(data);
        })
        .catch(error => console.error('Error:', error));
}

function requestCreate(reservation) {
    const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(reservation)
    };

    return fetch(RESERVATION_API_ENDPOINT, requestOptions)
        .then(response => {
            if (response.status === 201) return response.json();
            throw new Error('Create failed');
        });
}

function requestDelete(id) {
    const requestOptions = {
        method: 'DELETE',
    };

    return fetch(`${RESERVATION_API_ENDPOINT}/${id}`, requestOptions)
        .then(response => {
            if (response.status !== 204) throw new Error('Delete failed');
        });
}

function requestRead(endpoint) {
    return fetch(endpoint)
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        });
}
