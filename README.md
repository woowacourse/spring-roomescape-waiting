- EntityId가 toString으로 DB에 저장되는지 확인 및 필요한 경우 오버라이딩
- 예약 대기 Waiting 클래스
  - 새로 table이 필요한가? - reservation과 구분해야 하나?
    - 이미 예약되어있는 테마에 대기표 뽑고 기다리는 기능
      - 자기 자신의 예약에 대기를 걸 수 없음.
    - 예약이 취소되는 경우 자동으로 가장 낮은 번호가 예약 상태가 된다.

### Waiting이 Reservation을 참조

- Waiting - ManyToOne - Reservation
- 추가가 간편하다

- Reservation이 List<Waiting>을 들고 있고, OneToMany를 걸기
- Waiting(Many)이 Reservation(One)을 들고 있기
- 예약대기는 Id(random UUID)가 아니라 Long id를 걸어줘야 할 것 같은데?
  - 순서가 중요하다. 아니면 지금의 형태를 유지하되, 대기 생성 일시를 필드로 가져야 함
  1. UUID사용 + 대기 생성 일시 추가
  - 구조 통일 가능, 애플리케이션이 null 없이 온전한 ID 생성 가능
  - 대기 생성 일시를 기록해야 함. -> 필요할 수도 있음.
  2. GeneratedValue로 순서대로 받기
  - id가 DB 의존적, 통일성 깨짐, 애플리케이션 내에서 id = null 인 상태를 허용함.
### 결론 : 그냥 1. 유지하고 registeredAt 추가하기

---
- Reservation.user를 제외한 정보를 공유할 수 있음.
- 참조 무결성에 의해 예약 삭제가 불편
  - Reservation1에 Waiting 1, 2, 3이 참조가 걸려 있는 상황
  - Reservation1 취소(삭제) 불가능, 참조 무결성
    - Waiting 가장 빠른 것 조회, (Waiting1)
    - 참조하는 Reservation1에서 필요한 정보를 추출, Waiting의 user만 갈아껴서 Reservation2를 생성
    - Reservation2로 나머지 모든 Waiting(2, 3) 의 참조를 변경
    - Reservation1 삭제
  - 대기 기능이 생겼으니 어쩔수 없는 흐름, 서비스 단에서 이루어지면 된다. 
---
## Waiting을 왜 나눠야 돼?
Waiting이 Reservation을 들고 있는 경우 대기 추가를 하려면
1. 요청 (예약날짜, 시간, 테마) 로 이미 존재하는 Reservation을 탐색
2. 탐색한 Reservation의 Id로 Waiting을 생성해서 DB에 등록해야 함.
3. 이것처럼 모든 과정이 번거로움. 최소 2번씩 DB 조회가 필요. Waiting이 Reservation과 뗄 수 없는 관계이기 때문.
4. ReservationTime 과의 차이는 ReservationTime은 Reservation과 무관하게 관리될 수 있음.
5. BUT Waiting은 Reservation 이 없으면 생성도 안되지 않나? Entity 로 관리되어야 하나? -> Waiting은 Reservation이 아직 확정되지 않은 '상태'에 가깝다.
6. Waiting을 분리하지 말고, Reservation 에 필드로 Status를 추가하자.
   - 장점 : 기존 Reservation 메서드를 그대로 재활용 가능
   - 단점 : 대기 관련 로직의 추가로 Reservation Service의 책임이 늘어남(클래스가 뚱뚱해짐)
     - 해결 : Waiting의 경우를 처리하는 Service를 따로 파자.

## 결론
- [x] Reservation에 필드 status, createdAt을 추가
  - [ ] Reservation에 status 요청 필드 추가하기 
- [ ] 대기 관련 로직을 처리하는 WaitingService를 추가하자. 

