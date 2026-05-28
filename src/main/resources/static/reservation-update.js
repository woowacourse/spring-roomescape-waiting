const $ = (selector) => document.querySelector(selector);

const query = new URLSearchParams(window.location.search);
const reservationId = query.get("id");
const themeId = query.get("themeId");
let selectedTimeId = null;

function setMessage(message) {
  $("#message").textContent = message;
}

async function api(path, options = {}) {
  const { headers = {}, ...restOptions } = options;
  const mergedHeaders = {
    "Content-Type": "application/json",
    ...headers
  };

  const response = await fetch(path, {
    headers: mergedHeaders,
    ...restOptions
  });

  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || "요청 처리에 실패했습니다.");
  }

  if (response.status === 204) return null;
  return response.json();
}

function renderAvailableTimes(times) {
  const root = $("#availableTimes");
  root.innerHTML = "";

  if (!times.length) {
    root.textContent = "변경 가능한 시간이 없습니다.";
    selectedTimeId = null;
    return;
  }

  times.forEach((time) => {
    if (time.alreadyBooked) return;
    
    const button = document.createElement("button");
    button.className = "chip";
    button.type = "button";
    button.dataset.timeId = time.id;
    button.textContent = time.startAt;
    if (selectedTimeId === time.id) {
      button.classList.add("chip-selected");
    }
    root.appendChild(button);
  });

  if (root.children.length === 0) {
    root.textContent = "변경 가능한 시간이 없습니다.";
    selectedTimeId = null;
  }
}

async function loadAvailableTimes() {
  const date = $("#updateDate").value;
  if (!date) {
    setMessage("변경할 날짜를 입력해 주세요.");
    return;
  }

  const times = await api(`/times/available-times?date=${encodeURIComponent(date)}&themeId=${encodeURIComponent(themeId)}`);
  selectedTimeId = null;
  renderAvailableTimes(times);
}

function initPage() {
  if (!reservationId || !themeId) {
    $("#submitUpdate").disabled = true;
    $("#loadTimes").disabled = true;
    $("#reservationInfo").textContent = "예약 번호 또는 테마 정보가 없어 변경을 진행할 수 없습니다.";
    setMessage("잘못된 접근입니다. 내 예약 조회에서 다시 시도해 주세요.");
    return false;
  }

  $("#reservationInfo").textContent = `예약 번호 #${reservationId} 변경 화면입니다.`;
  return true;
}

$("#loadTimes").addEventListener("click", async () => {
  try {
    await loadAvailableTimes();
    setMessage("변경 가능한 시간을 조회했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#availableTimes").addEventListener("click", (event) => {
  const button = event.target.closest("button[data-time-id]");
  if (!button) return;

  selectedTimeId = Number(button.dataset.timeId);
  document.querySelectorAll("#availableTimes .chip").forEach((chip) => {
    chip.classList.toggle("chip-selected", chip === button);
  });
});

$("#submitUpdate").addEventListener("click", async () => {
  if (!reservationId) return;

  const authName = $("#authName").value.trim();
  const date = $("#updateDate").value;
  if (!authName) {
    setMessage("인증용 이름을 입력해 주세요.");
    return;
  }
  if (!date) {
    setMessage("변경할 날짜를 입력해 주세요.");
    return;
  }
  if (!selectedTimeId) {
    setMessage("변경할 시간을 선택해 주세요.");
    return;
  }

  try {
    await api(`/reservations/${reservationId}`, {
      method: "PATCH",
      headers: { Authorization: authName },
      body: JSON.stringify({
        date,
        timeId: selectedTimeId
      })
    });
    setMessage("예약 변경이 완료되었습니다. 잠시 후 사용자 페이지로 이동합니다.");
    setTimeout(() => {
      window.location.href = "/index.html";
    }, 1200);
  } catch (error) {
    setMessage(error.message);
  }
});

initPage();
