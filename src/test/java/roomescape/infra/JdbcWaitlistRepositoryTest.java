package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
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
    JdbcWaitlistRepository.class
})
class JdbcWaitlistRepositoryTest {

    private static final LocalDate FUTURE_SECOND_DATE = LocalDate.now().plusDays(2);
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 10, 0);
    private static final LocalTime TEN = LocalTime.of(10, 0);

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private WaitlistRepository waitlistRepository;

    @Test
    void 같은_슬롯의_대기_목록을_조회한다() {
        ReservationTime reservationTime = createReservationTime(TEN);
        Theme theme = createTheme();

        Long brieId = waitlistRepository.save(new Reservation("브리", FUTURE_SECOND_DATE, reservationTime, theme),
            CREATED_AT);
        Long pobiId = waitlistRepository.save(new Reservation("포비", FUTURE_SECOND_DATE, reservationTime, theme),
            CREATED_AT);
        Long neoId = waitlistRepository.save(new Reservation("네오", FUTURE_SECOND_DATE, reservationTime, theme),
            CREATED_AT);

        List<Waitlist> waitlists = waitlistRepository.findBySlot(
            FUTURE_SECOND_DATE,
            reservationTime.getId(),
            theme.getId()
        );

        assertThat(waitlists)
            .extracting(Waitlist::getId)
            .containsExactly(brieId, pobiId, neoId);
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
