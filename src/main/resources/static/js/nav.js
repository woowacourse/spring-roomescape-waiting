document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav');
  if (toggle && nav) {
    toggle.addEventListener('click', () => nav.classList.toggle('open'));
  }

  const authLink = document.getElementById('auth-link');

  fetch('/member/profile')
    .then(res => res.ok ? res.json() : null)
    .then(member => {
      if (member) {
        applyLoggedIn(member, authLink);
      } else {
        applyLoggedOut(authLink);
      }
    })
    .catch(() => applyLoggedOut(authLink));
});

function applyLoggedIn(member, authLink) {
  if (member.role === 'ADMIN') {
    document.querySelectorAll('.admin-entry').forEach(el => el.classList.remove('d-none'));
  }

  const greeting = document.getElementById('user-greeting');
  if (greeting) {
    greeting.textContent = `${member.name}님 환영합니다`;
    greeting.classList.remove('d-none');
  }

  if (!authLink) return;
  authLink.textContent = '로그아웃';
  authLink.href = '#';
  authLink.addEventListener('click', e => {
    e.preventDefault();
    fetch('/logout', {method: 'POST'})
      .then(() => { window.location.href = '/login'; });
  });
}

function applyLoggedOut(authLink) {
  if (!authLink) return;
  authLink.textContent = '로그인';
  authLink.href = '/login';
}
