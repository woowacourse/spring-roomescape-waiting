let isEditing = false;
const RESERVATION_API_ENDPOINT = '/reservations';
const TIME_API_ENDPOINT = '/times';
const ADMIN_THEME_API_ENDPOINT = '/admin/themes';

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('add-reservation').addEventListener('click', addEditableRow);
  fetchReservations();
  Promise.all([fetchTimes(), fetchThemes()]);
});

function fetchTimes() {
  return requestReadTimes()
      .then(data => {
        const control = createTimeFormControl(data);
        appendHiddenControlToDocument(control);
      })
      .catch(error => console.error('Error fetching time:', error));
}

function fetchThemes() {
  return fetch(ADMIN_THEME_API_ENDPOINT)
      .then(res => res.json())
      .then(data => {
        const control = createThemeFormControl(data);
        appendHiddenControlToDocument(control);
      })
      .catch(error => console.error('Error fetching themes:', error));
}

function createTimeFormControl(timeData) {
  const select = document.createElement('select');
  select.className = 'form-control';
  select.id = 'time-select';

  const defaultOption = document.createElement('option');
  defaultOption.value = '';
  defaultOption.textContent = '시간 선택';
  select.appendChild(defaultOption);

  timeData.forEach(time => {
    const option = document.createElement('option');
    option.value = time.id;
    option.textContent = time.startAt;
    select.appendChild(option);
  });

  return select;
}

function createThemeFormControl(themeData) {
  const select = document.createElement('select');
  select.className = 'form-control';
  select.id = 'theme-select';

  const defaultOption = document.createElement('option');
  defaultOption.value = '';
  defaultOption.textContent = '테마 선택';
  select.appendChild(defaultOption);

  themeData.forEach(theme => {
    const option = document.createElement('option');
    option.value = theme.id;
    option.textContent = theme.name;
    select.appendChild(option);
  });

  return select;
}

function appendHiddenControlToDocument(control) {
  control.style.display = 'none';
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
  row.insertCell(0).textContent = reservation.id;
  row.insertCell(1).textContent = reservation.name;
  row.insertCell(2).textContent = reservation.date;
  row.insertCell(3).textContent = reservation.themeName;
  row.insertCell(4).textContent = reservation.time.startAt;

  const actionCell = row.insertCell(5);
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
  if (isEditing) return;

  const tableBody = document.getElementById('reservation-table-body');
  const row = tableBody.insertRow();
  isEditing = true;

  createEditableFieldsFor(row);
  addSaveAndCancelButtonsToRow(row);
}

function createEditableFieldsFor(row) {
  const nameInput = createInput('text');
  const dateInput = createInput('date');

  const themeDropdown = document.getElementById('theme-select').cloneNode(true);
  themeDropdown.removeAttribute('id');
  themeDropdown.className = 'form-control theme-dropdown';
  themeDropdown.style.display = '';

  const timeDropdown = document.getElementById('time-select').cloneNode(true);
  timeDropdown.removeAttribute('id');
  timeDropdown.className = 'form-control time-dropdown';
  timeDropdown.style.display = '';

  const fields = ['', nameInput, dateInput, themeDropdown, timeDropdown];

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
  const actionCell = row.insertCell(5);
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
  const themeSelect = row.querySelector('.theme-dropdown');
  const timeSelect = row.querySelector('.time-dropdown');

  const reservation = {
    name: nameInput.value,
    date: dateInput.value,
    themeId: Number(themeSelect.value),
    timeId: Number(timeSelect.value)
  };

  requestCreate(reservation)
      .then(data => updateRowWithReservationData(row, data))
      .catch(error => console.error('Error:', error));

  isEditing = false;
}

function updateRowWithReservationData(row, data) {
  const cells = row.cells;
  cells[0].textContent = data.id;
  cells[1].textContent = data.name;
  cells[2].textContent = data.date;
  cells[3].textContent = data.themeName;
  cells[4].textContent = data.time.startAt;

  cells[5].innerHTML = '';
  cells[5].appendChild(createActionButton('삭제', 'btn-danger', deleteRow));

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
