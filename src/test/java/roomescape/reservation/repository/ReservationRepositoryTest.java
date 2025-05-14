package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.repository.ReservationTimeRepository;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.user.MemberTestDataConfig;

@DataJpaTest
@Import({ThemeTestDataConfig.class,
        MemberTestDataConfig.class,
})
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeTestDataConfig themeTestDataConfig;
    @Autowired
    private MemberTestDataConfig memberTestDataConfig;

    private Reservation createReservation(int plusDays, ReservationTime time) {
        LocalDate date = LocalDate.now().plusDays(plusDays);
        return ReservationFixture.createByBookedStatus(date, time, themeTestDataConfig.getSavedTheme(),
                memberTestDataConfig.getSavedMember());
    }

    @DisplayName("예약 시간에 해당하는 예약의 존재 여부를 알 수 있다.")
    @Test
    void existsByReservationTime() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.now()));

        Reservation reservation = createReservation(1, reservationTime);
        reservationRepository.save(reservation);

        // when
        boolean actual = reservationRepository.existsByReservationTime(reservationTime);

        // then
        assertThat(actual).isTrue();
    }
}
