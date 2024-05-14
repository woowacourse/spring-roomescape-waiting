document.addEventListener('DOMContentLoaded', () => {
    /*
    TODO: [3단계] 인기 테마 - 인기 테마 목록 조회 API 호출
    */

    const currentDate = new Date();
    const endDate = currentDate.toISOString().split('T')[0];
    currentDate.setDate(currentDate.getDate() - 7);
    const startDate = currentDate.toISOString().split('T')[0];
    const size = 10;

    const apiUrl = `/themes/rank?startDate=${startDate}&endDate=${endDate}&size=${size}`;
    requestRead(apiUrl) // 인기 테마 목록 조회 API endpoint
        .then(render)
        .catch(error => console.error('Error fetching times:', error));
});

function render(response) {
    const data = response.data;
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
