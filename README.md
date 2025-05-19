- EntityId가 toString으로 DB에 저장되는지 확인 및 필요한 경우 오버라이딩
- 예약 대기 Waiting 클래스
  - 새로 table이 필요한가? - reservation과 구분해야 하나?
    - 이미 예약되어있는 테마에 대기표 뽑고 기다리는 기능
      - 자기 자신의 예약에 대기를 걸 수 없음.
    - 예약이 취소되는 경우 자동으로 가장 낮은 번호가 예약 상태가 된다.

### Waiting이 Reservation을 Id로 참조
- 추가가 간편하다

- Reservation.user를 제외한 정보를 공유할 수 있음.
- 참조 무결성에 의해 삭제가 불편
  - Reservation1에 Waiting 1, 2, 3이 참조가 걸려 있는 상황
  - Reservation1 취소(삭제) 불가능, 참조 무결성
    - Waiting 가장 빠른 것 조회, (Waiting1)
    - 참조하는 Reservation1에서 필요한 정보를 추출, Waiting의 user만 갈아껴서 Reservation2를 생성
    - Reservation2로 나머지 모든 Waiting(2, 3) 의 참조를 변경
    - Reservation1 삭제
