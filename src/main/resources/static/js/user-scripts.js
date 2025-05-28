document.addEventListener('DOMContentLoaded', function () {
    updateUIBasedOnLogin();
});

document.getElementById('logout-btn').addEventListener('click', function (event) {
    event.preventDefault();
    fetch('/logout', {
        method: 'POST', // 또는 서버 설정에 따라 GET 일 수도 있음
        credentials: 'include' // 쿠키를 포함시키기 위해 필요
    })
        .then(response => {
            if (response.ok) {
                // 로그아웃 성공, 페이지 새로고침 또는 리다이렉트
                window.location.reload();
            } else {
                // 로그아웃 실패 처리
                console.error('Logout failed');
            }
        })
        .catch(error => {
            console.error('Error:', error);
        });
});

function updateUIBasedOnLogin() {
    fetch('/login/check', {
        credentials: 'include'
    })
        .then(response => {
            if (!response.ok) { // 요청이 실패하거나 로그인 상태가 아닌 경우
                throw new Error('Not logged in or other error');
            }
            return response.json(); // 응답 본문을 JSON으로 파싱
        })
        .then(data => {
            // 응답에서 사용자 이름을 추출하여 UI 업데이트
            document.getElementById('profile-name').textContent = data.name; // 프로필 이름 설정
            document.querySelector('.nav-item.dropdown').style.display = 'block'; // 드롭다운 메뉴 표시
            document.querySelector('.nav-item a[href="/login"]').parentElement.style.display = 'none'; // 로그인 버튼 숨김
        })
        .catch(error => {
            // 에러 처리 또는 로그아웃 상태일 때 UI 업데이트
            console.error('Error:', error);
            document.getElementById('profile-name').textContent = 'Profile'; // 기본 텍스트로 재설정
            document.querySelector('.nav-item.dropdown').style.display = 'none'; // 드롭다운 메뉴 숨김
            document.querySelector('.nav-item a[href="/login"]').parentElement.style.display = 'block'; // 로그인 버튼 표시
        });
}

// 드롭다운 메뉴 토글
document.getElementById("navbarDropdown").addEventListener('click', function (e) {
    e.preventDefault();
    const dropdownMenu = e.target.closest('.nav-item.dropdown').querySelector('.dropdown-menu');
    dropdownMenu.classList.toggle('show'); // Bootstrap 4에서는 data-toggle 사용, Bootstrap 5에서는 JS로 처리
});


function login() {
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    // 입력 필드 검증
    if (!email || !password) {
        alert('Please fill in all fields.');
        return; // 필수 입력 필드가 비어있으면 여기서 함수 실행을 중단
    }

    fetch('/signin', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        credentials: 'include', // 필수
        body: JSON.stringify({
            email: email,
            password: password
        })
    })
        .then(response => {
            if (200 === !response.status) {
                alert('Login failed'); // 로그인 실패 시 경고창 표시
                throw new Error('Login failed');
            }
        })
        .then(() => {
            updateUIBasedOnLogin(); // UI 업데이트
            window.location.href = '/';
        })
        .catch(error => {
            console.error('Error during login:', error);
        });
}

function signup() {
    // Redirect to signup page
    window.location.href = '/signup';
}

function register(event) {
    // 폼 데이터 수집
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const name = document.getElementById('name').value;

    // 입력 필드 검증
    if (!email || !password || !name) {
        alert('Please fill in all fields.');
        return; // 필수 입력 필드가 비어있으면 여기서 함수 실행을 중단
    }

    // 요청 데이터 포맷팅
    const formData = {
        email: email,
        password: password,
        name: name
    };

    // AJAX 요청 생성 및 전송
    fetch('/members', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
        .then(response => {
            if (!response.ok) {
                alert('Signup request failed');
                throw new Error('Signup request failed');
            }
            return response.json(); // 여기서 응답을 JSON 형태로 변환
        })
        .then(data => {
            // 성공적인 응답 처리
            console.log('Signup successful:', data);
            window.location.href = '/login';
        })
        .catch(error => {
            // 에러 처리
            console.error('Error during signup:', error);
        });

    // 폼 제출에 의한 페이지 리로드 방지
    event.preventDefault();
}

function base64DecodeUnicode(str) {
    // Base64 디코딩
    const decodedBytes = atob(str);
    // UTF-8 바이트를 문자열로 변환
    const encodedUriComponent = decodedBytes.split('').map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join('');
    return decodeURIComponent(encodedUriComponent);
}
