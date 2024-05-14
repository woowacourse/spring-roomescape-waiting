document.addEventListener('DOMContentLoaded', () => {
    /*
    TODO: [3단계] 인기 테마 - 인기 테마 목록 조회 API 호출
    */
    createPopularThemesRequest();
});

function createPopularThemesRequest() {
    const start = createDate(getDateAgo(7));
    const end = createDate(getDateAgo(1));
    const count = 10;

    fetchPopularThemes(start, end, count)
}

function getDateAgo(days) {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
}

function createDate(data) {
    const year = data.getFullYear();
    const month = ('0' + (data.getMonth() + 1)).slice(-2);
    const day = ('0' + data.getDate()).slice(-2);
    return year + '-' + month + '-' + day;
}

function fetchPopularThemes(start, end, count) {
    fetch(`/themes/popular?start-date=${start}&end-date=${end}&count=${count}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    }).then(response => {
        if (response.status === 200) return response.json();
        throw new Error('Read failed');
    }).then(render)
        .catch(error => console.error("Error fetching popular themes:", error));
}

function render(data) {
    const container = document.getElementById('theme-ranking');
    /*
    TODO: [3단계] 인기 테마 - 인기 테마 목록 조회 API 호출 후 렌더링
          response 명세에 맞춰 name, thumbnail, description 값 설정
    */
    data.forEach(theme => {
        const name = theme.name;
        const thumbnail = theme.thumbnail;
        const description = theme.description;
        const htmlContent = `
            <img class="mr-3 img-thumbnail" src="${thumbnail}" alt="${name}">
            <div class="media-body">
                <h5 class="mt-0 mb-1">${name}</h5>
                ${description}
            </div>
        `;

        const div = document.createElement('li');
        div.className = 'media my-4';
        div.innerHTML = htmlContent;

        container.appendChild(div);
    })
}

function requestRead(endpoint) {
    return fetch(endpoint)
        .then(response => {
            if (response.status === 200) return response.json();
            throw new Error('Read failed');
        });
}
