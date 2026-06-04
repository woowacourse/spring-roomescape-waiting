import { api, getNextThemeThumbnail, getThemeThumbnail } from './api.js';

const themeList = document.getElementById('theme-list');
const timeList = document.getElementById('time-list');
const reservationList = document.getElementById('reservation-list');
const adminStats = document.getElementById('admin-stats');
const reservationChartCanvas = document.getElementById('reservation-chart');
const timeDensityMap = document.getElementById('time-density-map');
const chartRangeLabel = document.getElementById('chart-range-label');

let reservationChart = null;
let lastDashboardData = { themes: [], times: [], reservations: [] };

const escapeHtml = (value) => String(value ?? '')
  .replaceAll('&', '&amp;')
  .replaceAll('<', '&lt;')
  .replaceAll('>', '&gt;')
  .replaceAll('"', '&quot;')
  .replaceAll("'", '&#039;');

const escapeCssUrl = (value) => String(value ?? '').replaceAll("'", '%27');

const getThemeLabel = (reservation) => reservation.theme?.name || '미지정';
const getTimeLabel = (reservation) => reservation.time?.startAt || '--:--';

const countBy = (items, getKey) => items.reduce((map, item) => {
  const key = getKey(item);
  map.set(key, (map.get(key) || 0) + 1);
  return map;
}, new Map());

const renderReservationChart = ({ themes = [], reservations = [] } = {}) => {
  if (!reservationChartCanvas || !window.Chart) return;

  const themeCounts = countBy(reservations, getThemeLabel);
  const labels = themes.map(theme => theme.name);
  const counts = labels.map(label => themeCounts.get(label) || 0);
  const fallbackLabels = reservations.length > 0 ? [...themeCounts.keys()] : ['예약 없음'];
  const chartLabels = labels.length > 0 ? labels : fallbackLabels;
  const chartCounts = labels.length > 0 ? counts : fallbackLabels.map(label => themeCounts.get(label) || 0);
  const maxCount = Math.max(1, ...chartCounts);

  if (reservationChart) reservationChart.destroy();

  reservationChart = new window.Chart(reservationChartCanvas, {
    type: 'bar',
    data: {
      labels: chartLabels,
      datasets: [{
        label: '예약 수',
        data: chartCounts,
        borderRadius: 8,
        borderSkipped: false,
        backgroundColor: chartCounts.map((count) => {
          const opacity = 0.32 + (count / maxCount) * 0.54;
          return `rgba(215, 167, 104, ${opacity.toFixed(2)})`;
        }),
        borderColor: 'rgba(255, 250, 240, 0.2)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        duration: 900,
        easing: 'easeOutQuart'
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(9, 8, 7, 0.94)',
          borderColor: 'rgba(215, 167, 104, 0.38)',
          borderWidth: 1,
          titleColor: '#fffaf0',
          bodyColor: '#d7a768',
          displayColors: false
        }
      },
      scales: {
        x: {
          grid: { display: false },
          ticks: {
            color: 'rgba(255, 252, 244, 0.62)',
            font: { weight: 700 },
            maxRotation: 0,
            autoSkip: false,
            callback(value) {
              const label = this.getLabelForValue(value);
              return label.length > 7 ? `${label.slice(0, 7)}...` : label;
            }
          }
        },
        y: {
          beginAtZero: true,
          ticks: {
            precision: 0,
            color: 'rgba(255, 252, 244, 0.5)'
          },
          grid: { color: 'rgba(255, 252, 244, 0.08)' }
        }
      }
    }
  });

  if (chartRangeLabel) {
    const dateLabels = [...new Set(reservations.map(reservation => reservation.date))].sort();
    chartRangeLabel.textContent = dateLabels.length > 0
      ? `${dateLabels[0]} - ${dateLabels[dateLabels.length - 1]}`
      : '예약 없음';
  }

  window.__dashboardChartReady = true;
};

const renderTimeDensity = ({ times = [], reservations = [] } = {}) => {
  if (!timeDensityMap || !window.d3) return;

  timeDensityMap.innerHTML = '';

  if (times.length === 0) {
    timeDensityMap.innerHTML = '<div class="density-empty">예약 시간이 없습니다.</div>';
    return;
  }

  const d3 = window.d3;
  const width = Math.max(timeDensityMap.clientWidth || 360, 320);
  const height = 286;
  const margin = { top: 18, right: 12, bottom: 34, left: 50 };
  const dates = [...new Set(reservations.map(reservation => reservation.date))].sort().slice(-5);
  const dayLabels = dates.length > 0 ? dates : [new Date().toISOString().split('T')[0]];
  const density = new Map();

  reservations.forEach((reservation) => {
    const key = `${reservation.date}|${getTimeLabel(reservation)}`;
    density.set(key, (density.get(key) || 0) + 1);
  });

  const x = d3.scaleBand()
    .domain(dayLabels)
    .range([margin.left, width - margin.right])
    .padding(0.12);
  const y = d3.scaleBand()
    .domain(times.map(time => time.startAt))
    .range([margin.top, height - margin.bottom])
    .padding(0.16);
  const maxDensity = Math.max(1, ...density.values());
  const color = d3.scaleLinear()
    .domain([0, maxDensity])
    .range(['rgba(255, 252, 244, 0.08)', 'rgba(215, 167, 104, 0.9)']);

  const svg = d3.select(timeDensityMap)
    .append('svg')
    .attr('viewBox', `0 0 ${width} ${height}`)
    .attr('role', 'img')
    .attr('aria-label', '날짜와 시간별 예약 밀도');

  svg.selectAll('text.time-label')
    .data(times)
    .join('text')
    .attr('class', 'density-axis')
    .attr('x', margin.left - 12)
    .attr('y', time => y(time.startAt) + y.bandwidth() / 2 + 4)
    .attr('text-anchor', 'end')
    .text(time => time.startAt);

  svg.selectAll('text.day-label')
    .data(dayLabels)
    .join('text')
    .attr('class', 'density-axis')
    .attr('x', date => x(date) + x.bandwidth() / 2)
    .attr('y', height - 10)
    .attr('text-anchor', 'middle')
    .text(date => date.slice(5));

  const cells = dayLabels.flatMap(date => times.map(time => ({
    date,
    time: time.startAt,
    count: density.get(`${date}|${time.startAt}`) || 0
  })));

  svg.selectAll('rect.density-cell')
    .data(cells)
    .join('rect')
    .attr('class', 'density-cell')
    .attr('x', cell => x(cell.date))
    .attr('y', cell => y(cell.time))
    .attr('width', x.bandwidth())
    .attr('height', y.bandwidth())
    .attr('rx', 7)
    .attr('fill', cell => color(cell.count))
    .attr('opacity', cell => cell.count > 0 ? 1 : 0.62)
    .append('title')
    .text(cell => `${cell.date} ${cell.time}: ${cell.count}건`);

  window.__dashboardMapReady = true;
};

const renderDashboard = (data) => {
  lastDashboardData = data;
  renderReservationChart(data);
  renderTimeDensity(data);
};

const renderStats = ({ themes = [], times = [], reservations = [] } = {}) => {
  if (!adminStats) return;

  adminStats.innerHTML = `
    <div class="stat-cell">
      <span>Rooms</span>
      <strong>${themes.length}</strong>
    </div>
    <div class="stat-cell">
      <span>Slots</span>
      <strong>${times.length}</strong>
    </div>
    <div class="stat-cell">
      <span>Bookings</span>
      <strong>${reservations.length}</strong>
    </div>
  `;
};

const loadStats = async () => {
  try {
    const [themes, times, reservations] = await Promise.all([
      api.getThemes(),
      api.getTimes(),
      api.getReservations()
    ]);
    renderStats({ themes, times, reservations });
    renderDashboard({ themes, times, reservations });
  } catch (error) {
    renderStats();
    renderDashboard();
  }
};

const loadThemes = async () => {
  try {
    const themes = await api.getThemes();

    if (themes.length === 0) {
      themeList.innerHTML = '<div class="item muted">등록된 테마가 없습니다.</div>';
      return;
    }

    themeList.innerHTML = themes.map((theme, index) => {
      const thumbnail = getThemeThumbnail(theme, index);
      return `
        <div class="item">
          <div class="item-thumbnail" style="background-image: url('${escapeCssUrl(thumbnail)}')"></div>
          <div class="item-info">
            <span class="item-name">${escapeHtml(theme.name)}</span>
            <span class="item-meta">${escapeHtml(theme.description)}</span>
            <span class="item-meta">${escapeHtml(thumbnail)}</span>
          </div>
          <button class="btn-delete" data-id="${theme.id}" data-type="theme">삭제</button>
        </div>
      `;
    }).join('');
  } catch (error) {
    themeList.innerHTML = `<div class="item muted">${escapeHtml(error.message)}</div>`;
  }
};

const loadTimes = async () => {
  try {
    const times = await api.getTimes();

    if (times.length === 0) {
      timeList.innerHTML = '<div class="item muted">등록된 시간이 없습니다.</div>';
      return;
    }

    timeList.innerHTML = times.map(time => `
      <div class="item">
        <div class="item-info">
          <span class="item-name">${escapeHtml(time.startAt)}</span>
        </div>
        <button class="btn-delete" data-id="${time.id}" data-type="time">삭제</button>
      </div>
    `).join('');
  } catch (error) {
    timeList.innerHTML = `<div class="item muted">${escapeHtml(error.message)}</div>`;
  }
};

const loadReservations = async () => {
  try {
    const reservations = await api.getReservations();

    if (reservations.length === 0) {
      reservationList.innerHTML = '<div class="item muted">예약 내역이 없습니다.</div>';
      return;
    }

    reservationList.innerHTML = reservations.map(reservation => `
      <div class="item">
        <div class="item-info">
          <span class="item-name">${escapeHtml(reservation.name)} · ${escapeHtml(reservation.theme?.name)}</span>
          <span class="item-meta">${escapeHtml(reservation.date)} ${escapeHtml(reservation.time?.startAt)}</span>
        </div>
        <button class="btn-delete" data-id="${reservation.id}" data-type="reservation">삭제</button>
      </div>
    `).join('');
  } catch (error) {
    reservationList.innerHTML = `<div class="item muted">${escapeHtml(error.message)}</div>`;
  }
};

document.getElementById('theme-name').addEventListener('input', (event) => {
  const thumbnailInput = document.getElementById('theme-thumbnail');
  thumbnailInput.placeholder = getNextThemeThumbnail(event.target.value || '방탈출');
});

document.getElementById('add-theme-btn').addEventListener('click', async () => {
  const name = document.getElementById('theme-name').value.trim();
  const description = document.getElementById('theme-description').value.trim();
  const thumbnail = document.getElementById('theme-thumbnail').value.trim() || getNextThemeThumbnail(name);

  if (!name || !description) {
    alert('테마 이름과 설명을 입력해주세요.');
    return;
  }

  try {
    await api.createTheme({ name, description, thumbnail });
    document.getElementById('theme-name').value = '';
    document.getElementById('theme-description').value = '';
    document.getElementById('theme-thumbnail').value = '';
    loadThemes();
    loadStats();
  } catch (error) {
    alert(error.message);
  }
});

document.getElementById('add-time-btn').addEventListener('click', async () => {
  const startAt = document.getElementById('time-start').value;

  if (!startAt) {
    alert('추가할 시간을 선택해주세요.');
    return;
  }

  try {
    await api.createTime(startAt);
    document.getElementById('time-start').value = '';
    loadTimes();
    loadStats();
  } catch (error) {
    alert(error.message);
  }
});

document.addEventListener('click', async (e) => {
  if (e.target.classList.contains('btn-delete')) {
    const id = e.target.dataset.id;
    const type = e.target.dataset.type;
    const typeName = {
      theme: '테마',
      time: '시간',
      reservation: '예약'
    }[type] || '항목';

    if (confirm(`이 ${typeName}을 삭제할까요?`)) {
      try {
        if (type === 'theme') {
          await api.deleteTheme(id);
          loadThemes();
          loadStats();
        } else if (type === 'reservation') {
          await api.deleteReservation(id);
          loadReservations();
          loadStats();
        } else {
          await api.deleteTime(id);
          loadTimes();
          loadStats();
        }
      } catch (error) {
        alert(error.message);
      }
    }
  }
});

loadThemes();
loadTimes();
loadReservations();
loadStats();

window.addEventListener('resize', () => {
  window.clearTimeout(window.__dashboardResizeTimer);
  window.__dashboardResizeTimer = window.setTimeout(() => renderTimeDensity(lastDashboardData), 160);
});
