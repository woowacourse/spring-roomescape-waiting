package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.ReservationTestFixture;
import roomescape.global.exception.BusinessRuleViolationException;
import roomescape.reservation.application.AdminReservationTimeService;
import roomescape.reservation.model.entity.Reservation;
import roomescape.reservation.model.entity.ReservationTheme;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.reservation.model.repository.ReservationRepository;
import roomescape.reservation.model.repository.ReservationThemeRepository;
import roomescape.reservation.model.repository.ReservationTimeRepository;
import roomescape.support.IntegrationTestSupport;

class AdminReservationTimeServiceTest extends IntegrationTestSupport {

    @Autowired
    private AdminReservationTimeService adminReservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationThemeRepository reservationThemeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("예약시간 삭제시 해당 예약시간 id를 참조하고 있는 예약이 있다면 예외를 발생시킨다")
    @Test
    void delete() {
        // given
        ReservationTime reservationTime = ReservationTestFixture.getReservationTimeFixture();
        ReservationTheme reservationTheme = ReservationTestFixture.getReservationThemeFixture();
        Reservation reservation = ReservationTestFixture.createReservation(LocalDate.now().minusDays(10), reservationTime, reservationTheme);

        reservationTimeRepository.save(reservationTime);
        reservationThemeRepository.save(reservationTheme);
        reservationRepository.save(reservation);

        // when & then
        assertThatThrownBy(() -> adminReservationTimeService.delete(reservationTime.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }
}
