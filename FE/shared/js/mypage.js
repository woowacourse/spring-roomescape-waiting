import { api, getThemeThumbnail } from './api.js';

const nameForm = document.getElementById('name-gate-form');
const nameInput = document.getElementById('guest-name');
const nameError = document.getElementById('name-error');
const ownerTitle = document.getElementById('reservation-owner');
const reservationList = document.getElementById('my-reservation-list');
const changeNameBtn = document.getElementById('change-name-btn');

let currentName = '';
let currentReservations = [];
let currentWaitings = [];

const escapeHtml = (value) => String(value ?? '')
  .replaceAll('&', '&amp;')
  .replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;')
  .replaceAll("'", '&#039;');

const escapeCssUrl = (value) => String(value ?? '').replaceAll("'", '%27');

const today = () => new Date().toISOString().split('T')[0];

const setMessage = (message = '', type = 'error') => {
  nameError.textContent = message;
  nameError.dataset.type = type;
};

const setLoading = () => {
  reservationList.innerHTML = `
    <div class="my-empty">
      <strong>예약을 불러오는 중입니다.</strong>
      <span>잠시만 기다려주세요.</span>
    </div>
  `;
};

const getReservationStatus = (reservation) => {
  const reservationDate = reservation.date;
  const startAt = reservation.time?.startAt || '00:00';
  const target = new Date(`${reservationDate}T${startAt}`);
  return target.getTime() < Date.now() ? 'past' : 'active';
};

const renderEmpty = (name) => {
  reservationList.innerHTML = `
    <div class="my-empty">
      <strong>${escapeHtml(name)}님의 예약과 대기가 없습니다.</strong>
      <span>테마를 선택해 새 예약이나 대기 신청을 진행해보세요.</span>
      <a class="btn-primary btn-pill" href="index.html#themes">테마 보러가기</a>
    </div>
  `;
};

const renderReservationCard = (reservation, index) => {
  const thumbnail = getThemeThumbnail(reservation.theme, index);
  const status = getReservationStatus(reservation);
  const disabled = status === 'past' ? 'disabled' : '';
  const statusText = status === 'past' ? '지난 예약' : '변경 가능';

  return `
    <article class="my-reservation-card" data-id="${reservation.id}" data-theme-id="${reservation.theme.id}">
      <div class="my-thumb" style="background-image: linear-gradient(135deg, rgba(7, 6, 5, 0.08), rgba(7, 6, 5, 0.42)), url('${escapeCssUrl(thumbnail)}')"></div>
      <div class="my-reservation-body">
        <div class="my-card-head">
          <div>
            <span class="my-status ${status}">${statusText}</span>
            <h3>${escapeHtml(reservation.theme.name)}</h3>
          </div>
          <strong>${escapeHtml(reservation.date)} ${escapeHtml(reservation.time.startAt)}</strong>
        </div>
        <p>${escapeHtml(reservation.theme.description)}</p>
        <div class="my-edit-grid">
          <label>
            날짜
            <input class="my-date" type="date" min="${today()}" value="${escapeHtml(reservation.date)}" ${disabled}>
          </label>
          <label>
            시간
            <select class="my-time" ${disabled}>
              <option value="${reservation.time.id}">${escapeHtml(reservation.time.startAt)}</option>
            </select>
          </label>
        </div>
        <p class="card-message" role="alert"></p>
        <div class="my-actions">
          <button class="btn-primary btn-pill update-reservation" type="button" ${disabled}>변경 저장</button>
          <button class="btn-link cancel-reservation" type="button" ${disabled}>예약 취소</button>
        </div>
      </div>
    </article>
  `;
};

const renderWaitingCard = (waiting, index) => {
  const thumbnail = getThemeThumbnail(waiting.theme, index + currentReservations.length);
  const status = getReservationStatus(waiting);
  const disabled = status === 'past' ? 'disabled' : '';
  const statusText = status === 'past' ? '지난 대기' : `${waiting.rank || '-'}순위 대기`;

  return `
    <article class="my-reservation-card waiting-card" data-id="${waiting.id}" data-kind="waiting">
      <div class="my-thumb" style="background-image: linear-gradient(135deg, rgba(7, 6, 5, 0.08), rgba(7, 6, 5, 0.42)), url('${escapeCssUrl(thumbnail)}')"></div>
      <div class="my-reservation-body">
        <div class="my-card-head">
          <div>
            <span class="my-status waiting">${escapeHtml(statusText)}</span>
            <h3>${escapeHtml(waiting.theme.name)}</h3>
          </div>
          <strong>${escapeHtml(waiting.date)} ${escapeHtml(waiting.time.startAt)}</strong>
        </div>
        <p>${escapeHtml(waiting.theme.description)}</p>
        <div class="waiting-rank-panel">
          <strong>${waiting.rank || '-'}순위</strong>
          <span>앞선 예약이 취소되면 자동으로 예약 전환됩니다.</span>
        </div>
        <p class="card-message" role="alert"></p>
        <div class="my-actions">
          <button class="btn-link cancel-waiting" type="button" ${disabled}>대기 취소</button>
        </div>
      </div>
    </article>
  `;
};

const loadAvailableTimes = async (card, reservation) => {
  const dateInput = card.querySelector('.my-date');
  const timeSelect = card.querySelector('.my-time');
  const message = card.querySelector('.card-message');
  const date = dateInput.value;

  timeSelect.innerHTML = '<option>시간 확인 중</option>';
  timeSelect.disabled = true;

  try {
    const times = await api.getReservableTimes(date, reservation.theme.id);
    const options = times
      .filter(time => time.available || String(time.timeId) === String(reservation.time.id))
      .map(time => `
        <option value="${time.timeId}" ${String(time.timeId) === String(reservation.time.id) ? 'selected' : ''}>
          ${escapeHtml(time.startAt)}${time.available ? '' : ' · 현재 예약'}
        </option>
      `)
      .join('');

    timeSelect.innerHTML = options || '<option value="">가능한 시간이 없습니다</option>';
    timeSelect.disabled = options.length === 0;
    message.textContent = '';
  } catch (error) {
    timeSelect.innerHTML = '<option value="">시간을 불러오지 못했습니다</option>';
    message.textContent = error.message;
  }
};

const hydrateCards = () => {
  document.querySelectorAll('.my-reservation-card').forEach((card) => {
    const reservation = currentReservations.find(item => String(item.id) === String(card.dataset.id));
    if (!reservation || getReservationStatus(reservation) === 'past') return;

    loadAvailableTimes(card, reservation);
  });
};

const renderReservations = ({ reservations = [], waitings = [] }, name) => {
  ownerTitle.textContent = `${name}님의 예약과 대기`;
  currentReservations = reservations;
  currentWaitings = waitings;

  if (reservations.length === 0 && waitings.length === 0) {
    renderEmpty(name);
    return;
  }

  const reservationSection = reservations.length > 0
    ? `
      <div class="my-list-section">
        <h3>예약</h3>
        ${reservations.map((reservation, index) => renderReservationCard(reservation, index)).join('')}
      </div>
    `
    : '';
  const waitingSection = waitings.length > 0
    ? `
      <div class="my-list-section">
        <h3>대기</h3>
        ${waitings.map((waiting, index) => renderWaitingCard(waiting, index)).join('')}
      </div>
    `
    : '';

  reservationList.innerHTML = reservationSection + waitingSection;
  hydrateCards();
};

const loadReservations = async (name) => {
  currentName = name.trim();
  if (!currentName) {
    setMessage('예약자 이름을 입력해주세요.');
    return;
  }

  setMessage('');
  setLoading();
  ownerTitle.textContent = `${currentName}님의 예약`;
  window.localStorage.setItem('roomzero.guestName', currentName);

  try {
    const result = await api.getMyReservations(currentName);
    renderReservations({
      reservations: result.reservations || [],
      waitings: result.waitings || []
    }, currentName);
  } catch (error) {
    reservationList.innerHTML = `
      <div class="my-empty">
        <strong>예약을 불러오지 못했습니다.</strong>
        <span>${escapeHtml(error.message)}</span>
      </div>
    `;
  }
};

const cancelWaiting = async (card) => {
  const id = card.dataset.id;
  const waiting = currentWaitings.find(item => String(item.id) === String(id));
  if (!waiting) return;

  if (!confirm(`${waiting.theme.name} 대기를 취소할까요?`)) return;

  const message = card.querySelector('.card-message');
  try {
    await api.cancelMyWaiting(id, currentName);
    await loadReservations(currentName);
  } catch (error) {
    message.dataset.type = 'error';
    message.textContent = error.message || '대기 취소에 실패했습니다.';
  }
};

const updateReservation = async (card) => {
  const id = card.dataset.id;
  const reservation = currentReservations.find(item => String(item.id) === String(id));
  const date = card.querySelector('.my-date').value;
  const timeId = card.querySelector('.my-time').value;
  const message = card.querySelector('.card-message');

  if (!date || !timeId) {
    message.textContent = '변경할 날짜와 시간을 선택해주세요.';
    return;
  }

  try {
    await api.updateMyReservation(id, { name: currentName, date, timeId });
    message.dataset.type = 'success';
    message.textContent = '예약이 변경되었습니다.';
    await loadReservations(currentName);
  } catch (error) {
    message.dataset.type = 'error';
    message.textContent = error.message || `${reservation?.theme?.name || '예약'} 변경에 실패했습니다.`;
  }
};

const cancelReservation = async (card) => {
  const id = card.dataset.id;
  const reservation = currentReservations.find(item => String(item.id) === String(id));
  if (!reservation) return;

  if (!confirm(`${reservation.theme.name} 예약을 취소할까요?`)) return;

  const message = card.querySelector('.card-message');
  try {
    await api.cancelMyReservation(id, currentName);
    await loadReservations(currentName);
  } catch (error) {
    message.dataset.type = 'error';
    message.textContent = error.message || '예약 취소에 실패했습니다.';
  }
};

nameForm.addEventListener('submit', (event) => {
  event.preventDefault();
  loadReservations(nameInput.value);
});

changeNameBtn.addEventListener('click', () => {
  nameInput.focus();
  nameInput.select();
});

reservationList.addEventListener('change', (event) => {
  const card = event.target.closest('.my-reservation-card');
  if (!card || !event.target.classList.contains('my-date')) return;

  const reservation = currentReservations.find(item => String(item.id) === String(card.dataset.id));
  if (reservation) loadAvailableTimes(card, reservation);
});

reservationList.addEventListener('click', (event) => {
  const card = event.target.closest('.my-reservation-card');
  if (!card) return;

  if (event.target.classList.contains('update-reservation')) {
    updateReservation(card);
  }

  if (event.target.classList.contains('cancel-reservation')) {
    cancelReservation(card);
  }

  if (event.target.classList.contains('cancel-waiting')) {
    cancelWaiting(card);
  }
});

const storedName = window.localStorage.getItem('roomzero.guestName') || '';
if (storedName) {
  nameInput.value = storedName;
  loadReservations(storedName);
}
