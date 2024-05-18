document.addEventListener('DOMContentLoaded', () => {
    const date = new Date().toISOString().split('T')[0];
    const RANKED_THEMES_ENDPOINT = `/themes/rank?date=${date}`;

    requestRead(RANKED_THEMES_ENDPOINT)
        .then(render)
        .catch(error => console.error('Error fetching times:', error));
});

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
            response.text().then(text => {
                alert('ERROR! ' + text);
                throw new Error('Read failed');
            });
        });
}
