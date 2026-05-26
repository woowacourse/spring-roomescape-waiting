package roomescape.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
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
import roomescape.service.dto.WaitingCommand;

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
    void save() {
        WaitingCommand waiting = new WaitingCommand("브라운", LocalDate.now(), 1L, 1L);
        jdbcWaitingRepository.save(waiting);
    }

    @Test
    @DisplayName("예약 대기를 삭제한다.")
    void delete() {
        WaitingCommand waiting = new WaitingCommand("브라운", LocalDate.now(), 1L, 1L);
        jdbcWaitingRepository.save(waiting);
        jdbcWaitingRepository.delete(waiting);
    }

    @Test
    @DisplayName("예약 대기 순번을 계산한다.")
    void calculateWaitingNumber() {
        WaitingCommand waiting1 = new WaitingCommand("브라운", LocalDate.now(), 1L, 1L);
        WaitingCommand waiting2 = new WaitingCommand("워니", LocalDate.now(), 1L, 1L);
        jdbcWaitingRepository.save(waiting1);
        jdbcWaitingRepository.save(waiting2);

        assertThat(jdbcWaitingRepository.calculateWaitingNumber(waiting2)).isEqualTo(2);
    }
}
