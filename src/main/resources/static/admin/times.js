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
    throw new Error((await response.text()) || "요청 처리에 실패했습니다.");
  }
  if (response.status === 204) return null;
  return response.json();
}

function renderTimes(times) {
  const root = $("#times");
  root.innerHTML = "";
  times.forEach((time) => {
    const button = document.createElement("button");
    button.className = "danger";
    button.type = "button";
    button.dataset.id = time.id;
    button.textContent = `${time.startAt} 삭제`;
    root.appendChild(button);
  });
}

async function refresh() {
  const times = await api("/times");
  renderTimes(times);
}

$("#timeForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  try {
    await api("/admin/times", {
      method: "POST",
      body: JSON.stringify({
        startAt: form.get("startAt")
      })
    });
    event.target.reset();
    await refresh();
    setMessage("시간을 추가했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#times").addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-id]");
  if (!button) return;
  try {
    await api(`/admin/times/${button.dataset.id}`, { method: "DELETE" });
    await refresh();
    setMessage("시간을 삭제했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

refresh().then(() => setMessage("초기 로딩 완료")).catch((error) => setMessage(error.message));
