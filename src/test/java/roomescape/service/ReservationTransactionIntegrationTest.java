package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBeans;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.FixedClockConfig;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationWaitingDao;
import roomescape.dao.dto.WaitingWithRank;
import roomescape.domain.common.UserName;
import roomescape.domain.reservation.Reservation;
import roomescape.exception.InvalidDomainStateException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/reservation-transaction-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@MockitoSpyBeans({
        @MockitoSpyBean(types = ReservationWaitingDao.class),
        @MockitoSpyBean(types = ReservationDao.class)
})
@Import(FixedClockConfig.class)
class ReservationTransactionIntegrationTest {

    private final ReservationAdminCommandService adminCommandService;
    private final ReservationQueryService reservationQueryService;
    private final WaitingQueryService waitingQueryService;
    private final ReservationWaitingDao waitingDao;
    private final ReservationDao reservationDao;

    @Autowired
    ReservationTransactionIntegrationTest(ReservationAdminCommandService adminCommandService, ReservationQueryService reservationQueryService, WaitingQueryService waitingQueryService, ReservationWaitingDao waitingDao, ReservationDao reservationDao) {
        this.adminCommandService = adminCommandService;
        this.reservationQueryService = reservationQueryService;
        this.waitingQueryService = waitingQueryService;
        this.waitingDao = waitingDao;
        this.reservationDao = reservationDao;
    }

    @Test
    @DisplayName("예약 취소 중 승격을 위한 신규 예약 생성 단계에서 예외 발생 시 모든 데이터가 롤백된다")
    void cancel_RollbackWhenCreateNewReservationFails() {
        // when
        assertThatThrownBy(() -> adminCommandService.delete(1L))
                .isInstanceOf(InvalidDomainStateException.class);

        // then
        List<Reservation> allReservation = reservationQueryService.getAllReservations();
        assertThat(allReservation).hasSize(2);

        List<Reservation> reservations = reservationQueryService.getByName(UserName.from("user_a"));
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getReservationDate()).isEqualTo(LocalDate.parse("2026-06-05"));
        assertThat(reservations.getFirst().getReservationTime().getId()).isEqualTo(1);
        assertThat(reservations.getFirst().getReservationTheme().getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약 취소 중 대기열 삭제 단계에서 예외 발생 시 롤백된다")
    void cancel_RollbackWhenDeleteWaitingFails() {
        // when
        doThrow(new RuntimeException("DB 내부 데드락 발생"))
                .when(waitingDao).delete(any());

        assertThatThrownBy(() -> adminCommandService.delete(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 내부 데드락 발생");

        // then
        List<Reservation> allReservation = reservationQueryService.getAllReservations();
        assertThat(allReservation).hasSize(2);

        List<Reservation> reservations = reservationQueryService.getByName(UserName.from("user_b"));
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getReservationDate()).isEqualTo(LocalDate.parse("2026-06-06"));
        assertThat(reservations.getFirst().getReservationTime().getId()).isEqualTo(1);
        assertThat(reservations.getFirst().getReservationTheme().getId()).isEqualTo(1);
    }

    @Test
    @DisplayName("예약 취소 중 기존 예약 삭제 단계에서 예외 발생 시 승격된 예약과 대기열 상태가 모두 원상복구 된다")
    void cancel_RollbackWhenDeleteOriginalReservationFails() {
        // when
        doThrow(new RuntimeException("DB 내부 데드락 발생"))
                .when(reservationDao).delete(any());

        assertThatThrownBy(() -> adminCommandService.delete(2L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 내부 데드락 발생");

        // then
        List<Reservation> allReservation = reservationQueryService.getAllReservations();
        assertThat(allReservation).hasSize(2);

        List<Reservation> reservations = reservationQueryService.getByName(UserName.from("user_b"));
        assertThat(reservations).hasSize(1);
        assertThat(reservations.getFirst().getReservationDate()).isEqualTo(LocalDate.parse("2026-06-06"));
        assertThat(reservations.getFirst().getReservationTime().getId()).isEqualTo(1);
        assertThat(reservations.getFirst().getReservationTheme().getId()).isEqualTo(1);

        List<WaitingWithRank> waitings = waitingQueryService.getByName(UserName.from("user_c"));
        assertThat(waitings).hasSize(1);
        assertThat(waitings.getFirst().reservationDate()).isEqualTo(LocalDate.parse("2026-06-06"));
        assertThat(waitings.getFirst().reservationTime().getId()).isEqualTo(1);
        assertThat(waitings.getFirst().reservationTheme().getId()).isEqualTo(1);

    }
}
