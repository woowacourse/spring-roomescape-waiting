const POPULAR_API = '/themes/top';

document.addEventListener('DOMContentLoaded', fetchPopularThemes);

function fetchPopularThemes() {
  apiFetch(POPULAR_API)
    .then(renderPopularThemes)
    .catch(showError);
}

function renderPopularThemes(themes) {
  const grid = document.getElementById('popular-grid');
  const empty = document.getElementById('popular-empty');
  grid.innerHTML = '';

  if (!themes || themes.length === 0) {
    empty.classList.remove('d-none');
    return;
  }
  empty.classList.add('d-none');

  themes.forEach((theme, index) => grid.appendChild(buildPopularCard(theme, index + 1)));
}

function buildPopularCard(theme, rank) {
  const card = document.createElement('a');
  card.className = 'theme-card';
  card.href = '/reservation';

  const thumb = document.createElement('div');
  thumb.className = 'thumb';
  if (theme.imageUrl) {
    thumb.style.backgroundImage = `url('${theme.imageUrl}')`;
  }
  const badge = document.createElement('div');
  badge.className = 'rank-badge';
  badge.textContent = rank;
  thumb.appendChild(badge);
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
