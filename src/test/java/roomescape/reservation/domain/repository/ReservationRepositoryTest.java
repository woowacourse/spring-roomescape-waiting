package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.ReservationFixture.getNextDayReservation;
import static roomescape.fixture.ReservationTimeFixture.getNoon;
import static roomescape.fixture.ThemeFixture.getTheme1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.util.RepositoryTest;

@DisplayName("예약 레포지토리 테스트")
class ReservationRepositoryTest extends RepositoryTest {

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @DisplayName("예약을 저장한다.")
    @Test
    void save() {
        //given & when
        ReservationTime time = timeRepository.save(getNoon());
        Theme theme = themeRepository.save(getTheme1());
        Reservation reservation = reservationRepository.save(getNextDayReservation(time, theme));

        //then
        assertAll(() -> assertThat(reservation.getId()).isNotNull(),
                () -> assertThat(reservation.getDate()).isEqualTo(getNextDayReservation(time, theme).getDate()),
                () -> assertThat(reservation.getTime()).isEqualTo(time),
                () -> assertThat(reservation.getTheme()).isEqualTo(theme)
        );
    }
}
