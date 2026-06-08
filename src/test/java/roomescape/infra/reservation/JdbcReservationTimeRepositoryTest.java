package roomescape.infra.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.reservation.ReservationTime;

@DisplayName("예약 시간 JDBC 저장소")
@JdbcTest(properties = "spring.sql.init.mode=always")
@Import(JdbcReservationTimeRepository.class)
class JdbcReservationTimeRepositoryTest {

    @Autowired
    private JdbcReservationTimeRepository timeRepository;

    @DisplayName("예약 시간을 저장할 수 있다")
    @Test
    void save() {
        // given
        ReservationTime saved = timeRepository.save(ReservationTime.create(LocalTime.of(18, 30)));

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStartAt()).isEqualTo(LocalTime.of(18, 30));
    }

    @DisplayName("예약 시간 목록을 조회할 수 있다")
    @Test
    void findAll() {
        // when & then
        assertThat(timeRepository.findAll())
                .extracting(ReservationTime::getStartAt)
                .contains(LocalTime.of(10, 0));
    }

    @DisplayName("예약 시간을 삭제할 수 있다")
    @Test
    void delete() {
        // given
        ReservationTime saved = timeRepository.save(ReservationTime.create(LocalTime.of(18, 30)));

        // when
        assertThat(timeRepository.deleteById(saved.getId())).isEqualTo(1);

        // then
        assertThat(timeRepository.findAll())
                .extracting(ReservationTime::getStartAt)
                .doesNotContain(LocalTime.of(18, 30));
    }
}
