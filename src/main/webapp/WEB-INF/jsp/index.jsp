<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>방탈출 예약 시스템</title>
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
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }

        .container {
            max-width: 1100px;
            width: 100%;
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

        .subtitle {
            font-size: 1rem;
            color: #8b93b0;
            font-weight: 300;
        }

        .grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 32px;
            margin-bottom: 40px;
        }

        .card {
            background: #151932;
            border-radius: 16px;
            padding: 40px;
            border: 1px solid #1f2547;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .card:hover {
            border-color: #2d3561;
            transform: translateY(-4px);
        }

        .card-header {
            display: flex;
            align-items: center;
            gap: 12px;
            margin-bottom: 28px;
            padding-bottom: 20px;
            border-bottom: 1px solid #1f2547;
        }

        .card-icon {
            width: 48px;
            height: 48px;
            border-radius: 12px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 24px;
        }

        .user-card .card-icon {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        }

        .admin-card .card-icon {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 600;
            color: #ffffff;
        }

        .menu-list {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 12px;
        }

        .menu-link {
            display: flex;
            align-items: center;
            padding: 16px 20px;
            background: #1a1f3a;
            color: #c5cae9;
            text-decoration: none;
            border-radius: 10px;
            font-size: 0.95rem;
            font-weight: 500;
            transition: all 0.2s ease;
            border: 1px solid transparent;
        }

        .menu-link:hover {
            background: #1f2547;
            color: #ffffff;
            border-color: #2d3561;
            transform: translateX(4px);
        }

        .menu-link::before {
            content: '→';
            margin-right: 12px;
            opacity: 0;
            transition: all 0.2s ease;
        }

        .menu-link:hover::before {
            opacity: 1;
            margin-right: 8px;
        }

        footer {
            text-align: center;
            padding-top: 40px;
            border-top: 1px solid #1f2547;
        }

        .footer-text {
            color: #5c6686;
            font-size: 0.875rem;
            font-weight: 300;
        }

        @media (max-width: 768px) {
            .grid {
                grid-template-columns: 1fr;
                gap: 24px;
            }

            h1 {
                font-size: 2rem;
            }

            .card {
                padding: 32px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <h1>방탈출 예약 시스템</h1>
            <p class="subtitle">Room Escape Reservation System</p>
        </header>

        <div class="grid">
            <div class="card user-card">
                <div class="card-header">
                    <div class="card-icon">🎭</div>
                    <h2 class="card-title">사용자</h2>
                </div>
                <ul class="menu-list">
                    <li>
                        <a href="/theme" class="menu-link">테마 둘러보기</a>
                    </li>
                    <li>
                        <a href="/reservation" class="menu-link">예약하기</a>
                    </li>
                    <li>
                        <a href="/my-reservations" class="menu-link">내 예약 목록</a>
                    </li>
                </ul>
            </div>

            <div class="card admin-card">
                <div class="card-header">
                    <div class="card-icon">⚙️</div>
                    <h2 class="card-title">관리자</h2>
                </div>
                <ul class="menu-list">
                    <li>
                        <a href="/admin/theme" class="menu-link">테마 관리</a>
                    </li>
                    <li>
                        <a href="/admin/reservation" class="menu-link">예약 관리</a>
                    </li>
                    <li>
                        <a href="/admin/time" class="menu-link">시간 관리</a>
                    </li>
                </ul>
            </div>
        </div>

        <footer>
            <p class="footer-text">© 2026 Room Escape Reservation System</p>
        </footer>
    </div>
</body>
</html>
