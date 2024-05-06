package roomescape.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.ReservationTime;

import javax.sql.DataSource;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = {"/drop.sql", "/schema.sql", "/data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@JdbcTest
class ReservationTimeRepositoryTest {

    private final ReservationTimeRepository timeRepository;

    @Autowired
    ReservationTimeRepositoryTest(DataSource dataSource) {
        this.timeRepository = new H2ReservationTimeRepository(dataSource);
    }

    @Test
    @DisplayName("모든 예약 시간 목록을 조회한다.")
    void findAll() {
        // given
        List<ReservationTime> expected = List.of(
                new ReservationTime(1L, LocalTime.of(10, 15)),
                new ReservationTime(2L, LocalTime.of(11, 20)),
                new ReservationTime(3L, LocalTime.of(12, 25))
        );

        // when
        List<ReservationTime> actual = timeRepository.findAll();

        // then
        assertThat(actual).containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("특정 id를 통해 예약 시간 데이터를 조회한다.")
    void findByIdPresent() {
        // given
        Long id = 2L;
        ReservationTime expected = new ReservationTime(id, LocalTime.of(11, 20));

        // when
        Optional<ReservationTime> actual = timeRepository.findById(id);

        // then
        assertThat(actual).hasValue(expected);
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간 데이터를 조회할 경우 빈 값을 반환한다.")
    void findByIdNotPresent() {
        // given
        Long id = 4L;

        // when
        Optional<ReservationTime> actual = timeRepository.findById(id);

        // then
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("예약 시간 정보를 저장하면 새로운 아이디가 부여된다.")
    void save() {
        // given
        ReservationTime time = new ReservationTime(null, LocalTime.of(13, 30));
        ReservationTime expected = new ReservationTime(4L, LocalTime.of(13, 30));

        // when
        ReservationTime actual = timeRepository.save(time);

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
