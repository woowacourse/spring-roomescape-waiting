let isEditing = false;
const RESERVATION_API_ENDPOINT = '/reservations';
const TIME_API_ENDPOINT = '/times';

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('add-reservation').addEventListener('click', addEditableRow);
  fetchReservations();
  fetchTimes();
});

function fetchTimes() {
  requestReadTimes()
      .then(data => {
        const timeSelectControl = createFormControl(data);
        appendFormControlToDocument(timeSelectControl);
      })
      .catch(error => console.error('Error fetching time:', error));
}

function createFormControl(timeData) {
  const select = document.createElement('select');
  select.className = 'form-control';
  select.id = 'time-select';

  const defaultOption = document.createElement('option');
  defaultOption.textContent = "시간 선택";
  select.appendChild(defaultOption);

  timeData.forEach(time => {
    const option = document.createElement('option');
    option.value = time.id;
    option.textContent = time.time;
    select.appendChild(option);
  });

  return select;
}

function appendFormControlToDocument(control) {
  document.body.appendChild(control);
}

function fetchReservations() {
  requestRead()
      .then(renderReservations)
      .catch(error => console.error('Error fetching reservations:', error));
}

function renderReservations(data) {
  const tableBody = document.getElementById('reservation-table-body');
  tableBody.innerHTML = '';

  data.forEach(reservation => {
    const row = tableBody.insertRow();
    insertReservationRow(row, reservation);
  });
}

function insertReservationRow(row, reservation) {
  ['id', 'name', 'date'].forEach((field, index) => {
    row.insertCell(index).textContent = reservation[field];
  });

  row.insertCell(3).textContent = reservation.time.time;

  const actionCell = row.insertCell(4);
  actionCell.appendChild(createActionButton('삭제', 'btn-danger', deleteRow));
}

function createActionButton(label, className, eventListener) {
  const button = document.createElement('button');
  button.textContent = label;
  button.classList.add('btn', className, 'mr-2');
  button.addEventListener('click', eventListener);
  return button;
}

function addEditableRow() {

  if (isEditing) return;  // 이미 편집 중인 경우 추가하지 않음

  const tableBody = document.getElementById('reservation-table-body');
  const row = tableBody.insertRow();
  isEditing = true;

  createEditableFieldsFor(row);
  addSaveAndCancelButtonsToRow(row);
}

function createEditableFieldsFor(row) {
  const nameInput = createInput('text');
  const dateInput = createInput('date');
  const timeDropdown = document.getElementById('time-select').cloneNode(true);

  const fields = ['', nameInput, dateInput, timeDropdown];

  fields.forEach((field, index) => {
    const cell = row.insertCell(index);
    if (typeof field === 'string') {
      cell.textContent = field;
    } else {
      cell.appendChild(field);
    }
  });
}

function addSaveAndCancelButtonsToRow(row) {
  const actionCell = row.insertCell(4);
  actionCell.appendChild(createActionButton('확인', 'btn-primary', saveRow));
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

function saveRow(event) {
  const row = event.target.parentNode.parentNode;
  const nameInput = row.querySelector('input[type="text"]');
  const dateInput = row.querySelector('input[type="date"]');
  const timeSelect = row.querySelector('select');

  const reservation = {
    name: nameInput.value,
    date: dateInput.value,
    time: timeSelect.value
  };

  requestCreate(reservation)
      .then(data => updateRowWithReservationData(row, data))
      .catch(error => console.error('Error:', error));

  isEditing = false;  // isEditing 값을 false로 설정
}

function updateRowWithReservationData(row, data) {
  const cells = row.cells;
  cells[0].textContent = data.id;
  cells[1].textContent = data.name;
  cells[2].textContent = data.date;
  cells[3].textContent = data.time.time;

  // 버튼 변경: 삭제 버튼으로 변경
  cells[4].innerHTML = '';
  cells[4].appendChild(createActionButton('삭제', 'btn-danger', deleteRow));

  isEditing = false;

  // Remove the editable input fields and just show the saved data
  for (let i = 1; i <= 3; i++) {
    const inputElement = cells[i].querySelector('input');
    if (inputElement) {
      inputElement.remove();
    }
  }
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

function requestRead() {
  return fetch(RESERVATION_API_ENDPOINT)
      .then(response => {
        if (response.status === 200) return response.json();
        throw new Error('Read failed');
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

function requestReadTimes() {
  return fetch(TIME_API_ENDPOINT)
      .then(response => {
        if (response.status === 200) return response.json();
        throw new Error('Read failed');
      });
}
