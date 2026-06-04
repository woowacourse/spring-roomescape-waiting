<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>예약하기</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', sans-serif; background: #f5f5f5; color: #333; }

        nav {
            background: #fff;
            border-bottom: 1px solid #e0e0e0;
            padding: 0 40px;
            display: flex;
            align-items: center;
            height: 60px;
            gap: 32px;
        }
        nav .brand { font-weight: 700; font-size: 18px; color: #1a1a1a; }
        nav a { text-decoration: none; color: #555; font-size: 14px; padding: 6px 0; border-bottom: 2px solid transparent; }
        nav a.active, nav a:hover { color: #4f46e5; border-bottom-color: #4f46e5; }

        .container { max-width: 900px; margin: 40px auto; padding: 0 20px; }

        .section-title { font-size: 20px; font-weight: 700; margin-bottom: 16px; }

        .form-group { margin-bottom: 24px; }
        .form-group label { display: block; font-size: 14px; font-weight: 600; margin-bottom: 8px; color: #555; }
        .form-group input {
            padding: 10px 14px;
            border: 1px solid #d1d5db;
            border-radius: 8px;
            font-size: 15px;
            width: 280px;
            outline: none;
        }
        .form-group input:focus { border-color: #4f46e5; }

        .theme-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 16px; margin-bottom: 32px; }
        .theme-card {
            background: #fff;
            border: 2px solid #e5e7eb;
            border-radius: 12px;
            overflow: hidden;
            cursor: pointer;
            transition: border-color 0.2s, transform 0.1s;
        }
        .theme-card:hover { border-color: #a5b4fc; transform: translateY(-2px); }
        .theme-card.selected { border-color: #4f46e5; }
        .theme-card img { width: 100%; height: 130px; object-fit: cover; }
        .theme-card-body { padding: 12px; }
        .theme-card-name { font-weight: 700; font-size: 15px; }
        .theme-card-desc { font-size: 12px; color: #888; margin-top: 4px; }

        .time-grid { display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 32px; }
        .time-btn {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: opacity 0.2s;
        }
        .time-btn.available { background: #4f46e5; color: #fff; }
        .time-btn.available:hover { opacity: 0.85; }
        .time-btn.unavailable { background: #e5e7eb; color: #9ca3af; cursor: default; }
        .time-btn.selected { outline: 3px solid #312e81; }

        .placeholder { color: #9ca3af; font-size: 14px; padding: 12px 0; }

        .submit-btn {
            padding: 12px 32px;
            background: #4f46e5;
            color: #fff;
            border: none;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
            margin-right: 12px;
        }
        .submit-btn:hover { background: #4338ca; }
        .submit-btn:disabled { background: #c7d2fe; cursor: default; }
        .wait-btn {
            padding: 12px 32px;
            background: #fff;
            color: #4f46e5;
            border: 2px solid #4f46e5;
            border-radius: 8px;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
        }
        .wait-btn:hover { background: #eef2ff; }

        .toast {
            position: fixed;
            bottom: 32px;
            left: 50%;
            transform: translateX(-50%);
            background: #1f2937;
            color: #fff;
            padding: 12px 24px;
            border-radius: 8px;
            font-size: 14px;
            display: none;
            z-index: 999;
        }
        .toast.show { display: block; }
        .toast.error { background: #dc2626; }
    </style>
</head>
<body>
<nav>
    <span class="brand">방탈출 예약</span>
    <a href="/reservation" class="active">예약하기</a>
    <a href="/my-reservations">내 예약 조회</a>
    <a href="/popular-themes">인기 테마</a>
</nav>

<div class="container">
    <div class="form-group">
        <label>이름</label>
        <input type="text" id="nameInput" placeholder="이름을 입력하세요" />
    </div>
    <div class="form-group">
        <label>날짜</label>
        <input type="date" id="dateInput" />
    </div>

    <div class="section-title">테마 선택</div>
    <div class="theme-grid" id="themeGrid">
        <p class="placeholder">테마를 불러오는 중...</p>
    </div>

    <div class="section-title">예약 가능 시간</div>
    <div class="time-grid" id="timeGrid">
        <p class="placeholder">날짜와 테마를 먼저 선택해주세요</p>
    </div>

    <div id="actionArea" style="display:none;">
        <button class="submit-btn" id="reserveBtn" onclick="submitReservation()">예약하기</button>
        <button class="wait-btn" id="waitBtn" onclick="submitWaiting()" style="display:none;">대기 신청</button>
    </div>
</div>

<div class="toast" id="toast"></div>

<script>
    let selectedThemeId = null;
    let selectedTimeId = null;
    let selectedTimeAvailable = false;

    async function loadThemes() {
        try {
            const res = await fetch('/themes');
            const themes = await res.json();
            const grid = document.getElementById('themeGrid');
            if (themes.length === 0) {
                grid.innerHTML = '<p class="placeholder">등록된 테마가 없습니다</p>';
                return;
            }
            grid.innerHTML = themes.map(t => `
                <div class="theme-card" onclick="selectTheme(${t.id}, this)">
                    <img src="${t.url}" alt="${t.name}" onerror="this.style.display='none'">
                    <div class="theme-card-body">
                        <div class="theme-card-name">${t.name}</div>
                        <div class="theme-card-desc">${t.description}</div>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            document.getElementById('themeGrid').innerHTML = '<p class="placeholder">테마를 불러올 수 없습니다</p>';
        }
    }

    function selectTheme(themeId, el) {
        selectedThemeId = themeId;
        selectedTimeId = null;
        document.querySelectorAll('.theme-card').forEach(c => c.classList.remove('selected'));
        el.classList.add('selected');
        loadTimes();
    }

    document.getElementById('dateInput').addEventListener('change', loadTimes);

    async function loadTimes() {
        const date = document.getElementById('dateInput').value;
        if (!date || !selectedThemeId) return;

        document.getElementById('actionArea').style.display = 'none';
        selectedTimeId = null;

        try {
            const res = await fetch(`/times?date=${date}&themeId=${selectedThemeId}`);
            const times = await res.json();
            const grid = document.getElementById('timeGrid');
            if (times.length === 0) {
                grid.innerHTML = '<p class="placeholder">예약 가능한 시간이 없습니다</p>';
                return;
            }
            grid.innerHTML = times.map(t => `
                <button
                    class="time-btn ${t.isAvailable ? 'available' : 'unavailable'}"
                    ${!t.isAvailable ? 'title="이미 예약된 시간 (대기 가능)"' : ''}
                    onclick="${t.isAvailable ? `selectTime(${t.id}, true, this)` : `selectTime(${t.id}, false, this)`}">
                    ${t.startAt}
                </button>
            `).join('');
        } catch (e) {
            document.getElementById('timeGrid').innerHTML = '<p class="placeholder">시간을 불러올 수 없습니다</p>';
        }
    }

    function selectTime(timeId, isAvailable, el) {
        selectedTimeId = timeId;
        selectedTimeAvailable = isAvailable;
        document.querySelectorAll('.time-btn').forEach(b => b.classList.remove('selected'));
        el.classList.add('selected');

        const actionArea = document.getElementById('actionArea');
        const reserveBtn = document.getElementById('reserveBtn');
        const waitBtn = document.getElementById('waitBtn');

        actionArea.style.display = 'block';
        if (isAvailable) {
            reserveBtn.style.display = 'inline-block';
            waitBtn.style.display = 'none';
        } else {
            reserveBtn.style.display = 'none';
            waitBtn.style.display = 'inline-block';
        }
    }

    async function submitReservation() {
        const name = document.getElementById('nameInput').value.trim();
        const date = document.getElementById('dateInput').value;
        if (!name) { showToast('이름을 입력해주세요', true); return; }
        if (!date) { showToast('날짜를 선택해주세요', true); return; }
        if (!selectedThemeId || !selectedTimeId) { showToast('테마와 시간을 선택해주세요', true); return; }

        try {
            const res = await fetch('/reservations', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, date, timeId: selectedTimeId, themeId: selectedThemeId })
            });
            if (res.ok) {
                showToast('예약이 완료되었습니다!');
                loadTimes();
            } else {
                const err = await res.json();
                showToast(err.message || '예약에 실패했습니다', true);
            }
        } catch (e) {
            showToast('오류가 발생했습니다', true);
        }
    }

    async function submitWaiting() {
        const name = document.getElementById('nameInput').value.trim();
        const date = document.getElementById('dateInput').value;
        if (!name) { showToast('이름을 입력해주세요', true); return; }
        if (!date) { showToast('날짜를 선택해주세요', true); return; }
        if (!selectedThemeId || !selectedTimeId) { showToast('테마와 시간을 선택해주세요', true); return; }

        try {
            const res = await fetch('/reservations/waitings', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name, date, timeId: selectedTimeId, themeId: selectedThemeId })
            });
            if (res.ok) {
                showToast('대기 신청이 완료되었습니다!');
            } else {
                const err = await res.json();
                showToast(err.message || '대기 신청에 실패했습니다', true);
            }
        } catch (e) {
            showToast('오류가 발생했습니다', true);
        }
    }

    function showToast(msg, isError = false) {
        const toast = document.getElementById('toast');
        toast.textContent = msg;
        toast.className = 'toast show' + (isError ? ' error' : '');
        setTimeout(() => toast.className = 'toast', 3000);
    }

    loadThemes();
</script>
</body>
</html>