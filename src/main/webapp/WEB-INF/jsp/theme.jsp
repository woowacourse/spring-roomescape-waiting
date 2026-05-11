<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>테마 - 방탈출 예약</title>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Noto Sans KR', sans-serif;
            background: #0a0e27;
            min-height: 100vh;
            padding: 40px 20px;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
        }

        .nav {
            margin-bottom: 40px;
        }

        .back-btn {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 20px;
            background: #151932;
            color: #c5cae9;
            text-decoration: none;
            border-radius: 8px;
            font-size: 0.9rem;
            font-weight: 500;
            border: 1px solid #1f2547;
            transition: all 0.2s ease;
        }

        .back-btn:hover {
            background: #1a1f3a;
            border-color: #2d3561;
            color: #ffffff;
        }

        header {
            text-align: center;
            margin-bottom: 60px;
        }

        h1 {
            font-size: 2.5rem;
            font-weight: 700;
            color: #ffffff;
            margin-bottom: 12px;
            letter-spacing: -0.5px;
        }

        .section {
            margin-bottom: 60px;
        }

        .section-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 24px;
        }

        .section-icon {
            font-size: 1.5rem;
        }

        .section-title {
            font-size: 1.5rem;
            font-weight: 600;
            color: #ffffff;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 24px;
        }

        .card {
            background: #151932;
            border-radius: 12px;
            overflow: hidden;
            border: 1px solid #1f2547;
            transition: all 0.3s ease;
            cursor: pointer;
        }

        .card:hover {
            border-color: #667eea;
            transform: translateY(-4px);
        }

        .card-image {
            width: 100%;
            height: 200px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 1.1rem;
            font-weight: 500;
            position: relative;
            overflow: hidden;
        }

        .card-image img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .popular-badge {
            position: absolute;
            top: 12px;
            right: 12px;
            background: #f5576c;
            color: white;
            padding: 6px 12px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 600;
        }

        .card-content {
            padding: 24px;
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 12px;
        }

        .card-description {
            font-size: 0.9rem;
            color: #8b93b0;
            line-height: 1.6;
        }

        .empty-state {
            background: #151932;
            border: 1px solid #1f2547;
            border-radius: 12px;
            padding: 60px 40px;
            text-align: center;
        }

        .empty-state-text {
            color: #5c6686;
            font-size: 1rem;
        }

        @media (max-width: 768px) {
            h1 {
                font-size: 2rem;
            }

            .grid {
                grid-template-columns: 1fr;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <nav class="nav">
            <a href="/" class="back-btn">
                <span>←</span>
                <span>메인으로</span>
            </a>
        </nav>

        <header>
            <h1>테마 둘러보기</h1>
        </header>

        <section class="section">
            <div class="section-header">
                <span class="section-icon">🔥</span>
                <h2 class="section-title">인기 테마</h2>
            </div>
            <div id="popularThemes" class="grid">
                <div class="empty-state">
                    <p class="empty-state-text">로딩 중...</p>
                </div>
            </div>
        </section>

        <section class="section">
            <div class="section-header">
                <span class="section-icon">📋</span>
                <h2 class="section-title">전체 테마</h2>
            </div>
            <div id="allThemes" class="grid">
                <div class="empty-state">
                    <p class="empty-state-text">로딩 중...</p>
                </div>
            </div>
        </section>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            loadPopularThemes();
            loadAllThemes();
        });

        function loadPopularThemes() {
            fetch('/themes/popular')
                .then(response => response.json())
                .then(themes => {
                    const container = document.getElementById('popularThemes');

                    if (themes.length === 0) {
                        container.innerHTML = '<div class="empty-state"><p class="empty-state-text">인기 테마가 없습니다.</p></div>';
                        return;
                    }

                    container.innerHTML = themes.map(theme => createThemeCard(theme, true)).join('');
                })
                .catch(error => {
                    console.error('인기 테마 불러오기 실패:', error);
                    document.getElementById('popularThemes').innerHTML =
                        '<div class="empty-state"><p class="empty-state-text">인기 테마를 불러오는데 실패했습니다.</p></div>';
                });
        }

        function loadAllThemes() {
            fetch('/themes')
                .then(response => response.json())
                .then(themes => {
                    const container = document.getElementById('allThemes');

                    if (themes.length === 0) {
                        container.innerHTML = '<div class="empty-state"><p class="empty-state-text">등록된 테마가 없습니다.</p></div>';
                        return;
                    }

                    container.innerHTML = themes.map(theme => createThemeCard(theme, false)).join('');
                })
                .catch(error => {
                    console.error('테마 목록 불러오기 실패:', error);
                    document.getElementById('allThemes').innerHTML =
                        '<div class="empty-state"><p class="empty-state-text">테마 목록을 불러오는데 실패했습니다.</p></div>';
                });
        }

        function createThemeCard(theme, isPopular) {
            return `
                <div class="card">
                    <div class="card-image">
                        ${isPopular ? '<span class="popular-badge">인기</span>' : ''}
                        <img src="${theme.url}" alt="${theme.name}"
                             onerror="this.parentElement.innerHTML='<div>${theme.name}</div>'">
                    </div>
                    <div class="card-content">
                        <h3 class="card-title">${theme.name}</h3>
                        <p class="card-description">${theme.description}</p>
                    </div>
                </div>
            `;
        }
    </script>
</body>
</html>
