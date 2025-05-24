## Waiting을 왜 나눠야 돼?

Waiting이 Reservation을 들고 있는 경우 대기 추가를 하려면

1. 요청 (예약날짜, 시간, 테마) -> 이미 존재하는 Reservation 인지 조회
2. 탐색한 Reservation의 Id로 Waiting을 생성해서 DB에 등록해야 함.
3. Waiting이 Reservation은 user를 제외한 모든 필드가 중복.
    - 중복제거방법 1 : Waiting이 Reservation Id를 들고 있자. (ManyToOne 연관관계매핑)
        - 연관 관계 걸면 Reservation 삭제가 복잡해짐.
        - 삭제요청 -> 해당 예약의 Waiting 조회, 1순위 찾아서 예약으로 변경 -> 나머지 순위 Waiting 참조를 새 예약으로 변경 -> 기존 예약 삭제
        - 예약 삭제가 대기에 의존적이므로 X
    - 중복제거방법 2 : 중복되는 필드를 VO로 묶기?
        - (날짜, 시간, 테마) -> ReservationSpec으로 묶기?
4. Waiting과 ReservationTime 과의 차이 : ReservationTime은 Reservation과 무관하게 관리될 수 있음. (관리자가 예약과 별도로 타임을 관리함)
5. BUT Waiting은 Reservation 이 없이 존재할 수 없는 개념. Entity 로 관리되어야 하나?
    - Waiting은 Reservation이 아직 확정되지 않은 '상태'에 가깝다.
6. Waiting을 분리하지 말고, Reservation 에 필드로 Status를 추가
    - 장점 : 기존 Reservation 메서드를 그대로 재활용 가능
   - 단점 : 대기 관련 로직의 추가로 Reservation Service의 책임이 늘어남(클래스가 뚱뚱해짐), 객체지향 X
        - 해결 : Waiting의 경우를 처리하는 Service를 따로 파자.
            - 하고 나니 별로 안 뚱뚱한데?

## 결론

- [x] Reservation에 필드 reservationStatus, createdAt을 추가
    - [x] Reservation에 reservationStatus 요청 필드 추가하기
- [x] 대기 관련 로직을 처리하는 WaitingService를 추가하자. 

