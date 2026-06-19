const RESERVATION_API = '/bookings';
const THEME_API = '/themes/top/10';

const state = {
  date: null,
  themeId: null,
  themeName: null,
  timeId: null,
  timeText: null,
  mode: null, // 'reserve' | 'waiting'
};

document.addEventListener('DOMContentLoaded', () => {
  initCalendar();
  loadThemes();

  document.getElementById('confirm-booking').addEventListener('click', confirmBooking);
  document.getElementById('cancel-booking').addEventListener('click', clearBookingBar);
});

function initCalendar() {
  flatpickr('#calendar', {
    inline: true,
    minDate: 'today',
    dateFormat: 'Y-m-d',
    locale: 'ko',
    onChange: (selected, dateStr) => {
      state.date = dateStr;
      refreshTimes();
    }
  });
}

function loadThemes() {
  fetch(THEME_API)
    .then(res => res.json())
    .then(renderThemeList)
    .catch(err => console.error('테마 조회 실패:', err));
}

function renderThemeList(themes) {
  const list = document.getElementById('theme-list');
  const empty = document.getElementById('theme-empty');
  list.innerHTML = '';
  if (!themes || themes.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');
  themes.forEach(theme => {
    const li = document.createElement('li');
    li.className = 'theme-item';
    li.dataset.themeId = theme.id;
    li.dataset.themeName = theme.name;

    const thumb = document.createElement('div');
    thumb.className = 'theme-thumb';
    if (theme.imageUrl) {
      thumb.style.backgroundImage = `url('${theme.imageUrl}')`;
    }
    li.appendChild(thumb);

    const info = document.createElement('div');
    info.className = 'theme-info';
    const name = document.createElement('p');
    name.className = 'theme-name';
    name.textContent = theme.name;
    info.appendChild(name);
    if (theme.description) {
      const desc = document.createElement('p');
      desc.className = 'theme-desc';
      desc.textContent = theme.description;
      info.appendChild(desc);
    }
    li.appendChild(info);

    li.addEventListener('click', () => selectTheme(li));
    list.appendChild(li);
  });
}

function selectTheme(li) {
  document.querySelectorAll('#theme-list .theme-item').forEach(el => el.classList.remove('active'));
  li.classList.add('active');
  state.themeId = parseInt(li.dataset.themeId);
  state.themeName = li.dataset.themeName;
  refreshTimes();
}

function refreshTimes() {
  clearBookingBar();
  const list = document.getElementById('time-list');
  const hint = document.getElementById('time-hint');
  list.innerHTML = '';

  if (!state.date || !state.themeId) {
    list.classList.add('d-none');
    hint.classList.remove('d-none');
    hint.textContent = '날짜와 테마를 먼저 선택해주세요.';
    return;
  }

  Promise.all([
    fetch('/times').then(r => r.json()),
    fetch(`/times/available?date=${state.date}&themeId=${state.themeId}`).then(r => r.json())
  ])
    .then(([allTimes, availableTimes]) => renderTimes(allTimes, availableTimes))
    .catch(() => {
      hint.classList.remove('d-none');
      hint.textContent = '시간을 불러오지 못했습니다.';
    });
}

function renderTimes(allTimes, availableTimes) {
  const list = document.getElementById('time-list');
  const hint = document.getElementById('time-hint');
  list.innerHTML = '';

  if (!allTimes || allTimes.length === 0) {
    list.classList.add('d-none');
    hint.classList.remove('d-none');
    hint.textContent = '등록된 시간이 없습니다.';
    return;
  }

  hint.classList.add('d-none');
  list.classList.remove('d-none');

  const availableIds = new Set(availableTimes.map(t => t.id));

  allTimes.forEach(time => {
    const isAvailable = availableIds.has(time.id);
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.dataset.timeId = time.id;
    btn.dataset.timeText = time.startAt;

    if (isAvailable) {
      btn.className = 'time-btn';
      btn.textContent = formatTime(time.startAt);
      btn.addEventListener('click', () => selectTime(btn, 'reserve'));
    } else {
      btn.className = 'time-btn time-btn--waiting';
      btn.innerHTML = `${formatTime(time.startAt)}<span class="time-badge">대기</span>`;
      btn.addEventListener('click', () => selectTime(btn, 'waiting'));
    }

    list.appendChild(btn);
  });
}

function formatTime(value) {
  if (!value) return '';
  const [h, m] = value.split(':');
  return `${h}:${m}`;
}

function selectTime(btn, mode) {
  document.querySelectorAll('#time-list .time-btn').forEach(el => el.classList.remove('active'));
  btn.classList.add('active');
  state.timeId = parseInt(btn.dataset.timeId);
  state.timeText = btn.dataset.timeText;
  state.mode = mode;
  showBookingBar(mode);
}

function showBookingBar(mode) {
  const bar = document.getElementById('booking-bar');
  const confirmBtn = document.getElementById('confirm-booking');
  document.getElementById('booking-summary-text').textContent =
    `${state.date} · ${state.themeName} · ${formatTime(state.timeText)}`;
  if (mode === 'waiting') {
    confirmBtn.textContent = '대기 신청';
    confirmBtn.className = 'btn btn-secondary';
  } else {
    confirmBtn.innerHTML = '<i class="fas fa-check"></i> 예약 확정';
    confirmBtn.className = 'btn btn-primary';
  }
  bar.classList.remove('d-none');
}

function clearBookingBar() {
  const bar = document.getElementById('booking-bar');
  bar.classList.add('d-none');
  state.timeId = null;
  state.timeText = null;
  state.mode = null;
  document.querySelectorAll('#time-list .time-btn').forEach(el => el.classList.remove('active'));
}

function confirmBooking() {
  if (!state.date || !state.themeId || !state.timeId) {
    showToast('날짜·테마·시간을 모두 선택해주세요.');
    return;
  }
  submitBooking();
}

function submitBooking() {
  fetch(RESERVATION_API, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({date: state.date, timeId: state.timeId, themeId: state.themeId})
  })
    .then(res => {
      if (res.status === 201) return res.json();
      return res.json().then(body => { throw new Error(body.message || '신청에 실패했습니다.'); });
    })
    .then(booking => {
      if (booking.status === 'PAYMENT_PENDING') {
        window.location.href = booking.redirectUrl;
        return;
      }
      showToast(booking.message || '대기 신청이 완료되었습니다.', 'success');
      clearBookingBar();
      refreshTimes();
    })
    .catch(err => showToast(err.message));
}
