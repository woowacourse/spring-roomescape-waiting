let isEditing = false;
const TIME_API = '/times';

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('add-time').addEventListener('click', addEditableRow);
  fetchTimes();
});

function fetchTimes() {
  apiFetch(TIME_API)
    .then(renderTimes)
    .catch(showError);
}

function renderTimes(data) {
  const tableBody = document.getElementById('time-table-body');
  tableBody.innerHTML = '';
  data.forEach(time => {
    const row = tableBody.insertRow();
    insertTimeRow(row, time);
  });
}

function insertTimeRow(row, time) {
  row.insertCell(0).textContent = time.id;
  row.insertCell(1).textContent = time.startAt;
  const actionCell = row.insertCell(2);
  actionCell.appendChild(createButton('삭제', 'btn-danger', deleteRow));
}

function addEditableRow() {
  if (isEditing) return;
  isEditing = true;

  const tableBody = document.getElementById('time-table-body');
  const row = tableBody.insertRow();

  row.insertCell(0).textContent = '';

  const startInput = createInput('time');
  startInput.placeholder = '시작 시간';
  const startCell = row.insertCell(1);
  startCell.appendChild(startInput);

  const actionCell = row.insertCell(2);

  const finishInput = createInput('time');
  finishInput.placeholder = '종료 시간';
  finishInput.className = 'form-control d-inline-block mr-1';
  finishInput.style.width = '130px';

  actionCell.appendChild(finishInput);
  actionCell.appendChild(createButton('확인', 'btn-primary mr-1', (e) => saveRow(e, startInput, finishInput)));
  actionCell.appendChild(createButton('취소', 'btn-secondary', () => { row.remove(); isEditing = false; }));
}

function saveRow(event, startInput, finishInput) {
  const row = event.target.closest('tr');
  const body = {
    startAt: startInput.value,
    finishAt: finishInput.value
  };

  if (!body.startAt || !body.finishAt) {
    alert('시작 시간과 종료 시간을 모두 입력해주세요.');
    return;
  }

  apiFetch(TIME_API, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(body)
  })
    .then(data => {
      row.cells[0].textContent = data.id;
      row.cells[1].innerHTML = '';
      row.cells[1].textContent = data.startAt;
      row.cells[2].innerHTML = '';
      row.cells[2].appendChild(createButton('삭제', 'btn-danger', deleteRow));
      isEditing = false;
    })
    .catch(showError);
}

function deleteRow(event) {
  const row = event.target.closest('tr');
  const id = row.cells[0].textContent;
  apiFetch(`${TIME_API}/${id}`, { method: 'DELETE' })
    .then(() => row.remove())
    .catch(showError);
}

function createInput(type) {
  const input = document.createElement('input');
  input.type = type;
  input.className = 'form-control d-inline-block mr-1';
  input.style.width = '130px';
  return input;
}

function createButton(label, className, handler) {
  const btn = document.createElement('button');
  btn.textContent = label;
  btn.className = `btn ${className}`;
  btn.addEventListener('click', handler);
  return btn;
}
