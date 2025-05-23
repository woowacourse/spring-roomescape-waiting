package roomescape.waiting.domain;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.user.MemberTestDataConfig;
import roomescape.waiting.fixture.WaitingFixture;

@DataJpaTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ReservationTimeTestDataConfig.class,
        ThemeTestDataConfig.class,
        MemberTestDataConfig.class
})
class WaitingTest {

    @Autowired
    ReservationTimeTestDataConfig reservationTimeTestDataConfig;
    @Autowired
    private ThemeTestDataConfig themeTestDataConfig;
    @Autowired
    private MemberTestDataConfig memberTestDataConfig;
    @Autowired
    private ReservationRepository reservationRepository;

    private Reservation createReservationByDate(LocalDate date) {
        return ReservationFixture.createByBookedStatus(date,
                reservationTimeTestDataConfig.getSavedReservationTime(),
                themeTestDataConfig.getSavedTheme(),
                memberTestDataConfig.getSavedUser());
    }

    @DisplayName("예약 대기 리스트에 예약 대기 객체를 추가한다")
    @Test
    void addWaiting() {
        // given
        Reservation reservation = createReservationByDate(LocalDate.now().plusDays(1));
        Reservation savedReservation = reservationRepository.save(reservation);

        // when
        WaitingFixture.createByReservation(savedReservation);

        // then
        Assertions.assertThat(savedReservation.getWaitings()).hasSize(1);
    }
}
