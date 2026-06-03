package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.TimeSlot;
import roomescape.repository.mapper.DomainRowMapperFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcSlotRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcSlotRepository jdbcSlotRepository;
    private TimeSlot savedTimeSlot;
    private Theme savedTheme;

    @BeforeEach
    void setUp() {
        DomainRowMapperFactory factory = new DomainRowMapperFactory();
        jdbcSlotRepository = new JdbcSlotRepository(jdbcTemplate, factory);
        insertDependencyData(factory);
    }

    private void insertDependencyData(DomainRowMapperFactory factory) {
        JdbcTimeSlotRepository timeRepo = new JdbcTimeSlotRepository(jdbcTemplate, factory);
        JdbcThemeRepository themeRepo = new JdbcThemeRepository(jdbcTemplate, factory);
        savedTimeSlot = timeRepo.save(new TimeSlot(1L, LocalTime.of(10, 0)));
        savedTheme = themeRepo.save(new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com"));
    }

    @Test
    @DisplayName("슬롯을 저장하고 영속화된 객체를 반환한다.")
    void save() {
        Slot slot = Slot.transientOf(LocalDate.now(), savedTimeSlot, savedTheme);
        Slot savedSlot = jdbcSlotRepository.save(slot);
        assertThat(savedSlot.getId()).isPositive();
    }

    @Test
    @DisplayName("날짜, 시간, 테마 식별자로 슬롯을 조회한다.")
    void findByConditions() {
        jdbcSlotRepository.save(Slot.transientOf(LocalDate.now(), savedTimeSlot, savedTheme));
        Optional<Slot> foundSlot = jdbcSlotRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.now(), savedTimeSlot.getId(), savedTheme.getId()
        );
        assertThat(foundSlot).isPresent();
    }

    @Test
    @DisplayName("식별자로 슬롯을 삭제한다.")
    void deleteById() {
        Slot savedSlot = jdbcSlotRepository.save(Slot.transientOf(LocalDate.now(), savedTimeSlot, savedTheme));
        jdbcSlotRepository.deleteById(savedSlot.getId());
        Optional<Slot> foundSlot = jdbcSlotRepository.findByDateAndTimeIdAndThemeId(
                LocalDate.now(), savedTimeSlot.getId(), savedTheme.getId()
        );
        assertThat(foundSlot).isEmpty();
    }
}
