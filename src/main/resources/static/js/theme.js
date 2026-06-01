const THEME_API = '/admin/themes';

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('add-theme').addEventListener('click', openForm);
  document.getElementById('save-theme').addEventListener('click', saveTheme);
  document.getElementById('cancel-theme').addEventListener('click', closeForm);
  fetchThemes();
});

function fetchThemes() {
  fetch(THEME_API)
    .then(res => res.json())
    .then(renderThemes)
    .catch(err => console.error('테마 조회 실패:', err));
}

function renderThemes(themes) {
  const grid = document.getElementById('theme-grid');
  const empty = document.getElementById('theme-empty');
  grid.innerHTML = '';

  if (!themes || themes.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');
  themes.forEach(theme => grid.appendChild(buildAdminCard(theme)));
}

function buildAdminCard(theme) {
  const card = document.createElement('div');
  card.className = 'theme-card';
  card.dataset.id = theme.id;

  const thumb = document.createElement('div');
  thumb.className = 'thumb';
  if (theme.imageUrl) {
    thumb.style.backgroundImage = `url('${theme.imageUrl}')`;
  }
  card.appendChild(thumb);

  const body = document.createElement('div');
  body.className = 'body';
  const name = document.createElement('h3');
  name.className = 'name';
  name.textContent = theme.name;
  body.appendChild(name);
  const desc = document.createElement('p');
  desc.className = 'desc';
  desc.textContent = theme.description || '';
  body.appendChild(desc);

  const actions = document.createElement('div');
  actions.className = 'card-actions';
  const delBtn = document.createElement('button');
  delBtn.className = 'btn btn-danger';
  delBtn.innerHTML = '<i class="fas fa-trash"></i> 삭제';
  delBtn.addEventListener('click', () => deleteTheme(theme.id, card));
  actions.appendChild(delBtn);
  body.appendChild(actions);

  card.appendChild(body);
  return card;
}

function openForm() {
  document.getElementById('theme-form').classList.remove('d-none');
  document.getElementById('new-name').focus();
}

function closeForm() {
  document.getElementById('theme-form').classList.add('d-none');
  document.getElementById('new-name').value = '';
  document.getElementById('new-desc').value = '';
  document.getElementById('new-image').value = '';
}

function saveTheme() {
  const body = {
    name: document.getElementById('new-name').value.trim(),
    description: document.getElementById('new-desc').value.trim(),
    imageUrl: document.getElementById('new-image').value.trim()
  };
  if (!body.name || !body.description) {
    alert('이름과 설명을 입력해주세요.');
    return;
  }
  fetch(THEME_API, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify(body)
  })
    .then(res => {
      if (res.status === 201) return res.json();
      return res.json().then(b => { throw new Error(b.message || '테마 추가에 실패했습니다.'); });
    })
    .then(theme => {
      const grid = document.getElementById('theme-grid');
      document.getElementById('theme-empty').classList.add('d-none');
      grid.appendChild(buildAdminCard(theme));
      closeForm();
    })
    .catch(err => showToast(err.message));
}

function deleteTheme(id, cardEl) {
  if (!confirm('이 테마를 삭제하시겠습니까?')) return;
  fetch(`${THEME_API}/${id}`, {method: 'DELETE'})
    .then(res => {
      if (res.status === 204) {
        cardEl.remove();
        const grid = document.getElementById('theme-grid');
        if (!grid.children.length) document.getElementById('theme-empty').classList.remove('d-none');
        return;
      }
      return res.json().then(b => { throw new Error(b.message || '삭제에 실패했습니다.'); });
    })
    .catch(err => showToast(err.message));
}
