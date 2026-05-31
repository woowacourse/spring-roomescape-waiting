const ADMIN_PASSCODE = '6789';
const ADMIN_PATHS = ['/theme', '/time'];

function isAdminVerified() {
  return sessionStorage.getItem('adminVerified') === 'true';
}

function verifyAdmin() {
  const input = prompt('관리자 번호를 입력하세요.');
  if (input === ADMIN_PASSCODE) {
    sessionStorage.setItem('adminVerified', 'true');
    return true;
  }
  if (input !== null) {
    alert('관리자 번호가 올바르지 않습니다.');
  }
  return false;
}

document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav');
  if (toggle && nav) {
    toggle.addEventListener('click', () => nav.classList.toggle('open'));
  }

  // 관리자 페이지 직접 접근 시 번호 확인
  if (ADMIN_PATHS.includes(window.location.pathname) && !isAdminVerified()) {
    if (!verifyAdmin()) {
      window.location.href = '/';
      return;
    }
  }

  // 관리자 진입 링크 클릭 시 번호 확인
  document.querySelectorAll('.admin-entry').forEach(entry => {
    entry.addEventListener('click', e => {
      if (isAdminVerified()) {
        return;
      }
      e.preventDefault();
      if (verifyAdmin()) {
        window.location.href = entry.getAttribute('href');
      }
    });
  });

  const isLoggedIn = document.cookie.includes('auth=1');
  const authLink = document.getElementById('auth-link');
  if (!authLink) return;

  if (isLoggedIn) {
    authLink.textContent = '로그아웃';
    authLink.href = '#';
    authLink.addEventListener('click', e => {
      e.preventDefault();
      fetch('/logout', {method: 'POST'})
        .then(() => {
          document.cookie = 'auth=1; path=/; max-age=0';
          window.location.href = '/login';
        });
    });
  } else {
    authLink.textContent = '로그인';
    authLink.href = '/login';
  }
});
