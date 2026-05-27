package roomescape.domain.waitingreservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

@JdbcTest
@Sql("/truncate.sql")
class JdbcWaitingReservationRepositoryTest {

    private static final long DATE_ID = 101L;
    private static final long TIME_ID = 201L;
    private static final long THEME_ID = 301L;
    private static final LocalDate PLAY_DAY = LocalDate.of(2026, 5, 10);
    private static final LocalTime START_AT = LocalTime.of(10, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private WaitingReservationRepository waitingReservationRepository;
    private ReservationDate date;
    private ReservationTime time;
    private Theme theme;

    @BeforeEach
    void setUp() {
        waitingReservationRepository = new JdbcWaitingReservationRepository(jdbcTemplate);

        jdbcTemplate.update("insert into reservation_date(id, play_day) values (?, ?)", DATE_ID, PLAY_DAY.toString());
        jdbcTemplate.update("insert into reservation_time(id, start_at) values (?, ?)", TIME_ID, START_AT.toString());
        jdbcTemplate.update(
            "insert into theme(id, name, content, url) values (?, ?, ?, ?)",
            THEME_ID, "공포", "테마 내용", "/themes/scary"
        );

        date = ReservationDate.of(DATE_ID, PLAY_DAY);
        time = ReservationTime.of(TIME_ID, START_AT);
        theme = Theme.of(THEME_ID, "공포", "테마 내용", "/themes/scary");
    }

    @Test
    void 같은_이름_날짜_테마_시간으로_예약_대기를_생성할_수_없다() {
        WaitingReservation waitingReservation = waiting("이산", LocalDateTime.of(2026, 5, 9, 10, 0));
        waitingReservationRepository.save(waitingReservation);

        assertThatThrownBy(() -> waitingReservationRepository.save(waitingReservation))
            .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 가장_먼저_신청한_예약_대기를_가져온다() {
        waitingReservationRepository.save(waiting("이산", LocalDateTime.of(2026, 5, 7, 10, 0)));
        waitingReservationRepository.save(waiting("고래", LocalDateTime.of(2026, 5, 8, 10, 0)));
        waitingReservationRepository.save(waiting("보예", LocalDateTime.of(2026, 5, 9, 10, 0)));

        WaitingReservation oldest = waitingReservationRepository.findOldest().orElseThrow();

        assertThat(oldest.getName()).isEqualTo("이산");
        assertThat(oldest.getDate().getId()).isEqualTo(DATE_ID);
        assertThat(oldest.getDate().getPlayDay()).isEqualTo(PLAY_DAY);
        assertThat(oldest.getTime().getId()).isEqualTo(TIME_ID);
        assertThat(oldest.getTime().getStartAt()).isEqualTo(START_AT);
        assertThat(oldest.getTheme().getId()).isEqualTo(THEME_ID);
        assertThat(oldest.getTheme().getName()).isEqualTo("공포");
        assertThat(oldest.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 7, 10, 0));
    }

    @Test
    void 예약_대기가_없으면_가장_먼저_신청한_예약_대기를_조회할_수_없다() {
        assertThat(waitingReservationRepository.findOldest()).isEmpty();
    }

    private WaitingReservation waiting(String name, LocalDateTime createdAt) {
        return WaitingReservation.createWithoutId(name, date, time, theme, createdAt);
    }

}
