package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.FakeThemeRepository;
import roomescape.theme.repository.ThemeRepository;

class FakeReservationRepositoryTest {

    private final LocalDate futureDate = TestFixture.makeFutureDate();

    private ReservationRepository reservationRepository;
    private Reservation reservation;
    private Theme theme;
    private Member member;

    @BeforeEach
    void setUp() {
        reservationRepository = new FakeReservationRepository();
        ThemeRepository themeRepository = new FakeThemeRepository();

        theme = TestFixture.makeTheme(1L);
        member = TestFixture.makeMember();
        reservation = TestFixture.makeReservation(1L, 1L);
        themeRepository.save(theme);
    }

    @Test
    void save_shouldStoreReservation() {
        reservationRepository.save(reservation);

        assertThat(reservationRepository.findFilteredReservations(null, null, null, null))
                .hasSize(1);
    }

    @Test
    void findAll_shouldReturnAllSavedReservations() {
        Reservation reservation2 = Reservation.of(1L, futureDate, member, ReservationTime.of(1L, LocalTime.of(16, 0)),
                theme);

        reservationRepository.save(reservation);
        reservationRepository.save(reservation2);

        List<Reservation> all = reservationRepository.findFilteredReservations(null, null, null, null);
        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById_shouldRemoveReservation() {
        reservationRepository.save(reservation);
        reservationRepository.deleteById(1L);

        assertThat(reservationRepository.findFilteredReservations(null, null, null, null)).isEmpty();
    }
}
