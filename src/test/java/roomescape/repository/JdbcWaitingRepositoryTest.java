package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Session;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.domain.Waiting;
import roomescape.repository.mapper.DomainRowMapperFactory;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcWaitingRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcWaitingRepository jdbcWaitingRepository;
    private JdbcSessionRepository jdbcSessionRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        DomainRowMapperFactory factory = new DomainRowMapperFactory();
        jdbcWaitingRepository = new JdbcWaitingRepository(jdbcTemplate, factory);
        jdbcSessionRepository = new JdbcSessionRepository(jdbcTemplate, factory);
        saveDependencyData(factory);
    }

    private void saveDependencyData(DomainRowMapperFactory factory) {
        JdbcTimeSlotRepository timeRepo = new JdbcTimeSlotRepository(jdbcTemplate, factory);
        JdbcThemeRepository themeRepo = new JdbcThemeRepository(jdbcTemplate, factory);
        savedTimeSlot = timeRepo.save(new TimeSlot(1L, LocalTime.of(10, 0)));
        savedTheme = themeRepo.save(new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com"));
    }

    private Session createSavedSlot() {
        return jdbcSessionRepository.save(Session.transientOf(LocalDate.now(), savedTimeSlot, savedTheme));
    }

    @Test
    @DisplayName("예약 대기를 저장한다.")
    void save() {
        Waiting waiting = new Waiting(null, "브라운", createSavedSlot(), null);
        jdbcWaitingRepository.save(waiting);
    }

    @Test
    @DisplayName("저장된 예약 대기 존재를 확인하면, 참을 반환한다.")
    void existsWaiting() {
        Session session = createSavedSlot();
        jdbcWaitingRepository.save(new Waiting(null, "브라운", session, null));
        assertThat(jdbcWaitingRepository.isExists(new Waiting(null, "브라운", session, null))).isEqualTo(true);
    }

    @Test
    @DisplayName("저장되지 않은 예약 대기 존재를 확인하면, 거짓을 반환한다.")
    void notExistsWaiting() {
        Waiting waiting = new Waiting(null, "브라운", createSavedSlot(), null);
        assertThat(jdbcWaitingRepository.isExists(waiting)).isEqualTo(false);
    }

    @Test
    @DisplayName("예약 대기를 삭제한다.")
    void deleteById() {
        Waiting waiting = new Waiting(null, "브라운", createSavedSlot(), null);
        jdbcWaitingRepository.save(waiting);
        jdbcWaitingRepository.deleteById(1L);
    }

    @Test
    @DisplayName("예약 대기 순번을 계산한다.")
    void calculateWaitingNumber() {
        Session session = createSavedSlot();
        Waiting waiting1 = new Waiting(null, "브라운", session, null);
        Waiting waiting2 = new Waiting(null, "워니", session, null);
        jdbcWaitingRepository.save(waiting1);
        jdbcWaitingRepository.save(waiting2);
        assertThat(jdbcWaitingRepository.calculateWaitingNumber(waiting2)).isEqualTo(2);
    }
}
