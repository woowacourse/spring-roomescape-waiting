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

function renderThemes(themes) {
  const root = $("#themes");
  root.innerHTML = "";
  themes.forEach((theme) => {
    const card = document.createElement("article");
    card.className = "card";
    card.innerHTML = `
      <img src="${theme.thumbnailUrl}" alt="${theme.name}" />
      <div class="card-body">
        <h3>${theme.name}</h3>
        <p>${theme.description}</p>
        <button class="danger" data-id="${theme.id}">삭제</button>
      </div>
    `;
    root.appendChild(card);
  });
}

async function refresh() {
  const themes = await api("/themes");
  renderThemes(themes);
}

$("#themeForm").addEventListener("submit", async (event) => {
  event.preventDefault();
  const form = new FormData(event.target);
  try {
    await api("/admin/themes", {
      method: "POST",
      body: JSON.stringify({
        name: form.get("name"),
        description: form.get("description"),
        thumbnailUrl: form.get("thumbnailUrl")
      })
    });
    event.target.reset();
    await refresh();
    setMessage("테마를 추가했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

$("#themes").addEventListener("click", async (event) => {
  const button = event.target.closest("button[data-id]");
  if (!button) return;
  try {
    await api(`/admin/themes/${button.dataset.id}`, { method: "DELETE" });
    await refresh();
    setMessage("테마를 삭제했습니다.");
  } catch (error) {
    setMessage(error.message);
  }
});

refresh().then(() => setMessage("초기 로딩 완료")).catch((error) => setMessage(error.message));
