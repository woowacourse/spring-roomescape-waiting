package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationdate.ReservationDateRepository;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@DataJpaTest
class JpaReservationRepositoryTest {

    private static final LocalDate PLAY_DAY = LocalDate.of(2026, 5, 15);
    private static final LocalTime START_AT = LocalTime.of(10, 0);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationDateRepository reservationDateRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ReservationDate reservationDate;
    private ReservationTime reservationTime;
    private Theme theme;

    @BeforeEach
    void setUp() {
        reservationDate = reservationDateRepository.save(ReservationDate.createWithoutId(PLAY_DAY));
        reservationTime = reservationTimeRepository.save(ReservationTime.createWithoutId(START_AT));
        theme = themeRepository.save(Theme.createWithoutId("테마", "설명", "url"));
    }

    @Test
    @DisplayName("이름으로 예약 시작 시각이 지나지 않은 예약을 조회한다.")
    void findUpcomingByName() {
        Reservation pastReservation = Reservation.createWithoutId("테스터", reservationDate, reservationTime, theme);
        reservationRepository.save(pastReservation);

        ReservationDate futureDate = ReservationDate.of(102L, LocalDate.of(2026, 5, 15));
        ReservationTime futureTime = reservationTimeRepository.save(ReservationTime.createWithoutId(LocalTime.of(10, 1)));
        Theme futureTheme = themeRepository.save(Theme.createWithoutId("미래테마", "설명", "url"));
        futureDate = reservationDateRepository.save(ReservationDate.createWithoutId(futureDate.getPlayDay()));
        reservationRepository.save(Reservation.createWithoutId("테스터", futureDate, futureTime, futureTheme));

        List<Reservation> reservations = reservationRepository.findUpcomingByName(
                "테스터",
                LocalDate.of(2026, 5, 15),
                LocalTime.of(10, 0)
        );

        assertThat(reservations).singleElement()
                .extracting(reservation -> reservation.getTime().getStartAt())
                .isEqualTo(LocalTime.of(10, 1));
    }

    @Test
    @DisplayName("예약 조회 후 시간 필드 접근 시 발생하는 SQL을 관찰한다.")
    void observeLazyLoadingSqlWhenAccessTime() {
        Reservation reservation = reservationRepository.save(
                Reservation.createWithoutId("쿠키", reservationDate, reservationTime, theme)
        );
        entityManager.flush();
        entityManager.clear();

        Reservation found = reservationRepository.findById(reservation.getId()).orElseThrow();
        LocalTime startAt = found.getTime().getStartAt();

        assertThat(startAt).isEqualTo(START_AT);
    }
}
