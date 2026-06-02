const $ = (selector) => document.querySelector(selector);

const query = new URLSearchParams(window.location.search);
const reservationId = query.get("id");
const cancelType = query.get("type") === "waiting" ? "waiting" : "reservation";

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

function initPage() {
  if (!reservationId) {
    $("#cancelReservation").disabled = true;
    $("#reservationInfo").textContent = "취소할 항목 번호가 없어 진행할 수 없습니다.";
    setMessage("잘못된 접근입니다. 사용자 예약 페이지에서 다시 시도해 주세요.");
    return false;
  }

  const label = cancelType === "waiting" ? "예약 대기" : "예약";
  $("#reservationInfo").textContent = `${label} 번호 #${reservationId} 를 취소합니다.`;
  return true;
}

$("#cancelForm").addEventListener("submit", async (event) => {
  event.preventDefault();

  if (!reservationId) return;

  const authName = $("#authName").value.trim();
  if (!authName) {
    setMessage("인증용 이름을 입력해 주세요.");
    return;
  }

  try {
    const path = cancelType === "waiting"
      ? `/reservation-waitings/${reservationId}`
      : `/reservations/${reservationId}`;

    await api(path, {
      method: "DELETE",
      headers: { Authorization: authName }
    });
    setMessage("취소되었습니다. 잠시 후 사용자 페이지로 이동합니다.");
    setTimeout(() => {
      window.location.href = "/index.html";
    }, 1200);
  } catch (error) {
    setMessage(error.message);
  }
});

initPage();
