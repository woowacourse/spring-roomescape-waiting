package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.ReservationTime;

@JdbcTest
@Import(ReservationTimeJdbcRepository.class)
class ReservationTimeJdbcRepositoryTest {

    @Autowired
    private ReservationTimeJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void save_저장하면_생성된_id를_반환한다() {
        ReservationTime time = new ReservationTime(null, LocalTime.of(10, 0));

        Long id = repository.save(time);

        assertThat(id).isPositive();
    }

    @Test
    void findById_저장한_시간을_조회한다() {
        Long id = repository.save(new ReservationTime(null, LocalTime.of(10, 30)));

        ReservationTime found = repository.findById(id).orElseThrow();

        assertThat(found.getId()).isEqualTo(id);
        assertThat(found.getStartAt()).isEqualTo(LocalTime.of(10, 30));
    }

    @Test
    void findById_없는_id이면_Optional_empty를_반환한다() {
        assertThat(repository.findById(9999L)).isEmpty();
    }

    @Test
    void findAll_저장된_모든_시간을_반환한다() {
        repository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        repository.save(new ReservationTime(null, LocalTime.of(11, 0)));

        List<ReservationTime> all = repository.findAll();

        assertThat(all).hasSize(2);
        assertThat(all).extracting(ReservationTime::getStartAt)
                .containsExactlyInAnyOrder(LocalTime.of(10, 0), LocalTime.of(11, 0));
    }

    @Test
    void findAll_페이징은_id_오름차순으로_limit과_offset을_적용한다() {
        Long id1 = repository.save(new ReservationTime(null, LocalTime.of(10, 0)));
        Long id2 = repository.save(new ReservationTime(null, LocalTime.of(11, 0)));
        Long id3 = repository.save(new ReservationTime(null, LocalTime.of(12, 0)));

        List<ReservationTime> firstPage = repository.findAll(2, 0);
        List<ReservationTime> secondPage = repository.findAll(2, 2);

        assertThat(firstPage).extracting(ReservationTime::getId).containsExactly(id1, id2);
        assertThat(secondPage).extracting(ReservationTime::getId).containsExactly(id3);
    }

    @Test
    void deleteById_삭제된_시간은_조회되지_않는다() {
        Long id = repository.save(new ReservationTime(null, LocalTime.of(10, 0)));

        repository.deleteById(id);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(1) FROM reservation_time WHERE id = ?", Integer.class, id);
        assertThat(count).isZero();
    }
}
