# ERD (Entity Relationship Diagram)

```mermaid
erDiagram
    THEME ||--o{ RESERVATION : "하나의 테마는 여러 예약을 가질 수 있다"
    RESERVATION_TIME ||--o{ RESERVATION : "하나의 예약 시간은 여러 예약을 가질 수 있다"
    
    THEME {
        bigint id PK "아이디"
        varchar name "테마 이름"
        varchar description "테마 설명"
        varchar thumbnail "썸네일 URL"
    }
    
    RESERVATION_TIME {
        bigint id PK "아이디"
        time start_at "시작 시간"
    }
    
    RESERVATION {
        bigint id PK "아이디"
        varchar name "예약자 이름"
        date date "예약 날짜"
        bigint time_id FK "예약 시간 아이디"
        bigint theme_id FK "테마 아이디"
    }
```
