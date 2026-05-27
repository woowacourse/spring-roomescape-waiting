document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("#name, #password").forEach(input => {
    input.addEventListener("keydown", event => {
      if (event.key === "Enter") {
        event.preventDefault();
        document.getElementById("login-button").click();
      }
    });
  });
});

async function login() {
  const name = document.getElementById("name").value;
  const password = document.getElementById("password").value;

  if (!name || !password) {
    alert("이름과 비밀번호를 입력해주세요.");
    return;
  }

  const response = await fetch("/login", {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({name, password})
  });

  if (response.ok) {
    const token = response.headers.get("Authorization");
    localStorage.setItem("token", token);
    location.href = "/admin-page";
  } else {
    alert("관리자 로그인에 실패했습니다. 정보를 확인해주세요.");
  }
}
