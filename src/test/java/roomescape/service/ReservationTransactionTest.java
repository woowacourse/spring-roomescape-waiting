package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.ServiceTest;
import roomescape.dao.ReservationDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.request.UpdateReservationRequest;

/*
학습용 - 트랜잭션 테스트

 * [검증하려는 트랜잭션 롤백 시나리오]

 *   update():
      1. reservationDao.update()     ← 예약 슬롯 변경
      2. reservationDao.save()       ← 대기 → 예약 전환 (승격)
      3. waitingDao.delete()         ← 대기 삭제  ← 여기서 예외 발생

 *   delete():
      1. reservationDao.delete()     ← 예약 삭제
      2. reservationDao.save()       ← 대기 → 예약 전환 (승격)
      3. waitingDao.delete()         ← 대기 삭제  ← 여기서 예외 발생

 * waitingDao.delete() 에서 RuntimeException 이 발생하면 @Transactional 에 의해 1, 2번 변경이 모두 롤백되어야 한다.

 * [검증 전략 - @MockitoSpyBean]
    waitingDao 를 Spy 로 교체 → delete() 호출 시 RuntimeException 을 던지도록 설정
    나머지 메서드(save, findById 등)는 실제 구현을 그대로 사용

   -> 수정, 삭제했던 예약이 다시 존재해야 한다 & 대기 1번도 롤백되어야 한다
      즉, @Transactional이 선언한 작업 단위가 실제로 원자적으로 처리되는지 검증한다.
 */
class ReservationTransactionTest extends ServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationDao reservationDao;

    @MockitoSpyBean
    private WaitingDao waitingDao;

    @Test
    void 대기자_승격_과정에서_예외가_발생하면_예약_수정도_함께_롤백된다() {
        // given
        LocalDateTime currentDateTime = LocalDateTime.of(2026, 5, 1, 10, 0);
        LocalDate originalDate = LocalDate.of(2026, 7, 10);
        LocalDate newDate = LocalDate.of(2026, 7, 20);

        ReservationTime time = fixtureGenerator.saveReservationTime(LocalTime.of(10, 0));
        Theme theme = fixtureGenerator.saveTheme("테마", "설명", "https://thumbnail");

        Reservation reservation = fixtureGenerator.saveReservation("예약자", originalDate, time, theme);
        Waiting waiting = fixtureGenerator.saveWaiting("대기자", originalDate, time, theme, currentDateTime);

        doThrow(new RuntimeException("예상치 못한 예외"))
                .when(waitingDao).delete(anyLong());


        // when
        assertThatThrownBy(() -> reservationService.update(
                reservation.getId(),
                new UpdateReservationRequest(newDate, time.getId()),
                currentDateTime))
                .isInstanceOf(RuntimeException.class);

        // then
        Reservation foundReservation = reservationDao.findById(reservation.getId()).orElseThrow();
        assertThat(foundReservation.getDate()).isEqualTo(originalDate);

        Waiting foundWaiting = waitingDao.findFirstBySlot(reservation.getSlot().getId()).orElseThrow();
        assertThat(foundWaiting.getName()).isEqualTo(waiting.getName());
    }

    @Test
    void 대기자_승격_과정에서_예외가_발생하면_예약_삭제도_함께_롤백된다() {
        // given
        LocalDate reservationDate = LocalDate.of(2026, 7, 10);
        ReservationTime time = fixtureGenerator.saveReservationTime(LocalTime.of(10, 0));
        Theme theme = fixtureGenerator.saveTheme("테마", "설명", "https://thumbnail");
        LocalDateTime currentDateTime = LocalDateTime.of(2026, 5, 1, 10, 0);

        Reservation reservation = fixtureGenerator.saveReservation("예약자", reservationDate, time, theme);
        Waiting waiting = fixtureGenerator.saveWaiting("대기자", reservationDate, time, theme, currentDateTime);

        doThrow(new RuntimeException("예상치 못한 예외"))
                .when(waitingDao).delete(anyLong());

        // when
        assertThatThrownBy(() -> reservationService.delete(reservation.getId(), currentDateTime))
                .isInstanceOf(RuntimeException.class);

        // then
        assertAll(
                () -> assertThat(reservationDao.findById(reservation.getId())).isPresent(),
                () -> assertThat(waitingDao.findFirstBySlot(reservation.getSlot().getId()).get()).isEqualTo(waiting)
        );
    }
}
