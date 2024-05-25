document.addEventListener('DOMContentLoaded', () => {
  const today = new Date();
  let startDate = formatDate(minusDay(today, 7));
  let endDate = formatDate(minusDay(today, 1));
  const count = 10;
  const endpoint = `/themes/ranking?start=${startDate}&end=${endDate}&count=${count}`;
  requestRead(endpoint) // 인기 테마 목록 조회 API endpoint
      .then(render)
      .catch(error => console.error('Error fetching times:', error));
});

function minusDay(date, minusValue) {
  return new Date(new Date(date).setDate(date.getDate() - minusValue));
}

function formatDate(date) {
  const year = date.getFullYear();
  const month = ('0' + (date.getMonth() + 1)).slice(-2);
  const day = ('0' + date.getDate()).slice(-2);
  return year + '-' + month + '-' + day;
}

function render(data) {
  const container = document.getElementById('theme-ranking');

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
