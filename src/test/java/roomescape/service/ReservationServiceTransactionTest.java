package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.dao.ReservationDao;
import roomescape.dao.exception.DataConflictException;
import roomescape.domain.Reservation;
import roomescape.service.exception.ReservationConflictException;

@SpringBootTest
@Sql(scripts = {"classpath:truncate.sql", "classpath:data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationServiceTransactionTest {

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private ReservationDao reservationDao;

    @Test
    void 예약_변경_중_대기_승격이_실패하면_예약_변경과_대기_삭제가_롤백된다() {
        LocalDate originalDate = LocalDate.of(2026, 12, 31);
        LocalDate updateDate = LocalDate.of(2027, 1, 1);
        long reservationId = reservationService.save("브라운", originalDate, 1L, 1L).getId();
        long waitingId = reservationService.saveWaiting("이든", originalDate, 1L, 1L).getId();
        willThrow(new DataConflictException(new RuntimeException()))
                .given(reservationDao).save(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.update(reservationId, "브라운", updateDate, 2L))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");

        Reservation reservation = reservationDao.findById(reservationId).orElseThrow();
        assertThat(reservation.getDate()).isEqualTo(originalDate);
        assertThat(reservation.getTime().getId()).isEqualTo(1L);
        assertThat(reservationDao.findByWaitingId(waitingId)).isPresent();
        assertThat(reservationDao.existsByDateAndTimeIdAndThemeId(updateDate, 2L, 1L)).isFalse();
    }

    @Test
    void 예약_취소_중_대기_승격이_실패하면_예약_삭제와_대기_삭제가_롤백된다() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        long reservationId = reservationService.save("브라운", date, 1L, 1L).getId();
        long waitingId = reservationService.saveWaiting("이든", date, 1L, 1L).getId();
        willThrow(new DataConflictException(new RuntimeException()))
                .given(reservationDao).save(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.delete(reservationId, "브라운"))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");

        assertThat(reservationDao.findById(reservationId)).isPresent();
        assertThat(reservationDao.findByWaitingId(waitingId)).isPresent();
        assertThat(reservationDao.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).isTrue();
    }

    @Test
    void 관리자_예약_삭제_중_대기_승격이_실패하면_예약_삭제와_대기_삭제가_롤백된다() {
        LocalDate date = LocalDate.of(2026, 12, 31);
        long reservationId = reservationService.save("브라운", date, 1L, 1L).getId();
        long waitingId = reservationService.saveWaiting("이든", date, 1L, 1L).getId();
        willThrow(new DataConflictException(new RuntimeException()))
                .given(reservationDao).save(any(Reservation.class));

        assertThatThrownBy(() -> reservationService.delete(reservationId))
                .isInstanceOf(ReservationConflictException.class)
                .hasMessage("이미 예약된 시간입니다.");

        assertThat(reservationDao.findById(reservationId)).isPresent();
        assertThat(reservationDao.findByWaitingId(waitingId)).isPresent();
        assertThat(reservationDao.existsByDateAndTimeIdAndThemeId(date, 1L, 1L)).isTrue();
    }
}
