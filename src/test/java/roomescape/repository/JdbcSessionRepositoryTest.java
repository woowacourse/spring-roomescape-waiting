package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
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
import roomescape.repository.mapper.DomainRowMapperFactory;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcSessionRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcSessionRepository jdbcSessionRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        DomainRowMapperFactory factory = new DomainRowMapperFactory();
        jdbcSessionRepository = new JdbcSessionRepository(jdbcTemplate, factory);
        insertDependencyData(factory);
    }

    private void insertDependencyData(DomainRowMapperFactory factory) {
        JdbcTimeSlotRepository timeRepo = new JdbcTimeSlotRepository(jdbcTemplate, factory);
        JdbcThemeRepository themeRepo = new JdbcThemeRepository(jdbcTemplate, factory);
        savedTimeSlot = timeRepo.save(new TimeSlot(1L, LocalTime.of(10, 0)));
        savedTheme = themeRepo.save(new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com"));
    }

    @Test
    @DisplayName("세션을 저장하고 저장된 세션들의 목록을 반환한다.")
    void findAll() {
        jdbcSessionRepository.save(Session.transientOf(LocalDate.now(), savedTimeSlot, savedTheme));
        jdbcSessionRepository.save(Session.transientOf(LocalDate.now().plusDays(1), savedTimeSlot, savedTheme));
        jdbcSessionRepository.save(Session.transientOf(LocalDate.now().plusDays(2), savedTimeSlot, savedTheme));
        jdbcSessionRepository.save(Session.transientOf(LocalDate.now().plusDays(3), savedTimeSlot, savedTheme));
        jdbcSessionRepository.save(Session.transientOf(LocalDate.now().plusDays(4), savedTimeSlot, savedTheme));
        List<Session> sessions = jdbcSessionRepository.findAll();

        assertThat(sessions).hasSize(5);
    }

    @Test
    @DisplayName("세션을 저장하고 영속화된 객체를 반환한다.")
    void save() {
        Session session = Session.transientOf(LocalDate.now(), savedTimeSlot, savedTheme);
        Session savedSession = jdbcSessionRepository.save(session);
        assertThat(savedSession.getId()).isPositive();
    }

    @Test
    @DisplayName("날짜, 시간, 테마 식별자로 세션을 조회한다.")
    void findByConditions() {
        jdbcSessionRepository.save(Session.transientOf(LocalDate.now(), savedTimeSlot, savedTheme));
        Optional<Session> foundSession = jdbcSessionRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.now(), savedTimeSlot.getId(), savedTheme.getId()
        );
        assertThat(foundSession).isPresent();
    }

    @Test
    @DisplayName("식별자로 세션을 삭제한다.")
    void deleteById() {
        Session savedSession = jdbcSessionRepository.save(Session.transientOf(LocalDate.now(), savedTimeSlot, savedTheme));
        jdbcSessionRepository.deleteById(savedSession.getId());
        Optional<Session> foundSession = jdbcSessionRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.now(), savedTimeSlot.getId(), savedTheme.getId()
        );
        assertThat(foundSession).isEmpty();
    }
}
