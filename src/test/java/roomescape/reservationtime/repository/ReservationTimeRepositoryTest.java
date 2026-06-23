package roomescape.reservationtime.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.reservationtime.domain.ReservationTime;

@JdbcTest
@Import({JdbcReservationTimeRepository.class})
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('18:00', '19:00')");
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('user1', ?, 1, 1)",
                LocalDate.now().minusDays(1));
    }

    @Test
    @DisplayName("시간 저장 성공")
    void 시간_저장_성공() {
        ReservationTime saved = timeRepository.save(ReservationTime.of(LocalTime.of(20, 0), LocalTime.of(21, 0)));
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("전체 시간 조회")
    void 전체_시간_조회() {
        List<ReservationTime> times = timeRepository.findAll();
        assertThat(times).hasSize(3);
    }

    @Test
    @DisplayName("id로 시간 조회 성공")
    void id로_시간_조회_성공() {
        assertThat(timeRepository.findById(1L)).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 id 조회 시 빈 Optional 반환")
    void 존재하지_않는_id_조회() {
        assertThat(timeRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("시간 삭제 성공")
    void 시간_삭제_성공() {
        ReservationTime saved = timeRepository.save(ReservationTime.of(LocalTime.of(20, 0), LocalTime.of(21, 0)));
        timeRepository.deleteById(saved.getId());
        assertThat(timeRepository.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("예약 가능 시간 조회")
    void 예약_가능_시간_조회() {
        List<ReservationTime> available = timeRepository.findAvailableByDateAndThemeId(LocalDate.now().minusDays(1),
                1L);
        assertThat(available).hasSize(2);
    }
}
