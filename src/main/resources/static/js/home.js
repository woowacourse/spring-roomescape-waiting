const ALL_THEMES_API = '/admin/themes';

document.addEventListener('DOMContentLoaded', loadAllThemes);

function loadAllThemes() {
  fetch(ALL_THEMES_API)
    .then(res => res.json())
    .then(renderAllThemes)
    .catch(err => console.error('테마 조회 실패:', err));
}

function renderAllThemes(themes) {
  const grid = document.getElementById('all-themes');
  const empty = document.getElementById('all-themes-empty');
  grid.innerHTML = '';

  if (!themes || themes.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  themes.forEach(theme => grid.appendChild(buildThemeCard(theme)));
}

function buildThemeCard(theme) {
  const card = document.createElement('a');
  card.className = 'theme-card';
  card.href = '/reservation';

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
  card.appendChild(body);

  return card;
}