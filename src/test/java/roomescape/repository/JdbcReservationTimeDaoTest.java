package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.test.annotation.DirtiesContext;

import roomescape.model.ReservationTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JdbcReservationTimeDaoTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    private SimpleJdbcInsert insertActor;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        insertActor = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");

        insertToReservationTime("10:00");
        insertToReservationTime("11:00");
    }

    private void insertToReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        insertActor.execute(parameters);
    }

    @DisplayName("모든 예약 시간을 조회한다")
    @Test
    void should_get_reservation_times() {
        List<ReservationTime> reservationTimes = reservationTimeDao.findAllReservationTimes();
        assertThat(reservationTimes).hasSize(2);
    }

    @DisplayName("예약 시간을 추가한다")
    @Test
    void should_add_reservation_time() {
        reservationTimeDao.addReservationTime(new ReservationTime(LocalTime.of(12, 0)));
        Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation_time", Integer.class);
        assertThat(count).isEqualTo(3);
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void should_delete_reservation_time() {
        reservationTimeDao.deleteReservationTime(1);
        Integer count = jdbcTemplate.queryForObject("SELECT count(1) FROM reservation_time", Integer.class);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("아이디에 해당하는 예약 시간을 조회한다.")
    @Test
    void should_get_reservation_time() {
        ReservationTime reservationTime = reservationTimeDao.findReservationById(1);
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(10, 0));
    }

    @DisplayName("아이디가 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_id_exist() {
        long count = reservationTimeDao.countReservationTimeById(1);
        assertThat(count).isEqualTo(1);
    }

    @DisplayName("아이디가 존재하면 거짓을 반환한다.")
    @Test
    void should_return_false_when_id_not_exist() {
        long count = reservationTimeDao.countReservationTimeById(100000000);
        assertThat(count).isZero();
    }
}
