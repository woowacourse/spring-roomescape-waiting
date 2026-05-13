<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>내 예약 목록 - 방탈출 예약</title>
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

        .search-card {
            background: #151932;
            border: 1px solid #1f2547;
            border-radius: 12px;
            padding: 32px;
            margin-bottom: 40px;
        }

        .search-card h2 {
            font-size: 1.25rem;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 24px;
        }

        .search-row {
            display: flex;
            gap: 12px;
            align-items: flex-end;
        }

        .form-group {
            flex: 1;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            color: #c5cae9;
            font-size: 0.9rem;
            font-weight: 500;
        }

        .form-group input {
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

        .form-group input:focus {
            outline: none;
            border-color: #667eea;
            background: #0d1129;
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
            white-space: nowrap;
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

        .btn-edit {
            background: #667eea;
            color: white;
            padding: 6px 14px;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-weight: 600;
            font-size: 0.85rem;
            font-family: 'Noto Sans KR', sans-serif;
            transition: all 0.2s ease;
            margin-right: 6px;
        }

        .btn-edit:hover {
            background: #5a6fd6;
        }

        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: rgba(0, 0, 0, 0.6);
            z-index: 100;
            align-items: center;
            justify-content: center;
        }

        .modal-overlay.active {
            display: flex;
        }

        .modal {
            background: #151932;
            border: 1px solid #1f2547;
            border-radius: 12px;
            padding: 32px;
            width: 100%;
            max-width: 480px;
        }

        .modal h2 {
            font-size: 1.25rem;
            font-weight: 600;
            color: #ffffff;
            margin-bottom: 24px;
        }

        .modal .form-group {
            flex: none;
            margin-bottom: 20px;
        }

        .modal .form-group input {
            width: 100%;
        }

        .modal-buttons {
            display: flex;
            gap: 12px;
            justify-content: flex-end;
            margin-top: 24px;
        }

        .btn-cancel-modal {
            background: #1a1f3a;
            color: #c5cae9;
            padding: 10px 20px;
            border: 1px solid #1f2547;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.95rem;
            font-family: 'Noto Sans KR', sans-serif;
        }

        .time-slot-group-buttons {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(90px, 1fr));
            gap: 8px;
            margin-top: 8px;
        }

        .time-slot-btn {
            padding: 10px;
            border-radius: 8px;
            border: 1px solid #2a315b;
            background: #0a0e27;
            color: #c5cae9;
            font-size: 0.9rem;
            font-family: 'Noto Sans KR', sans-serif;
            cursor: pointer;
            transition: all 0.2s ease;
        }

        .time-slot-btn.selected {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-color: transparent;
            color: #ffffff;
        }

        .time-slot-empty {
            color: #5c6686;
            font-size: 0.9rem;
            padding: 12px 0;
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

            .search-row {
                flex-direction: column;
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
            <h1>내 예약 목록</h1>
        </header>

        <div class="search-card">
            <h2>예약 조회</h2>
            <div class="search-row">
                <div class="form-group">
                    <label for="name">이름</label>
                    <input type="text" id="name" placeholder="예약 시 입력한 이름을 입력하세요">
                </div>
                <button class="btn" onclick="loadReservations()">조회하기</button>
            </div>
        </div>

        <div class="list-card">
            <h2>예약 내역</h2>
            <div id="reservationsList">
                <div class="empty-state">이름을 입력하고 조회하기 버튼을 눌러주세요.</div>
            </div>
        </div>
    </div>

    <div class="modal-overlay" id="editModal">
        <div class="modal">
            <h2>예약 변경</h2>
            <div class="form-group">
                <label for="editDate">날짜</label>
                <input type="date" id="editDate" onchange="loadAvailableTimes()">
            </div>
            <div class="form-group">
                <label>시간</label>
                <div id="editTimeSlots">
                    <div class="time-slot-empty">날짜를 선택하면 예약 가능한 시간이 표시됩니다.</div>
                </div>
                <input type="hidden" id="editTimeId">
            </div>
            <div class="modal-buttons">
                <button class="btn-cancel-modal" onclick="closeModal()">취소</button>
                <button class="btn" onclick="submitUpdate()">변경하기</button>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('name').addEventListener('keydown', function(e) {
            if (e.key === 'Enter') loadReservations();
        });

        function loadReservations() {
            const name = document.getElementById('name').value.trim();
            const container = document.getElementById('reservationsList');

            if (!name) {
                container.innerHTML = '<div class="empty-state">이름을 입력해주세요.</div>';
                return;
            }

            fetch(`/reservations/mine?name=${encodeURIComponent(name)}`)
                .then(res => res.json())
                .then(reservations => {
                    if (reservations.length === 0) {
                        container.innerHTML = '<div class="empty-state">해당 이름으로 예약된 내역이 없습니다.</div>';
                        return;
                    }

                    container.innerHTML = `
                        <table>
                            <thead>
                                <tr>
                                    <th>이름</th>
                                    <th>날짜</th>
                                    <th>시간</th>
                                    <th>테마</th>
                                    <th>관리</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${reservations.map(reservation => `
                                    <tr>
                                        <td>${reservation.name}</td>
                                        <td>${reservation.date}</td>
                                        <td>${reservation.time.startAt}</td>
                                        <td>${reservation.theme.name}</td>
                                        <td>
                                            <button class="btn-edit" onclick="openModal(${reservation.id}, '${reservation.name}', ${reservation.theme.id})">변경</button>
                                            <button class="btn-delete" onclick="deleteReservation(${reservation.id}, '${reservation.name}')">취소</button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    `;
                })
                .catch(error => {
                    console.error('예약 목록 불러오기 실패:', error);
                    container.innerHTML = '<div class="empty-state">예약 목록을 불러오는데 실패했습니다.</div>';
                });
        }

        let editingReservationId = null;
        let editingName = null;
        let editingThemeId = null;

        function openModal(id, name, themeId) {
            editingReservationId = id;
            editingName = name;
            editingThemeId = themeId;
            document.getElementById('editDate').value = '';
            document.getElementById('editTimeId').value = '';
            document.getElementById('editTimeSlots').innerHTML =
                '<div class="time-slot-empty">날짜를 선택하면 예약 가능한 시간이 표시됩니다.</div>';
            document.getElementById('editModal').classList.add('active');
        }

        function closeModal() {
            document.getElementById('editModal').classList.remove('active');
        }

        function loadAvailableTimes() {
            const date = document.getElementById('editDate').value;
            document.getElementById('editTimeId').value = '';

            if (!date) return;

            fetch(`/times/available?date=${date}&themeId=${editingThemeId}`)
                .then(res => res.json())
                .then(times => {
                    if (times.length === 0) {
                        document.getElementById('editTimeSlots').innerHTML =
                            '<div class="time-slot-empty">선택 가능한 시간이 없습니다.</div>';
                        return;
                    }
                    document.getElementById('editTimeSlots').innerHTML = `
                        <div class="time-slot-group-buttons">
                            ${times.map(time => `
                                <button type="button" class="time-slot-btn"
                                    onclick="selectTime(${time.id}, this)">
                                    ${time.startAt.substring(0, 5)}
                                </button>
                            `).join('')}
                        </div>
                    `;
                });
        }

        function selectTime(timeId, btn) {
            document.getElementById('editTimeId').value = timeId;
            document.querySelectorAll('.time-slot-btn').forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');
        }

        function submitUpdate() {
            const date = document.getElementById('editDate').value;
            const timeId = document.getElementById('editTimeId').value;

            if (!date) {
                alert('날짜를 선택해주세요.');
                return;
            }
            if (!timeId) {
                alert('시간을 선택해주세요.');
                return;
            }

            fetch(`/reservations/${editingReservationId}`, {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: editingName,
                    date: date,
                    timeId: parseInt(timeId),
                    themeId: editingThemeId
                })
            })
            .then(response => {
                if (response.ok) {
                    alert('예약이 변경되었습니다.');
                    closeModal();
                    loadReservations();
                } else {
                    response.json().then(error => {
                        alert(error.message || '예약 변경에 실패했습니다.');
                    });
                }
            })
            .catch(() => alert('예약 변경 중 오류가 발생했습니다.'));
        }

        function deleteReservation(id, name) {
            if (!confirm('정말로 이 예약을 취소하시겠습니까?')) {
                return;
            }

            fetch(`/reservations/${id}`, {
                method: 'DELETE'
            })
            .then(response => {
                if (response.ok) {
                    alert('예약이 취소되었습니다.');
                    loadReservations();
                } else {
                    response.json().then(error => {
                        alert(error.message || '예약 취소에 실패했습니다.');
                    });
                }
            })
            .catch(error => {
                console.error('예약 취소 실패:', error);
                alert('예약 취소 중 오류가 발생했습니다.');
            });
        }
    </script>
</body>
</html>