document.addEventListener('DOMContentLoaded', () => {
  const today = new Date();
  const eightDaysAgo = new Date(today);
  eightDaysAgo.setDate(today.getDate() - 8);
  const oneDayAgo = new Date(today);
  oneDayAgo.setDate(today.getDate() - 1);

  const from = formatDate(eightDaysAgo);
  const until = formatDate(oneDayAgo);
  const limit = 10;
    requestRead(`/themes/popular?from=${from}&until=${until}&limit=${limit}`) // 인기 테마 목록 조회 API endpoint
        .then(render)
        .catch(error => console.error('Error fetching times:', error));
});

function formatDate(value) {
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
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
