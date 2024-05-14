package roomescape.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.InitialDataFixture.INITIAL_RESERVATION_TIME_COUNT;
import static roomescape.InitialDataFixture.NOT_RESERVATION_TIME;
import static roomescape.InitialDataFixture.RESERVATION_TIME_1;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.exceptions.ValidationException;
import roomescape.reservation.domain.ReservationTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql(scripts = {"/schema.sql", "/initial_test_data.sql"})
class ReservationTimeH2RepositoryTest {

    @Autowired
    private ReservationTimeH2Repository reservationTimeH2Repository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("ReservationTime을 저장한다.")
    void save() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(12, 0));

        ReservationTime saved = reservationTimeH2Repository.save(reservationTime);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("id에 맞는 ReservationTime을 제거한다.")
    void delete() {
        reservationTimeH2Repository.delete(NOT_RESERVATION_TIME.getId());

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_time", Integer.class);

        assertThat(count).isEqualTo(INITIAL_RESERVATION_TIME_COUNT - 1);
    }

    @Test
    @DisplayName("참조되어 있는 시간을 삭제하는 경우 예외가 발생한다.")
    void deleteReferencedGetTime() {
        assertThatThrownBy(() -> reservationTimeH2Repository.delete(RESERVATION_TIME_1.getId()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("모든 ReservationTime을 찾는다.")
    void findAll() {
        List<ReservationTime> found = reservationTimeH2Repository.findAll();

        assertThat(found).hasSize(INITIAL_RESERVATION_TIME_COUNT);
    }

    @Test
    @DisplayName("id에 맞는 ReservationTime을 찾는다.")
    void findBy() {
        ReservationTime found = reservationTimeH2Repository.findById(RESERVATION_TIME_1.getId()).get();

        assertThat(found.getStartAt()).isEqualTo(RESERVATION_TIME_1.getStartAt());
    }

    @Test
    @DisplayName("존재하지 않는 id가 들어오면 빈 Optional 객체를 반환한다.")
    void findEmpty() {
        Optional<ReservationTime> reservationTime = reservationTimeH2Repository.findById(-1L);

        assertThat(reservationTime.isEmpty()).isTrue();
    }
}
