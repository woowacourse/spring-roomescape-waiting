package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Time;
import roomescape.infra.JdbcTimeRepository;

@JdbcTest
@Sql("/schema.sql")
class JdbcTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcTimeRepository jdbcTimeRepository;

    @BeforeEach
    void setUp() {
        jdbcTimeRepository = new JdbcTimeRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("예약 시간을 저장하고 영속화된 객체를 반환한다.")
    void save() {
        Time time = Time.of(LocalTime.of(10, 0));
        Time savedTime = jdbcTimeRepository.save(time);
        assertThat(savedTime.getId()).isPositive();
    }

    @Test
    @DisplayName("식별자로 예약 시간 객체를 조회한다.")
    void findById() {
        Time savedTime = jdbcTimeRepository.save(Time.of(LocalTime.of(10, 0)));
        Time foundTime = jdbcTimeRepository.findById(savedTime.getId()).get();
        assertThat(foundTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    @DisplayName("모든 예약 시간 객체 목록을 조회한다.")
    void findAll() {
        jdbcTimeRepository.save(Time.of(LocalTime.of(10, 0)));
        List<Time> times = jdbcTimeRepository.findAll();
        assertThat(times).hasSize(1);
    }

    @Test
    @DisplayName("식별자로 예약 시간을 삭제한다.")
    void deleteById() {
        Time savedTime = jdbcTimeRepository.save(Time.of(LocalTime.of(10, 0)));
        jdbcTimeRepository.deleteById(savedTime.getId());
        assertThat(jdbcTimeRepository.findAll()).isEmpty();
    }
}
