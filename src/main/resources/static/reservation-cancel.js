const $ = (selector) => document.querySelector(selector);

const query = new URLSearchParams(window.location.search);
const reservationId = query.get("id");
const status = query.get("status") || "reserved";

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
    const errorData = await response.json().catch(() => null);
    const msg = errorData?.message || "요청 처리에 실패했습니다.";
    window.alert(msg);
    throw new Error(msg);
  }

  if (response.status === 204) return null;
  return response.json();
}

function initPage() {
  if (!reservationId) {
    $("#cancelReservation").disabled = true;
    $("#reservationInfo").textContent = "예약 번호가 없어 취소를 진행할 수 없습니다.";
    setMessage("잘못된 접근입니다. 사용자 예약 페이지에서 다시 시도해 주세요.");
    return false;
  }

  $("#reservationInfo").textContent = `예약 번호 #${reservationId} 를 취소합니다.`;
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
    const endpoint = status === "waiting" 
      ? `/reservations-waitings/${reservationId}` 
      : `/reservations/${reservationId}`;

    await api(endpoint, {
      method: "DELETE",
      headers: { Authorization: authName }
    });
    setMessage("정상적으로 취소되었습니다. 잠시 후 사용자 페이지로 이동합니다.");
    setTimeout(() => {
      window.location.href = "/index.html";
    }, 1200);
  } catch (error) {
    setMessage(error.message);
  }
});

initPage();
