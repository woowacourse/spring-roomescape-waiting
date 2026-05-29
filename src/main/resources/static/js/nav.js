document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav');
  if (toggle && nav) {
    toggle.addEventListener('click', () => nav.classList.toggle('open'));
  }

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