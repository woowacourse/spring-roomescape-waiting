const API = '/themes/popular';

document.addEventListener('DOMContentLoaded', () => {
    refresh();
});

async function refresh() {
    try {
        const data = await fetchJson(API);
        render(data.themes);
    } catch (error) {
        console.error('인기 테마 조회 실패:', error);
        showEmpty('인기 테마를 불러오지 못했습니다.');
    }
}

function render(themes) {
    const podium = document.getElementById('popular-podium');
    const list = document.getElementById('popular-list');
    const empty = document.getElementById('popular-empty');
    podium.innerHTML = '';
    list.innerHTML = '';
    empty.hidden = true;
    podium.hidden = false;
    list.parentElement.hidden = false;

    if (!themes || themes.length === 0) {
        showEmpty('최근 1주일 동안 예약된 테마가 없습니다.');
        return;
    }

    const top3 = themes.slice(0, 3);
    const rest = themes.slice(3);

    top3.forEach((theme, index) => podium.appendChild(buildPodiumCard(theme, index + 1)));
    rest.forEach((theme, index) => list.appendChild(buildRankedRow(theme, index + 4)));

    if (rest.length === 0) {
        list.parentElement.hidden = true;
    }
}

function buildPodiumCard(theme, rank) {
    const card = document.createElement('article');
    card.className = `podium-card podium-rank-${rank}`;

    const media = document.createElement('div');
    media.className = 'podium-media';
    if (theme.thumbnailImageUrl) {
        const img = document.createElement('img');
        img.src = theme.thumbnailImageUrl;
        img.alt = theme.name;
        img.loading = 'lazy';
        img.onerror = () => { img.style.display = 'none'; };
        media.appendChild(img);
    }

    const badge = document.createElement('span');
    badge.className = 'podium-badge';
    badge.textContent = rank;
    media.appendChild(badge);

    const medal = document.createElement('span');
    medal.className = 'podium-medal';
    medal.textContent = ['🥇', '🥈', '🥉'][rank - 1];
    media.appendChild(medal);

    card.appendChild(media);

    const body = document.createElement('div');
    body.className = 'podium-body';

    const name = document.createElement('h3');
    name.className = 'podium-name';
    name.textContent = theme.name;
    body.appendChild(name);

    const desc = document.createElement('p');
    desc.className = 'podium-desc';
    desc.textContent = theme.description;
    body.appendChild(desc);

    card.appendChild(body);
    return card;
}

function buildRankedRow(theme, rank) {
    const row = document.createElement('li');
    row.className = 'ranked-row';

    const rankCell = document.createElement('span');
    rankCell.className = 'ranked-rank';
    rankCell.textContent = rank;
    row.appendChild(rankCell);

    const thumb = document.createElement('div');
    thumb.className = 'ranked-thumb';
    if (theme.thumbnailImageUrl) {
        const img = document.createElement('img');
        img.src = theme.thumbnailImageUrl;
        img.alt = theme.name;
        img.loading = 'lazy';
        img.onerror = () => { img.style.display = 'none'; };
        thumb.appendChild(img);
    }
    row.appendChild(thumb);

    const info = document.createElement('div');
    info.className = 'ranked-info';

    const name = document.createElement('h4');
    name.className = 'ranked-name';
    name.textContent = theme.name;
    info.appendChild(name);

    const desc = document.createElement('p');
    desc.className = 'ranked-desc';
    desc.textContent = theme.description;
    info.appendChild(desc);

    row.appendChild(info);
    return row;
}

function showEmpty(message) {
    const podium = document.getElementById('popular-podium');
    const list = document.getElementById('popular-list');
    const empty = document.getElementById('popular-empty');
    podium.innerHTML = '';
    list.innerHTML = '';
    podium.hidden = true;
    list.parentElement.hidden = true;
    empty.textContent = message;
    empty.hidden = false;
}
