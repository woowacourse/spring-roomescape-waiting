const $ = (selector) => document.querySelector(selector);

function setMessage(message) {
  $("#message").textContent = message;
}

async function api(path, options = {}) {
  const { headers = {}, ...restOptions } = options;
  const response = await fetch(path, {
    headers: {
      "Content-Type": "application/json",
      ...headers
    },
    ...restOptions
  });
  if (!response.ok) {
    const errorData = await response.json().catch(() => null);
    const msg = errorData?.message || "요청 처리에 실패했습니다.";
    window.alert(msg);
    throw new Error(msg);
  }
  if (response.status === 204) return null;
  return response.json();
}

function renderReservations(reservations) {
  const root = $("#reservations");
  if (!reservations.length) {
    root.textContent = "예약 데이터가 없습니다.";
    return;
  }

  root.innerHTML = "";

  reservations.forEach((reservation) => {
    const row = document.createElement("div");
    row.className = "chips";
    row.innerHTML = `
      <span>${reservation.id}. [${reservation.theme?.name ?? "테마 없음"}] ${reservation.date} ${reservation.time.startAt} - ${reservation.name}</span>
      <button class="danger reservation-delete" data-id="${reservation.id}" type="button">삭제</button>
    `;
    root.appendChild(row);
  });
}

async function refresh() {
  const reservations = await api("/admin/reservations");
  renderReservations(reservations);
}

$("#loadReservations").addEventListener("click", async () => {
  try {
    await refresh();
    setMessage("전체 예약을 조회했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#reservations").addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-id]");
  if (!button) return;

  try {
    await api(`/admin/reservations/${button.dataset.id}`, { method: "DELETE" });
    await refresh();
    setMessage("예약을 삭제했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

refresh().then(() => setMessage("초기 로딩 완료")).catch((error) => setMessage(error.message));
