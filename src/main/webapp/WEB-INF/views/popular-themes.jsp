<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>인기 테마</title>
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

        .page-title { font-size: 24px; font-weight: 700; margin-bottom: 8px; }
        .page-subtitle { font-size: 14px; color: #6b7280; margin-bottom: 32px; }

        .theme-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 20px; }
        .theme-card {
            background: #fff;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 1px 4px rgba(0,0,0,0.08);
            transition: transform 0.15s, box-shadow 0.15s;
        }
        .theme-card:hover { transform: translateY(-3px); box-shadow: 0 4px 12px rgba(0,0,0,0.12); }
        .theme-card img { width: 100%; height: 160px; object-fit: cover; }
        .theme-card-body { padding: 16px; }
        .rank { display: inline-block; background: #4f46e5; color: #fff; font-size: 12px; font-weight: 700; padding: 2px 8px; border-radius: 99px; margin-bottom: 8px; }
        .theme-name { font-size: 16px; font-weight: 700; margin-bottom: 6px; }
        .theme-desc { font-size: 13px; color: #6b7280; line-height: 1.5; }

        .reserve-btn {
            display: block;
            margin: 12px 16px 16px;
            padding: 9px;
            background: #4f46e5;
            color: #fff;
            border: none;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            text-align: center;
            text-decoration: none;
        }
        .reserve-btn:hover { background: #4338ca; }

        .empty { text-align: center; padding: 64px; color: #9ca3af; font-size: 14px; }
    </style>
</head>
<body>
<nav>
    <span class="brand">방탈출 예약</span>
    <a href="/reservation">예약하기</a>
    <a href="/my-reservations">내 예약 조회</a>
    <a href="/popular-themes" class="active">인기 테마</a>
</nav>

<div class="container">
    <div class="page-title">주간 인기 테마</div>
    <div class="page-subtitle">최근 1주일 예약 기준 인기 테마 TOP 10</div>

    <div class="theme-grid" id="themeGrid">
        <p style="color:#9ca3af; font-size:14px;">불러오는 중...</p>
    </div>
</div>

<script>
    async function loadPopularThemes() {
        try {
            const res = await fetch('/themes/popular');
            const themes = await res.json();
            const grid = document.getElementById('themeGrid');

            if (themes.length === 0) {
                grid.innerHTML = '<div class="empty" style="grid-column:1/-1">최근 예약 데이터가 없습니다</div>';
                return;
            }

            grid.innerHTML = themes.map((t, i) => `
                <div class="theme-card">
                    <img src="${t.url}" alt="${t.name}" onerror="this.style.display='none'">
                    <div class="theme-card-body">
                        <span class="rank">${i + 1}위</span>
                        <div class="theme-name">${t.name}</div>
                        <div class="theme-desc">${t.description}</div>
                    </div>
                    <a class="reserve-btn" href="/reservation">예약하러 가기</a>
                </div>
            `).join('');
        } catch (e) {
            document.getElementById('themeGrid').innerHTML = '<div class="empty" style="grid-column:1/-1">테마를 불러올 수 없습니다</div>';
        }
    }

    loadPopularThemes();
</script>
</body>
</html>