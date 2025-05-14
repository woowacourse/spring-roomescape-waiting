package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.TestFixture;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.sql.init.data-locations="
})
class ReservationRepositoryTest {

    private static final LocalDate futureDate = TestFixture.makeFutureDate();

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member;

    private ReservationTime reservationTime;

    private Theme theme;

    @BeforeEach
    public void setup() {
        member = memberRepository.save(TestFixture.makeMember());
        reservationTime = reservationTimeRepository.save(ReservationTime.of(LocalTime.of(10, 0)));
        theme = themeRepository.save(TestFixture.makeTheme(1L));
        reservationRepository.save(Reservation.of(futureDate, member, reservationTime, theme));
    }

    @Test
    void findFilteredReservations() {
        Theme theme2 = themeRepository.save(Theme.of("논리", "셜록 논리 게임 with Vector", "image.png"));

        ReservationTime reservationTime2 = ReservationTime.of(LocalTime.of(11, 0));
        reservationTime2 = reservationTimeRepository.save(reservationTime2);

        Reservation reservation2 = Reservation.of(futureDate, member, reservationTime2, theme2);
        reservationRepository.save(reservation2);

        List<Reservation> filteredReservations = reservationRepository.findFilteredReservations(theme.getId(),
                member.getId(), futureDate,
                futureDate.plusDays(1));

        assertThat(filteredReservations.size()).isEqualTo(1);
    }

    @Test
    void existsByTimeId() {
        boolean existsByTimeId = reservationRepository.existsByTimeId(reservationTime.getId());

        assertThat(existsByTimeId).isTrue();
    }

    @Test
    void existsByThemeId() {
        boolean existsByThemeId = reservationRepository.existsByThemeId(theme.getId());

        assertThat(existsByThemeId).isTrue();
    }

    @Test
    void existsByDateAndTimeIdAndThemeId() {
        boolean existsByDateAndTimeIdAndThemeId = reservationRepository.existsByDateAndTimeIdAndThemeId(futureDate,
                theme.getId(),
                reservationTime.getId());

        assertThat(existsByDateAndTimeIdAndThemeId).isTrue();
    }

    @Test
    void findAvailableTimesByDateAndThemeId() {
        ReservationTime reservationTime2 = reservationTimeRepository.save(ReservationTime.of(LocalTime.of(11, 0)));
        ReservationTime reservationTime3 = reservationTimeRepository.save(ReservationTime.of(LocalTime.of(12, 0)));

        reservationRepository.save(Reservation.of(futureDate, member, reservationTime2, theme));
        reservationRepository.save(Reservation.of(futureDate, member, reservationTime3, theme));

        List<AvailableReservationTimeResponse> bookedTimesByDateAndThemeId = reservationRepository.findBookedTimesByDateAndThemeId(
                futureDate, theme.getId());

        assertThat(bookedTimesByDateAndThemeId.size()).isEqualTo(3);

    }
}
