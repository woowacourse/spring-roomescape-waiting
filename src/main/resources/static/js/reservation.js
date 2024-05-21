let isEditing = false;
const RESERVATION_API_ENDPOINT = '/reservations';
const TIME_API_ENDPOINT = '/times';
const timesOptions = [];

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('add-button').addEventListener('click', addInputRow);

    requestRead(RESERVATION_API_ENDPOINT)
        .then(render)
        .catch(error => console.error('Error fetching reservations:', error));

    fetchTimes();
});

function render(data) {
    const tableBody = document.getElementById('table-body');
    tableBody.innerHTML = '';

    data.forEach(item => {
        const row = tableBody.insertRow();

        row.insertCell(0).textContent = item.id;
        row.insertCell(1).textContent = item.name;
        row.insertCell(2).textContent = item.date;
        row.insertCell(3).textContent = item.time.startAt;

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

function createSelect(options, defaultText, selectId, textProperty) {
    const select = document.createElement('select');
    select.className = 'form-control';
    select.id = selectId;

    const defaultOption = document.createElement('option');
    defaultOption.textContent = defaultText;
    select.appendChild(defaultOption);

    options.forEach(optionData => {
        const option = document.createElement('option');
        option.value = optionData.id;
        option.textContent = optionData[textProperty];
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
    if (isEditing) return;

    const tableBody = document.getElementById('table-body');
    const row = tableBody.insertRow();
    isEditing = true;

    const nameInput = createInput('text');
    const dateInput = createInput('date');
    const timeDropdown = createSelect(timesOptions, "시간 선택", 'time-select', 'startAt');

    const cellFieldsToCreate = ['', nameInput, dateInput, timeDropdown];

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
    event.stopPropagation();

    const row = event.target.parentNode.parentNode;
    const nameInput = row.querySelector('input[type="text"]');
    const dateInput = row.querySelector('input[type="date"]');
    const timeSelect = row.querySelector('select');

    const reservation = {
        name: nameInput.value,
        date: dateInput.value,
        timeId: timeSelect.value
    };

    requestCreate(reservation)
        .then(() => {
            location.reload();
        })
        .catch(error => console.error('Error:', error));

    isEditing = false;
}

function deleteRow(event) {
    const row = event.target.closest('tr');
    const reservationId = row.cells[0].textContent;

    requestDelete(reservationId)
        .then(() => row.remove())
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
            if (response.status !== 200) throw new Error('Delete failed');
        });
}

function requestRead(endpoint) {
    return fetch(endpoint)
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        });
}
