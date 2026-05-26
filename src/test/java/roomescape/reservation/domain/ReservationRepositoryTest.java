package roomescape.reservation.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.config.TestTimeConfig;
import roomescape.reservation.infra.JdbcReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.infra.JdbcThemeRepository;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.time.infra.JdbcReservationTimeRepository;

@JdbcTest
@Import({
        TestTimeConfig.class,
        JdbcReservationRepository.class,
        JdbcThemeRepository.class,
        JdbcReservationTimeRepository.class
})
class ReservationRepositoryTest {

    @Autowired
    private Clock clock;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    @DisplayName("사용자 이름으로 예약 조회시 Pending 상태인 예약이 1개 있으면 대기 순번이 2번으로 조회된다.")
    void findReservationByNameTest() {
        Theme theme = Theme.builder()
                .name("판타지")
                .description("판타지래요")
                .durationTime(LocalTime.now(clock))
                .thumbnailImageUrl("https://~~~")
                .build();

        Theme savedTheme = themeRepository.save(theme);

        ReservationTime reservationTime = ReservationTime.builder()
                .startAt(LocalTime.now(clock))
                .build();

        ReservationTime savedTime = reservationTimeRepository.save(reservationTime);

        Reservation pobi = Reservation.builder()
                .name("포비")
                .status(Status.PENDING)
                .date(LocalDate.now(clock))
                .theme(savedTheme)
                .time(savedTime)
                .createdAt(LocalDateTime.now(clock)).build();

        Reservation lisa = Reservation.builder()
                .name("리사")
                .status(Status.PENDING)
                .date(LocalDate.now(clock))
                .theme(savedTheme)
                .time(savedTime)
                .createdAt(LocalDateTime.now(clock)).build();

        reservationRepository.save(pobi);
        reservationRepository.save(lisa);

        Assertions.assertThat(reservationRepository.findAllByName("리사").getFirst().pendingIndex()).isEqualTo(2);
    }
}
