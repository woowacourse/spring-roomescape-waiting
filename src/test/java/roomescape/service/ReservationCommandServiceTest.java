package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.FixedClockConfig;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.exception.DuplicateException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.repository.ReservationDao;
import roomescape.repository.WaitingDao;

@SpringBootTest
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-fixture.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationCommandServiceTest {

    // reservation-fixture.sql 기준 (fixed clock: 2026-05-05):
    // id=2: user_b / 2026-06-05 / time2 / theme1 (미래)
    // id=3: user_c / 2026-06-05 / time1 / theme1 (미래)
    // 2026-06-05 / time1 / theme2 슬롯은 비어 있음

    @Autowired
    private ReservationCommandService reservationCommandService;
    @Autowired
    private WaitingDao waitingDao;
    @Autowired
    private WaitingQueryService waitingQueryService;
    @MockitoSpyBean
    private ReservationDao reservationDao;

    @Test
    @DisplayName("이미 예약된 슬롯에는 예약을 생성할 수 없다.")
    void createDuplicateSlot() {
        assertThatThrownBy(() ->
                reservationCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 1L))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("비어 있는 슬롯에는 예약 생성에 성공한다.")
    void createSuccess() {
        Reservation created = reservationCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        assertThat(created.id()).isNotNull();
        assertThat(created.owner().name()).isEqualTo("new-user");
    }

    @Test
    @DisplayName("존재하지 않는 예약은 취소할 수 없다.")
    void cancelNonExistent() {
        assertThatThrownBy(() ->
                reservationCommandService.cancel(999L, "user_b"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 예약은 변경할 수 없다.")
    void updateNonExistent() {
        assertThatThrownBy(() ->
                reservationCommandService.update(999L, "user_b", LocalDate.of(2026, 7, 1), 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("이미 예약된 슬롯으로는 변경할 수 없다.")
    void updateToDuplicateSlot() {
        // id=2(user_b)를 id=3(user_c)이 점유한 2026-06-05/time1/theme1로 변경 시도
        assertThatThrownBy(() ->
                reservationCommandService.update(2L, "user_b", LocalDate.of(2026, 6, 5), 1L))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("예약 취소 시 같은 슬롯의 대기 1번이 예약으로 전환되고 대기 목록에서 사라진다.")
    void cancelPromotesFirstWaiting() {
        reservationCommandService.cancel(3L, "user_c");

        assertThat(reservationDao.findAllByName(new Member("user_e"))).hasSize(1);
        assertThat(waitingDao.findById(3L)).isEmpty();
    }

    @Test
    @DisplayName("대기 없는 예약 취소 시 예약만 삭제된다.")
    void cancelWithoutWaitingDeletesReservationOnly() {
        Reservation reservation = reservationCommandService.create("new-user", LocalDate.of(2026, 6, 5), 1L, 2L);

        reservationCommandService.cancel(reservation.id(), "new-user");

        assertThat(reservationDao.findById(reservation.id())).isEmpty();
        assertThat(waitingDao.findById(2L)).isPresent();
    }

    @Test
    @DisplayName("대기 N명 중 1번만 예약으로 전환되고 나머지 대기의 순번이 당겨진다.")
    void cancelPromotesOnlyFirstWaiting() {
        reservationCommandService.cancel(3L, "user_c");

        assertThat(reservationDao.findAllByName(new Member("user_e"))).hasSize(1);
        assertThat(reservationDao.findAllByName(new Member("user_b"))).hasSize(1);
        assertThat(waitingDao.findById(4L)).isPresent();
        assertThat(waitingQueryService.getByName("user_b").getFirst().rank()).isEqualTo(1);
    }

    @Test
    @DisplayName("관리자 삭제 시 같은 슬롯의 대기 1번이 예약으로 전환된다.")
    void deletePromotesFirstWaiting() {
        reservationCommandService.delete(3L);

        assertThat(reservationDao.findAllByName(new Member("user_e"))).hasSize(1);
        assertThat(waitingDao.findById(3L)).isEmpty();
    }

    @Test
    @DisplayName("승격 중 예약 저장 실패 시 예약 삭제와 대기 삭제가 함께 롤백된다.")
    void cancelRollsBackWhenPromotionFails() {
        doThrow(new DuplicateException("승격 실패"))
                .when(reservationDao)
                .save(any(Reservation.class));

        assertThatThrownBy(() -> reservationCommandService.cancel(3L, "user_c"))
                .isInstanceOf(DuplicateException.class);

        assertThat(reservationDao.findById(3L)).isPresent();
        assertThat(waitingDao.findById(3L)).isPresent();
    }
}
