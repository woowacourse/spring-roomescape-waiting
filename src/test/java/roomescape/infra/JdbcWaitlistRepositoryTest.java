package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.config.TestClockConfig;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waitlist;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitlistRepository;

@JdbcTest
@Import({
        JdbcReservationRepository.class,
        JdbcReservationTimeRepository.class,
        JdbcThemeRepository.class,
        JdbcWaitlistRepository.class,
        TestClockConfig.class
})
class JdbcWaitlistRepositoryTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Test
    void 대기_생성시각이_같으면_id_순서로_앞_대기를_센다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        waitlistRepository.save(new Reservation("브리", FUTURE_SECOND_DATE, reservationTime, theme));
        waitlistRepository.save(new Reservation("포비", FUTURE_SECOND_DATE, reservationTime, theme));
        Long neoId = waitlistRepository.save(new Reservation("네오", FUTURE_SECOND_DATE, reservationTime, theme));

        Waitlist neoWaitlist = waitlistRepository.findById(neoId).get();
        int count = waitlistRepository.countBefore(neoWaitlist);

        assertThat(count).isEqualTo(2);
    }

    private ReservationTime createReservationTime(LocalTime time) {
        ReservationTime reservationTime = new ReservationTime(time);
        Long id = timeRepository.save(reservationTime);
        return new ReservationTime(id, reservationTime.getStartAt());
    }

    private Theme createTheme() {
        Theme theme = new Theme("방탈출 제목", "방탈출 설명", "thumbnail.png");
        Long id = themeRepository.save(theme);
        return new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl()
        );
    }
}
