document.addEventListener('DOMContentLoaded', () => {
    /*
    TODO: [3단계] 인기 테마 - 인기 테마 목록 조회 API 호출
    */
    const today = new Date();
    const startDay = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);

    const startDate = createDate(today);
    const endDate = createDate(startDate);
    const limit = 10;

    requestRead(`/themes/popular?startDate=${startDate}&endDate=${endDate}&limit=${limit}`) // 인기 테마 목록 조회 API endpoint
        .then(render)
        .catch(error => console.error('Error fetching times:', error));
});

function createDate(input) {
    var dd = new Date(input);

    const year = dd.getFullYear();
    const month = ('0' + (dd.getMonth() + 1)).slice(-2);
    const day = ('0' + dd.getDate()).slice(-2);
    return `${year}-${month}-${day}`;
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
