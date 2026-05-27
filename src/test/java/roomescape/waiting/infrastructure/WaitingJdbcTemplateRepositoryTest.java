package roomescape.waiting.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.infrastructure.ReservationTimeJdbcTemplateRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.infrastructure.ThemeJdbcTemplateRepository;
import roomescape.waiting.domain.Waiting;

@JdbcTest
@Import({
        WaitingJdbcTemplateRepository.class,
        ReservationTimeJdbcTemplateRepository.class,
        ThemeJdbcTemplateRepository.class
})
class WaitingJdbcTemplateRepositoryTest {

    private final WaitingJdbcTemplateRepository waitingRepository;
    private final ReservationTimeJdbcTemplateRepository timeRepository;
    private final ThemeJdbcTemplateRepository themeRepository;
    private final JdbcTemplate jdbcTemplate;

    private ReservationTime savedTime;
    private Theme savedTheme;

    @Autowired
    WaitingJdbcTemplateRepositoryTest(
            WaitingJdbcTemplateRepository waitingRepository,
            ReservationTimeJdbcTemplateRepository timeRepository,
            ThemeJdbcTemplateRepository themeRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.waitingRepository = waitingRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        savedTime = timeRepository.save(ReservationTime.create(LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(
                Theme.create("테스트 테마", "테스트 테마 설명", "https://good.com/thumb-nail/1")
        );
    }

    @Test
    @DisplayName("예약 대기를 저장하면 생성된 ID를 포함한 대기 객체를 반환한다")
    void save_success() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Waiting waiting = Waiting.create("브라운", date, savedTime, savedTheme);

        // when
        Waiting savedWaiting = waitingRepository.save(waiting);

        // then
        assertThat(savedWaiting.getId()).isNotNull();
        assertThat(savedWaiting.getName()).isEqualTo("브라운");
        assertThat(savedWaiting.getDate()).isEqualTo(date);
        assertThat(savedWaiting.getTime()).isEqualTo(savedTime);
        assertThat(savedWaiting.getTheme()).isEqualTo(savedTheme);
    }

    @Test
    @DisplayName("예약 대기 저장 시 waiting 테이블에 값이 저장된다")
    void save_persist_waiting_row() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        Waiting waiting = Waiting.create("브라운", date, savedTime, savedTheme);

        // when
        Waiting savedWaiting = waitingRepository.save(waiting);

        // then
        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT name, date, time_id, theme_id FROM waiting WHERE id = ?",
                savedWaiting.getId()
        );
        assertThat(row.get("NAME")).isEqualTo("브라운");
        assertThat(row.get("DATE").toString()).isEqualTo(date.toString());
        assertThat(((Number) row.get("TIME_ID")).longValue()).isEqualTo(savedTime.getId());
        assertThat(((Number) row.get("THEME_ID")).longValue()).isEqualTo(savedTheme.getId());
    }
}
