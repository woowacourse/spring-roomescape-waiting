package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.TimeSlot;
import roomescape.domain.repository.TimeSlotRepository;

@Sql(scripts = {"/test-schema.sql"})
@JdbcTest
class TimeSlotJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private TimeSlotRepository repository;

    @BeforeEach
    void setUp() {
        repository = new TimeSlotJdbcRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("타임 슬롯을 아이디로 조회한다.")
    void findById() {
        // given
        var timeSlot = readyTimeSlot();
        var savedId = repository.save(timeSlot);

        // when
        Optional<TimeSlot> found = repository.findById(savedId);

        // then
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("타임 슬롯을 저장한다.")
    void save() {
        // given
        var timeSlot = readyTimeSlot();

        // when
        repository.save(timeSlot);

        // then
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("타임 슬롯을 삭제한다.")
    void removeById() {
        // given
        var timeSlot = readyTimeSlot();
        var savedId = repository.save(timeSlot);

        // when
        repository.removeById(savedId);

        // then
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("모든 타임 슬롯을 조회한다.")
    void findAll() {
        // given
        var timeSlot1 = readyTimeSlot();
        var timeSlot2 = readyTimeSlot();
        repository.save(timeSlot1);
        repository.save(timeSlot2);

        // when
        var timeSlots = repository.findAll();

        // then
        assertThat(timeSlots).hasSize(2);
    }

    private TimeSlot readyTimeSlot() {
        return new TimeSlot(LocalTime.of(10, 0));
    }
}
