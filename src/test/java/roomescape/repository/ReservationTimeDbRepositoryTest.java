package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.reservation.infrastructure.db.ReservationTimeDbRepository;
import roomescape.domain.reservation.infrastructure.db.dao.ReservationTimeH2Dao;
import roomescape.domain.reservation.model.entity.ReservationTime;
import roomescape.global.exception.ResourceNotFoundException;
import roomescape.support.JdbcTestSupport;

@Import({ReservationTimeDbRepository.class, ReservationTimeH2Dao.class})
class ReservationTimeDbRepositoryTest extends JdbcTestSupport {

    @Autowired
    private ReservationTimeDbRepository reservationTimeDbRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("존재하지 않는 id를 조회하면 예외를 발생시킨다")
    @Test
    void getByGetGetIdException() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:00");

        // when & then
        assertThatThrownBy(() -> reservationTimeDbRepository.getById(2L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @DisplayName("존재하는 id를 조회하면 조회된 예약 객체를 반환한다")
    @Test
    void getByGetGetId() {
        // given
        LocalTime startAt = LocalTime.parse("10:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", startAt);

        // when
        ReservationTime reservationTime = reservationTimeDbRepository.getById(1L);

        // then
        assertThat(reservationTime.getStartAt()).isEqualTo(startAt);
    }
}
