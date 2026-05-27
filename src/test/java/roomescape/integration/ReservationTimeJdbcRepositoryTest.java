package roomescape.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.ReservationTime;
import roomescape.repository.ReservationTimeJdbcRepository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(ReservationTimeJdbcRepository.class)
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class ReservationTimeJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationTimeJdbcRepository repository;

    @Test
    void save는_생성된_id를_부여한_시간을_반환한다() {
        ReservationTime saved = repository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void findAll은_시작_시간_오름차순으로_정렬해_반환한다() {
        repository.save(new ReservationTime(null, LocalTime.of(11, 0)));
        repository.save(new ReservationTime(null, LocalTime.of(9, 0)));

        List<ReservationTime> times = repository.findAll();

        assertThat(times)
                .extracting(ReservationTime::getStartAt)
                .containsExactly(LocalTime.of(9, 0), LocalTime.of(11, 0));
    }

    @Test
    void findById는_존재하는_시간을_반환한다() {
        ReservationTime saved = repository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        Optional<ReservationTime> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @Test
    void findById는_존재하지_않으면_빈_Optional을_반환한다() {
        assertThat(repository.findById(9999L)).isEmpty();
    }

    @Test
    void existsById는_저장된_시간에_대해_true를_반환한다() {
        ReservationTime saved = repository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        assertThat(repository.existsById(saved.getId())).isTrue();
    }

    @Test
    void deleteById_이후_existsById는_false를_반환한다() {
        ReservationTime saved = repository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        repository.deleteById(saved.getId());

        assertThat(repository.existsById(saved.getId())).isFalse();
    }
}
