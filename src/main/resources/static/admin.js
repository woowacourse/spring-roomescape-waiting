// ===== admin.js — 관리자 페이지 전용 =====

const $ = id => document.getElementById(id);

// ===== Error Code Mapping =====
const ERROR_MESSAGES = {
  'TIME_HAS_RESERVATION': '해당 시간대에 예약된 내역이 있어 삭제할 수 없습니다.',
  'THEME_HAS_RESERVATION': '해당 테마에 예약된 내역이 있어 삭제할 수 없습니다.',
  'INVALID_INPUT_VALUE': '입력 정보가 올바르지 않습니다. 다시 한 번 확인해 주세요.',
  'START_TIME_NULL': '시작 시간을 입력해 주세요.',
  'END_TIME_NULL': '종료 시간을 입력해 주세요.',
  'THEME_NAME_NULL_OR_BLANK': '테마 이름을 입력해 주세요.',
  'DESCRIPTION_NULL_OR_BLANK': '테마 설명을 입력해 주세요.',
  'THUMBNAIL_URL_NULL_OR_BLANK': '테마 썸네일 URL을 입력해 주세요.',
  'DEFAULT': '알 수 없는 오류가 발생했습니다. 문제가 지속되면 시스템 관리자에게 문의해주세요.'
};

function showToast(msg, type = 'default') {
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.textContent = msg;
  $('toast-container').appendChild(el);
  setTimeout(() => el.remove(), 3100);
}

// ===== API =====
const api = {
  async request(url, options) {
    const res = await fetch(url, options);

    if (!res.ok) {
      try {
        const errorData = await res.json();
        const errorCode = errorData.code || 'DEFAULT';
        const errorMessage = ERROR_MESSAGES[errorCode] || ERROR_MESSAGES['DEFAULT'];
        throw new Error(errorMessage);
      } catch (e) {
        throw new Error(e.message || ERROR_MESSAGES['DEFAULT']);
      }
    }

    if (res.status === 204) return null;
    return res.json();
  },

  get(url) {
    return this.request(url);
  },

  post(url, body) {
    return this.request(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
  },

  del(url) {
    return this.request(url, { method: 'DELETE' });
  }
};

function formatTime(t) {
  if (!t) return '';
  if (Array.isArray(t)) return `${String(t[0]).padStart(2,'0')}:${String(t[1]).padStart(2,'0')}`;
  return t.substring(0, 5);
}

// ===== Panel switching =====
function switchPanel(panel) {
  document.querySelectorAll('.admin-panel').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.admin-tab').forEach(t => { t.classList.remove('active'); t.setAttribute('aria-selected', 'false'); });
  $(`admin-panel-${panel}`).classList.add('active');
  const tab = document.querySelector(`[data-panel="${panel}"]`);
  if (tab) { tab.classList.add('active'); tab.setAttribute('aria-selected', 'true'); }

  if (panel === 'reservations') loadReservations();
  if (panel === 'times') loadTimes();
  if (panel === 'themes') loadThemes();
}

// ===== Reservations =====
async function loadReservations() {
  const tbody = $('admin-reservations-tbody');
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  try {
    const data = await api.get('/admin/reservations');
    if (!data.length) {
      tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">예약 내역이 없습니다.</td></tr>`;
      return;
    }
    tbody.innerHTML = data.map(r => `
      <tr>
        <td>${r.id}</td><td>${r.name}</td><td>${r.date}</td>
        <td>${formatTime(r.time.startAt)}</td><td>${r.theme.name}</td>
        <td><button class="btn-delete" onclick="deleteReservation(${r.id})">삭제</button></td>
      </tr>
    `).join('');
  } catch (e) {
    showToast(e.message, 'error');
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">예약 목록을 불러오지 못했습니다.</td></tr>`;
  }
}

async function deleteReservation(id) {
  if (!confirm('정말 이 예약을 삭제하시겠습니까? 삭제된 예약은 복구할 수 없습니다.')) return;
  try {
    await api.del(`/admin/reservations/${id}`);
    showToast('예약 내역이 성공적으로 삭제되었습니다.', 'success');
    loadReservations();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

// ===== Times =====
async function loadTimes() {
  const tbody = $('admin-times-tbody');
  tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  try {
    const data = await api.get('/times');
    if (!data.length) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">등록된 시간대가 없습니다.</td></tr>`;
      return;
    }
    tbody.innerHTML = data.map(t => `
      <tr>
        <td>${t.id}</td><td>${formatTime(t.startAt)}</td><td>${formatTime(t.endAt)}</td>
        <td><button class="btn-delete" onclick="deleteTime(${t.id})">삭제</button></td>
      </tr>
    `).join('');
  } catch (e) {
    showToast(e.message, 'error');
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">시간대 목록을 불러오지 못했습니다.</td></tr>`;
  }
}

async function addTime() {
  const startAt = $('new-time-start').value;
  const endAt = $('new-time-end').value;

  if (!startAt) {
    showToast(ERROR_MESSAGES['START_TIME_NULL'], 'error');
    return;
  }
  if (!endAt) {
    showToast(ERROR_MESSAGES['END_TIME_NULL'], 'error');
    return;
  }

  try {
    await api.post('/times', { startAt, endAt });
    showToast('새로운 시간대가 성공적으로 추가되었습니다.', 'success');
    $('new-time-start').value = '';
    $('new-time-end').value = '';
    loadTimes();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function deleteTime(id) {
  if (!confirm('정말 이 시간대를 삭제하시겠습니까? 관련된 설정에 영향을 미칠 수 있습니다.')) return;
  try {
    await api.del(`/times/${id}`);
    showToast('시간대가 성공적으로 삭제되었습니다.', 'success');
    loadTimes();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

// ===== Themes =====
async function loadThemes() {
  const tbody = $('admin-themes-tbody');
  tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  try {
    const data = await api.get('/themes');
    if (!data.length) {
      tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">등록된 테마가 없습니다.</td></tr>`;
      return;
    }
    tbody.innerHTML = data.map(t => `
      <tr>
        <td>${t.id}</td>
        <td><img style="width:28px;height:28px;border-radius:4px;object-fit:cover;vertical-align:middle;margin-right:8px;background:var(--surface-3)" src="${t.thumbnailUrl}" alt="${t.name}" onerror="this.style.display='none'">${t.name}</td>
        <td style="max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">${t.description}</td>
        <td><button class="btn-delete" onclick="deleteTheme(${t.id})">삭제</button></td>
      </tr>
    `).join('');
  } catch (e) {
    showToast(e.message, 'error');
    tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">테마 목록을 불러오지 못했습니다.</td></tr>`;
  }
}

async function addTheme() {
  const name         = $('new-theme-name').value.trim();
  const description  = $('new-theme-desc').value.trim();
  const thumbnailUrl = $('new-theme-thumb').value.trim();
  if (!name) { showToast(ERROR_MESSAGES['THEME_NAME_NULL_OR_BLANK'], 'error'); return; }
  if (!description) { showToast(ERROR_MESSAGES['DESCRIPTION_NULL_OR_BLANK'], 'error'); return; }
  if (!thumbnailUrl) { showToast(ERROR_MESSAGES['THUMBNAIL_URL_NULL_OR_BLANK'], 'error'); return; }
  
  try {
    await api.post('/themes', { name, description, thumbnailUrl });
    showToast('새로운 테마가 성공적으로 추가되었습니다.', 'success');
    $('new-theme-name').value = ''; $('new-theme-desc').value = ''; $('new-theme-thumb').value = '';
    loadThemes();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

async function deleteTheme(id) {
  if (!confirm('정말 이 테마를 삭제하시겠습니까? 관련된 설정에 영향을 미칠 수 있습니다.')) return;
  try {
    await api.del(`/themes/${id}`);
    showToast('테마가 성공적으로 삭제되었습니다.', 'success');
    loadThemes();
  } catch (e) {
    showToast(e.message, 'error');
  }
}

// ===== Init =====
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.admin-tab').forEach(btn => {
    btn.addEventListener('click', () => switchPanel(btn.dataset.panel));
  });
  $('btn-add-time').addEventListener('click', addTime);
  $('btn-add-theme').addEventListener('click', addTheme);

  loadReservations(); // 기본으로 예약 목록 로드
});
