# Cycle 1 - PR Review v1 - 수정 사항

---

## ✅ 리팩토링 할 것 목록

- [x] **1. request dto의 사용되지 않는 toDomain() 제거하기**
- [x] **2. EOF 개행 문자 추가 확인할 것.**
- [x] **3. Reservation의 도메인 제약 사항 추가하기**
- [x] **4. ReservationDao의 SELECT_WITH_JOIN 쿼리를 인라인으로 되돌리기**
- [x] **5. time_id가 없는 예약을 막기**
- [x] **6. Service에서 save() 전에 ReservationTimeDao.findById(timeId)로 존재 여부 확인하고, 없으면 도메인 예외를 던지도록 수정**
- [x] **7. ReservationDao.save()는 id만 반환하도록 변경하고, 조립 책임은 Service로 이동 (Service에서 ReservationTimeDao.findById()로 time 조회 후    
  Reservation 조립)**
- [x] **8. 중복 시간 슬롯 방지: Service에서 start_at 중복 검증 + DB UNIQUE 제약 추가**
