document.addEventListener('DOMContentLoaded', () => {
    const today = new Date();

    // 오늘 날짜에서 7일을 빼서 새로운 Date 객체를 만듭니다.
    const startDay = new Date(today);
    startDay.setDate(today.getDate() - 7);

    // 오늘 날짜에서 1일을 빼서 새로운 Date 객체를 만듭니다.
    const endDay = new Date(today);
    endDay.setDate(today.getDate() - 1);

    requestRead(`/themes/top?count=10&startAt=${formatDate(startDay)}&endAt=${formatDate(endDay)}`) // 인기 테마 목록 조회 API endpoint
        .then(render)
        .catch(error => console.error('Error fetching times:', error));
});

function formatDate(dateString) {
    let date = new Date(dateString);
    let year = date.getFullYear();
    let month = (date.getMonth() + 1).toString().padStart(2, '0'); // '04'
    let day = date.getDate().toString().padStart(2, '0'); // '28'

    return `${year}-${month}-${day}`; // '2024-04-28'
}

function render(data) {
    const container = document.getElementById('theme-ranking');
    data.data.themes.forEach(theme => {
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
