let isLoginMode = true;

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("#name, #password").forEach(input => {
        input.addEventListener("keydown", event => {
            if (event.key === "Enter") {
                event.preventDefault();
                document.getElementById("auth-button").click();
            }
        });
    });
});

function toggleAuthMode() {
    isLoginMode = !isLoginMode;
    const title = document.getElementById("auth-title");
    const button = document.getElementById("auth-button");
    const toggleText = document.getElementById("auth-toggle");

    if (isLoginMode) {
        title.textContent = "로그인";
        button.textContent = "로그인";
        toggleText.textContent = "회원이 아니신가요? 회원가입";
    } else {
        title.textContent = "회원가입";
        button.textContent = "회원가입";
        toggleText.textContent = "이미 회원이신가요? 로그인";
    }
}

async function handleAuth() {
    const name = document.getElementById("name").value;
    const password = document.getElementById("password").value;

    if (!name || !password) {
        alert("이름과 비밀번호를 입력해주세요.");
        return;
    }

    if (isLoginMode) {
        await login(name, password);
    } else {
        await register(name, password);
    }
}

async function login(name, password) {
    const response = await fetch("/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, password })
    });

    if (response.ok) {
        const token = response.headers.get("Authorization");
        localStorage.setItem("token", token);
        location.href = "/reservation";
    } else {
        alert("로그인에 실패했습니다.");
    }
}

async function register(name, password) {
    const response = await fetch("/member/members", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, password })
    });

    if (response.ok) {
        alert("회원가입이 완료되었습니다. 로그인해주세요.");
        toggleAuthMode();
    } else {
        alert("회원가입에 실패했습니다.");
    }
}
