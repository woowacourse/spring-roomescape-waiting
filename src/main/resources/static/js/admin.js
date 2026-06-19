(function () {
    "use strict";

    const $ = (s) => document.querySelector(s);

    async function init() {
        await Promise.all([loadThemes(), loadTimes(), loadReservations()]);
    }

    function getPillInfo(status) {
        if (status === 'CONFIRMED') return {text: '확정', class: 'badge-confirmed'};
        if (status === 'WAITING') return {text: '대기', class: 'badge-waiting'};
        return {text: '결제대기', class: 'badge-pending'};
    }

    // 테마 관리
    async function loadThemes() {
        try {
            const res = await fetch('/themes');
            const themes = await res.json();
            const container = $('#admin-theme-list');
            if (!themes || themes.length === 0) {
                container.innerHTML = '<div style="padding:20px; text-align:center; color:var(--text-muted); font-size:0.9rem;">등록된 테마가 없습니다.</div>';
                return;
            }
            container.innerHTML = themes.map(t => `
                <div class="admin-item">
                    <span style="font-weight:600;">${t.name}</span>
                    <button class="btn-danger-sm" onclick="deleteTheme(${t.id})">삭제</button>
                </div>
            `).join('');
        } catch (e) {
            console.error('테마 로드 실패', e);
        }
    }

    $('#theme-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        const formData = new FormData($('#theme-form'));

        try {
            const res = await fetch('/admin/themes', {
                method: 'POST',
                body: formData
            });
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({message: '알 수 없는 오류가 발생했습니다.'}));
                throw new Error(errorData.message || '알 수 없는 오류가 발생했습니다.');
            }
            alert('테마가 추가되었습니다.');
            location.reload();
        } catch (e) {
            alert('테마 추가 실패: ' + e.message);
        }
    });

    window.deleteTheme = async (id) => {
        const confirmed = await confirm('이 테마를 삭제하시겠습니까?');
        if (!confirmed) return;
        try {
            const res = await fetch(`/admin/themes/${id}`, {method: 'DELETE'});
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({message: '알 수 없는 오류가 발생했습니다.'}));
                throw new Error(errorData.message || '알 수 없는 오류가 발생했습니다.');
            }
            loadThemes();
        } catch (e) {
            alert('삭제 실패: ' + e.message);
        }
    };

    // 시간 관리
    async function loadTimes() {
        try {
            const res = await fetch('/times');
            const times = await res.json();
            const container = $('#admin-time-list');

            if (!times || times.length === 0) {
                container.innerHTML = '<div style="padding:20px; text-align:center; color:var(--text-muted); font-size:0.9rem;">등록된 시간이 없습니다.</div>';
                return;
            }

            container.innerHTML = times.map(t => `
                <div class="admin-item">
                    <span style="font-family:monospace; font-weight:700;">${t.startAt.slice(0, 5)}</span>
                    <button class="btn-danger-sm" onclick="deleteTime(${t.id})">삭제</button>
                </div>
            `).join('');
        } catch (e) {
            console.error('시간 로드 실패', e);
        }
    }

    $('#time-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        try {
            const res = await fetch('/admin/times', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({startAt: $('#time-start').value + ':00'})
            });
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({message: '알 수 없는 오류가 발생했습니다.'}));
                throw new Error(errorData.message || '알 수 없는 오류가 발생했습니다.');
            }
            alert('시간이 추가되었습니다.');
            location.reload();
        } catch (e) {
            alert('시간 추가 실패: ' + e.message);
        }
    });

    window.deleteTime = async (id) => {
        const confirmed = await confirm('이 시간 슬롯을 삭제하시겠습니까?');
        if (!confirmed) return;
        try {
            const res = await fetch(`/admin/times/${id}`, {method: 'DELETE'});
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({message: '알 수 없는 오류가 발생했습니다.'}));
                throw new Error(errorData.message || '알 수 없는 오류가 발생했습니다.');
            }
            loadTimes();
        } catch (e) {
            alert('삭제 실패: ' + e.message);
        }
    };

    // 예약 관리
    async function loadReservations() {
        try {
            const res = await fetch('/reservations');
            const list = await res.json();
            const container = $('#admin-reservation-list');
            if (!list || list.length === 0) {
                container.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:40px; color:var(--text-muted);">현재 예약 내역이 없습니다.</td></tr>';
                return;
            }
            container.innerHTML = list.map(r => {
                const badge = getPillInfo(r.status);
                return `
                    <tr>
                        <td>#${r.id}</td>
                        <td style="font-weight:600;">${r.name}</td>
                        <td>${r.date}</td>
                        <td style="font-family:monospace;">${r.timeResponse.startAt.slice(0, 5)}</td>
                        <td>${r.themeResponse.name}</td>
                        <td><span class="badge ${badge.class}">${badge.text}</span></td>
                        <td><button class="btn-danger-sm" onclick="deleteReservation(${r.id})">취소</button></td>
                    </tr>
                `;
            }).join('');
        } catch (e) {
            console.error('예약 로드 실패', e);
        }
    }

    window.deleteReservation = async (id) => {
        const confirmed = await confirm('이 예약을 취소하시겠습니까?');
        if (!confirmed) return;
        try {
            const res = await fetch(`/reservations/${id}`, {method: 'DELETE'});
            if (!res.ok) {
                const errorData = await res.json().catch(() => ({message: '알 수 없는 오류가 발생했습니다.'}));
                throw new Error(errorData.message || '알 수 없는 오류가 발생했습니다.');
            }
            loadReservations();
        } catch (e) {
            alert('취소 실패: ' + e.message);
        }
    };

    init();
})();
