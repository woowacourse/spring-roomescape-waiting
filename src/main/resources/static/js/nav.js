document.addEventListener('DOMContentLoaded', () => {
  const toggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav');
  if (toggle && nav) {
    toggle.addEventListener('click', () => nav.classList.toggle('open'));
  }

  const isLoggedIn = document.cookie.includes('auth=1');
  const authLink = document.getElementById('auth-link');

  if (isLoggedIn) {
    fetch('/member/profile')
      .then(res => res.ok ? res.json() : null)
      .then(member => {
        if (member && member.role === 'ADMIN') {
          document.querySelectorAll('.admin-entry').forEach(el => el.classList.remove('d-none'));
        }
      });

    if (authLink) {
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
    }
  } else {
    if (authLink) {
      authLink.textContent = '로그인';
      authLink.href = '/login';
    }
  }
});
