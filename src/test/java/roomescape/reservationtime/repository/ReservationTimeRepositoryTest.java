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
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.domain.ReservationTimeFactory;

@JdbcTest(properties = "spring.sql.init.data-locations=")
@Import({JdbcReservationTimeRepository.class, ReservationTimeFactory.class})
class ReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ReservationTimeFactory reservationTimeFactory;

    private Long timeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('18:00', '19:00')");
        jdbcTemplate.update("INSERT INTO member (name, email, password) VALUES ('user1', 'user1@test.com', '1234')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url) VALUES ('테마A', '설명A', 'https://a.com')");

        timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = '10:00:00'", Long.class);
        Long memberId = jdbcTemplate.queryForObject("SELECT id FROM member WHERE email = 'user1@test.com'", Long.class);
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마A'", Long.class);

        jdbcTemplate.update("INSERT INTO reservation (member_id, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                memberId, LocalDate.now().minusDays(1), timeId, themeId);
    }

    @DisplayName("시간을 저장하면 ID가 부여된다.")
    @Test
    void 시간_저장_성공() {
        ReservationTime saved = timeRepository.save(
                reservationTimeFactory.create(LocalTime.of(20, 0), LocalTime.of(21, 0)));
        assertThat(saved.getId()).isNotNull().isPositive();
    }

    @DisplayName("전체 시간 목록을 조회한다.")
    @Test
    void 전체_시간_조회() {
        assertThat(timeRepository.findAll()).hasSize(3);
    }

    @DisplayName("ID로 시간을 조회하면 해당 시간 정보가 반환된다.")
    @Test
    void ID로_시간_조회_성공() {
        ReservationTime time = timeRepository.findById(timeId).get();
        assertThat(time.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("시간을 삭제하면 더 이상 조회되지 않는다.")
    @Test
    void 시간_삭제_성공() {
        ReservationTime saved = timeRepository.save(
                reservationTimeFactory.create(LocalTime.of(20, 0), LocalTime.of(21, 0)));
        timeRepository.deleteById(saved.getId());
        assertThat(timeRepository.findById(saved.getId())).isEmpty();
    }

    @DisplayName("예약이 있는 시간이면 예약 존재 여부가 true다.")
    @Test
    void 예약_존재하는_시간() {
        assertThat(timeRepository.existsReservationByTimeId(timeId)).isTrue();
    }

    @DisplayName("날짜·테마로 예약 가능한 시간을 조회한다.")
    @Test
    void 예약_가능_시간_조회() {
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마A'", Long.class);
        List<ReservationTime> available = timeRepository.findAvailableByDateAndThemeId(
                LocalDate.now().minusDays(1), themeId);
        assertThat(available).hasSize(2);
    }
}
