package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcWaitingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcWaitingRepository jdbcWaitingRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        jdbcWaitingRepository = new JdbcWaitingRepository(jdbcTemplate);
        saveDependencyData();
    }

    private void saveDependencyData() {
        JdbcTimeSlotRepository timeRepository = new JdbcTimeSlotRepository(jdbcTemplate);
        JdbcThemeRepository themeRepository = new JdbcThemeRepository(jdbcTemplate);
        savedTimeSlot = timeRepository.save(new TimeSlot(1L, LocalTime.of(10, 0)));
        savedTheme = themeRepository.save(new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com"));
    }

    @Test
    @DisplayName("예약 대기를 저장한다.")
    void 대기_저장() {
        Waiting waiting = createWaiting();
        Waiting savedWaiting = jdbcWaitingRepository.save(waiting);
        assertThat(savedWaiting.getId()).isNotNull();
    }

    @Test
    @DisplayName("저장된 예약 대기 존재를 확인하면, 참을 반환한다.")
    void 대기_존재_검증() {
        Waiting waiting = createWaiting();
        jdbcWaitingRepository.save(waiting);
        assertThat(jdbcWaitingRepository.exists("브라운", waiting.getDate(), savedTimeSlot.getId(),
                savedTheme.getId())).isEqualTo(true);
    }

    @Test
    @DisplayName("저장되지 않은 예약 대기 존재를 확인하면, 거짓을 반환한다.")
    void 대기_미존재_검증() {
        Waiting waiting = createWaiting();
        assertThat(jdbcWaitingRepository.exists("브라운", waiting.getDate(), savedTimeSlot.getId(),
                savedTheme.getId())).isEqualTo(false);
    }

    @Test
    @DisplayName("예약 대기를 삭제한다.")
    void 식별자로_대기_삭제() {
        Waiting waiting = createWaiting();
        jdbcWaitingRepository.save(waiting);
        jdbcWaitingRepository.deleteById(1L);
    }

    private Waiting createWaiting() {
        return new Waiting(null, "브라운", LocalDate.now(), savedTimeSlot, savedTheme, LocalDateTime.now());
    }
}
