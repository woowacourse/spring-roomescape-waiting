<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>예약 관리 - 방탈출 예약</title>
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
                padding: 10px 8px;
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
            <h1>예약 관리</h1>
        </header>

        <div class="list-card">
            <h2>전체 예약 목록</h2>
            <div id="reservationsList">
                <div class="empty-state">로딩 중...</div>
            </div>
        </div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            loadReservations();
        });

        function loadReservations() {
            fetch('/reservations')
                .then(response => response.json())
                .then(reservations => {
                    const container = document.getElementById('reservationsList');

                    if (reservations.length === 0) {
                        container.innerHTML = '<div class="empty-state">예약 내역이 없습니다.</div>';
                        return;
                    }

                    container.innerHTML = `
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>이름</th>
                                    <th>날짜</th>
                                    <th>시간</th>
                                    <th>테마</th>
                                    <th>생성일시</th>
                                    <th>관리</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${reservations.map(reservation => `
                                    <tr>
                                        <td>${reservation.id}</td>
                                        <td>${reservation.name}</td>
                                        <td>${reservation.date}</td>
                                        <td>${reservation.time.startAt}</td>
                                        <td>${reservation.theme.name}</td>
                                        <td>${new Date(reservation.createdAt).toLocaleString('ko-KR')}</td>
                                        <td>
                                            <button class="btn-delete" onclick="deleteReservation(${reservation.id})">삭제</button>
                                        </td>
                                    </tr>
                                `).join('')}
                            </tbody>
                        </table>
                    `;
                })
                .catch(error => {
                    console.error('예약 목록 불러오기 실패:', error);
                    document.getElementById('reservationsList').innerHTML =
                        '<div class="empty-state">예약 목록을 불러오는데 실패했습니다.</div>';
                });
        }

        function deleteReservation(id) {
            if (!confirm('정말로 이 예약을 삭제하시겠습니까?')) {
                return;
            }

            fetch(`/reservations/${id}`, {
                method: 'DELETE'
            })
            .then(response => {
                if (response.ok) {
                    alert('예약이 삭제되었습니다.');
                    loadReservations();
                } else {
                    response.json().then(error => {
                        alert(error.message || '예약 삭제에 실패했습니다.');
                    });
                }
            })
            .catch(error => {
                console.error('예약 삭제 실패:', error);
                alert('예약 삭제 중 오류가 발생했습니다.');
            });
        }
    </script>
</body>
</html>
