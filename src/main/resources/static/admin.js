// ===== admin.js — 관리자 페이지 전용 =====

const $ = id => document.getElementById(id);

function showToast(msg, type = 'default') {
  const el = document.createElement('div');
  el.className = `toast ${type}`;
  el.textContent = msg;
  $('toast-container').appendChild(el);
  setTimeout(() => el.remove(), 3100);
}

async function extractErrorMessage(res) {
  try {
    const text = await res.text();
    if (!text) return `요청이 실패했습니다. (${res.status})`;
    try {
      const json = JSON.parse(text);
      return json.message || json.detail || json.error || json.title
        || text || `요청이 실패했습니다. (${res.status})`;
    } catch {
      return text || `요청이 실패했습니다. (${res.status})`;
    }
  } catch {
    return `요청이 실패했습니다. (${res.status})`;
  }
}

const api = {
  async get(url) {
    const res = await fetch(url);
    if (!res.ok) {
      const msg = await extractErrorMessage(res);
      throw new Error(msg);
    }
    return res.json();
  },
  async post(url, body) {
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const msg = await extractErrorMessage(res);
      throw new Error(msg);
    }
    return res.json();
  },
  async del(url) {
    const res = await fetch(url, { method: 'DELETE' });
    if (!res.ok) {
      const msg = await extractErrorMessage(res);
      throw new Error(msg);
    }
    return res;
  },
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
  if (panel === 'waitings') loadWaitings();
  if (panel === 'times') loadTimes();
  if (panel === 'themes') loadThemes();
}

// ===== Reservations =====
async function loadReservations() {
  const tbody = $('admin-reservations-tbody');
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
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
}

async function deleteReservation(id) {
  if (!confirm('이 예약을 삭제하시겠습니까?')) return;
  try {
    await api.del(`/admin/reservations/${id}`);
    showToast('예약이 삭제되었습니다.', 'success');
    loadReservations();
  } catch (e) {
    showToast('삭제에 실패했습니다. ' + e.message, 'error');
  }
}

// ===== Waitings =====
async function loadWaitings() {
  const tbody = $('admin-waitings-tbody');
  tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  const data = await api.get('/admin/waitings');
  if (!data.length) {
    tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;padding:24px;color:var(--text-muted)">대기 내역이 없습니다.</td></tr>`;
    return;
  }
  tbody.innerHTML = data.map(w => `
    <tr>
      <td>${w.id}</td>
      <td>${w.customerName}</td>
      <td>${w.date}</td>
      <td>${formatTime(w.startAt)}</td>
      <td>${w.theme.name}</td>
      <td><span class="waiting-rank-badge">${w.rank}번째</span></td>
    </tr>
  `).join('');
}

// ===== Times =====
async function loadTimes() {
  const tbody = $('admin-times-tbody');
  tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  const data = await api.get('/times');
  if (!data.length) {
    tbody.innerHTML = `<tr><td colspan="3" style="text-align:center;padding:24px;color:var(--text-muted)">등록된 시간대가 없습니다.</td></tr>`;
    return;
  }
  tbody.innerHTML = data.map(t => `
    <tr>
      <td>${t.id}</td><td>${formatTime(t.startAt)}</td>
      <td><button class="btn-delete" onclick="deleteTime(${t.id})">삭제</button></td>
    </tr>
  `).join('');
}

async function addTime() {
  const startAt = $('new-time-start').value;
  if (!startAt) { showToast('시작 시간을 입력해주세요.', 'error'); return; }
  try {
    await api.post('/times', { startAt });
    showToast('시간대가 추가되었습니다.', 'success');
    $('new-time-start').value = '';
    loadTimes();
  } catch (e) {
    showToast('시간대 추가에 실패했습니다. ' + e.message, 'error');
  }
}

async function deleteTime(id) {
  if (!confirm('이 시간대를 삭제하시겠습니까?')) return;
  try {
    await api.del(`/times/${id}`);
    showToast('시간대가 삭제되었습니다.', 'success');
    loadTimes();
  } catch (e) {
    showToast('삭제에 실패했습니다. ' + e.message, 'error');
  }
}

// ===== Themes =====
async function loadThemes() {
  const tbody = $('admin-themes-tbody');
  tbody.innerHTML = `<tr><td colspan="4" style="text-align:center;padding:24px;color:var(--text-muted)">불러오는 중...</td></tr>`;
  const data = await api.get('/themes/popular');
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
}

async function addTheme() {
  const name         = $('new-theme-name').value.trim();
  const description  = $('new-theme-desc').value.trim();
  const thumbnailUrl = $('new-theme-thumb').value.trim();
  if (!name || !description || !thumbnailUrl) { showToast('모든 항목을 입력해주세요.', 'error'); return; }
  try {
    await api.post('/themes', { name, description, thumbnailUrl });
    showToast('테마가 추가되었습니다.', 'success');
    $('new-theme-name').value = ''; $('new-theme-desc').value = ''; $('new-theme-thumb').value = '';
    loadThemes();
  } catch (e) {
    showToast('테마 추가에 실패했습니다. ' + e.message, 'error');
  }
}

async function deleteTheme(id) {
  if (!confirm('이 테마를 삭제하시겠습니까?')) return;
  try {
    await api.del(`/themes/${id}`);
    showToast('테마가 삭제되었습니다.', 'success');
    loadThemes();
  } catch (e) {
    showToast('삭제에 실패했습니다. ' + e.message, 'error');
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
