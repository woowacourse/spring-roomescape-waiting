<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>예약 - 방탈출 예약</title>
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
            max-width: 1200px;
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

        .form-card {
            background: #151932;
            border: 1px solid #1f2547;
            border-radius: 12px;
            padding: 32px;
            margin-bottom: 40px;
        }

        .form-card h2 {
            font-size: 1.25rem;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 24px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #c5cae9;
            font-size: 0.9rem;
            font-weight: 500;
        }

        .form-group input,
        .form-group select {
            width: 100%;
            padding: 12px 16px;
            background: #0a0e27;
            border: 1px solid #1f2547;
            border-radius: 8px;
            font-size: 0.95rem;
            color: #ffffff;
            font-family: 'Noto Sans KR', sans-serif;
            transition: all 0.2s ease;
        }

        .form-group input:focus,
        .form-group select:focus {
            outline: none;
            border-color: #667eea;
            background: #0d1129;
        }

        .form-group select option {
            background: #151932;
            color: #ffffff;
        }

        .time-picker {
            background: #10152d;
            border: 1px solid #1f2547;
            border-radius: 12px;
            padding: 18px;
        }

        .time-picker-guide {
            color: #8b93b0;
            font-size: 0.85rem;
            margin-bottom: 16px;
            line-height: 1.5;
        }

        .time-slots {
            display: grid;
            gap: 14px;
        }

        .time-slot-group-title {
            color: #ffffff;
            font-size: 0.95rem;
            font-weight: 600;
            margin-bottom: 10px;
        }

        .time-slot-group-buttons {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(110px, 1fr));
            gap: 10px;
        }

        .time-slot-btn {
            width: 100%;
            padding: 14px 12px;
            border-radius: 10px;
            border: 1px solid #2a315b;
            background: #151932;
            color: #c5cae9;
            font-size: 0.95rem;
            font-weight: 600;
            font-family: 'Noto Sans KR', sans-serif;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .time-slot-btn:hover {
            border-color: #667eea;
            color: #ffffff;
            transform: translateY(-1px);
        }

        .time-slot-btn:focus-visible {
            outline: 2px solid #8ea2ff;
            outline-offset: 2px;
        }

        .time-slot-btn.selected {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-color: transparent;
            color: #ffffff;
            box-shadow: 0 8px 20px rgba(102, 126, 234, 0.28);
        }

        .time-slot-empty {
            padding: 18px;
            border-radius: 10px;
            background: #151932;
            color: #5c6686;
            text-align: center;
            font-size: 0.9rem;
            border: 1px dashed #2a315b;
        }

        .btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 12px 24px;
            border: none;
            border-radius: 8px;
            font-size: 0.95rem;
            font-weight: 600;
            cursor: pointer;
            font-family: 'Noto Sans KR', sans-serif;
            transition: all 0.2s ease;
        }

        .btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
        }

        .list-card {
            background: #151932;
            border: 1px solid #1f2547;
            border-radius: 12px;
            padding: 32px;
        }

        .list-card h2 {
            font-size: 1.25rem;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 24px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th {
            background: #1a1f3a;
            color: #c5cae9;
            padding: 14px;
            text-align: left;
            font-weight: 600;
            font-size: 0.9rem;
            border: 1px solid #1f2547;
        }

        td {
            padding: 14px;
            border: 1px solid #1f2547;
            color: #c5cae9;
            font-size: 0.9rem;
        }

        tr:hover {
            background: #1a1f3a;
        }

        .btn-delete {
            background: #f5576c;
            color: white;
            padding: 6px 14px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-weight: 600;
            font-size: 0.85rem;
            font-family: 'Noto Sans KR', sans-serif;
            transition: all 0.2s ease;
        }

        .btn-delete:hover {
            background: #f34359;
        }

        .empty-state {
            text-align: center;
            color: #5c6686;
            padding: 40px;
            font-size: 0.95rem;
        }

        @media (max-width: 768px) {
            h1 {
                font-size: 2rem;
            }

            table {
                font-size: 0.85rem;
            }

            th, td {
                padding: 10px;
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
            <h1>예약하기</h1>
        </header>

        <div class="form-card">
            <h2>새 예약 만들기</h2>
            <form id="reservationForm">
                <div class="form-group">
                    <label for="name">이름</label>
                    <input type="text" id="name" name="name" required placeholder="이름을 입력하세요">
                </div>
                <div class="form-group">
                    <label for="themeId">테마</label>
                    <select id="themeId" name="themeId" required>
                        <option value="">테마를 선택하세요</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="date">날짜</label>
                    <input type="date" id="date" name="date" required>
                </div>
                <div class="form-group">
                    <label for="timeId">시간</label>
                    <div class="time-picker">
                        <p class="time-picker-guide">직접 입력 없이, 아래에서 원하는 시간대를 눌러 선택하세요.</p>
                        <div id="timeSlots" class="time-slots">
                            <div class="time-slot-empty">날짜와 테마를 선택하면 예약 가능한 시간이 표시됩니다.</div>
                        </div>
                        <input type="hidden" id="timeId" name="timeId" required>
                    </div>
                </div>
                <button type="submit" class="btn">예약하기</button>
            </form>
        </div>

    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            loadThemes();
            document.getElementById('date').addEventListener('change', loadAvailableTimes);
            document.getElementById('themeId').addEventListener('change', loadAvailableTimes);
        });

        function loadAvailableTimes() {
            const date = document.getElementById('date').value;
            const themeId = document.getElementById('themeId').value;

            document.getElementById('timeId').value = '';

            if (!date || !themeId) {
                document.getElementById('timeSlots').innerHTML =
                    '<div class="time-slot-empty">날짜와 테마를 선택하면 예약 가능한 시간이 표시됩니다.</div>';
                return;
            }

            fetch(`/times/available?date=${date}&themeId=${themeId}`)
                .then(response => response.json())
                .then(times => {
                    const today = new Date().toISOString().split('T')[0];
                    if (date === today) {
                        const now = new Date();
                        times = times.filter(time => {
                            const [hour, minute] = time.startAt.split(':');
                            const slotTime = new Date();
                            slotTime.setHours(parseInt(hour), parseInt(minute), 0);
                            return slotTime > now;
                        });
                    }
                    renderTimeSlots(times);
                })
                .catch(error => {
                    console.error('시간 목록 불러오기 실패:', error);
                    document.getElementById('timeSlots').innerHTML =
                        '<div class="time-slot-empty">시간 목록을 불러오지 못했습니다.</div>';
                });
        }

        function renderTimeSlots(times) {
            const container = document.getElementById('timeSlots');

            if (times.length === 0) {
                container.innerHTML = '<div class="time-slot-empty">선택 가능한 시간이 없습니다.</div>';
                return;
            }

            container.innerHTML = `
                <div class="time-slot-group-buttons">
                    ${times.map(time => `
                        <button
                            type="button"
                            class="time-slot-btn"
                            data-time-id="${time.id}"
                            aria-pressed="false"
                            onclick="selectTime(${time.id})"
                        >
                            ${time.startAt.substring(0, 5)}
                        </button>
                    `).join('')}
                </div>
            `;
        }

        function selectTime(timeId) {
            document.getElementById('timeId').value = timeId;

            document.querySelectorAll('.time-slot-btn').forEach(button => {
                const isSelected = parseInt(button.dataset.timeId, 10) === timeId;
                button.classList.toggle('selected', isSelected);
                button.setAttribute('aria-pressed', String(isSelected));
            });
        }

        function loadThemes() {
            fetch('/themes')
                .then(response => response.json())
                .then(themes => {
                    const select = document.getElementById('themeId');
                    themes.forEach(theme => {
                        const option = document.createElement('option');
                        option.value = theme.id;
                        option.textContent = theme.name;
                        select.appendChild(option);
                    });
                })
                .catch(error => console.error('테마 목록 불러오기 실패:', error));
        }

        document.getElementById('reservationForm').addEventListener('submit', function(e) {
            e.preventDefault();

            const reservationData = {
                name: document.getElementById('name').value,
                date: document.getElementById('date').value,
                timeId: parseInt(document.getElementById('timeId').value),
                themeId: parseInt(document.getElementById('themeId').value)
            };

            if (!reservationData.timeId) {
                alert('예약 시간을 선택해주세요.');
                return;
            }

            fetch('/reservations', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(reservationData)
            })
            .then(response => {
                if (response.ok) {
                    return response.json().then(() => {
                        alert('예약이 완료되었습니다!');
                        document.getElementById('reservationForm').reset();
                        document.querySelectorAll('.time-slot-btn').forEach(button => {
                            button.classList.remove('selected');
                            button.setAttribute('aria-pressed', 'false');
                        });
                    });
                } else {
                    return response.json().then(error => {
                        alert(error.message || '예약에 실패했습니다.');
                    });
                }
            })
            .catch(error => {
                console.error('예약 생성 실패:', error);
                alert('예약 중 오류가 발생했습니다.');
            });
        });

    </script>
</body>
</html>
