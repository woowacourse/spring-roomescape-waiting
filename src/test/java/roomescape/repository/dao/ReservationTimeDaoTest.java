package roomescape.repository.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.model.ReservationTime;
import roomescape.service.dto.ReservationTimeDto;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationTimeDaoTest {

    private static final int INITIAL_TIME_COUNT = 2;

    private final JdbcTemplate jdbcTemplate;
    private final ReservationTimeDao reservationTimeDao;
    private final SimpleJdbcInsert timeInsertActor;

    @Autowired
    public ReservationTimeDaoTest(JdbcTemplate jdbcTemplate, ReservationTimeDao reservationTimeDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationTimeDao = reservationTimeDao;
        this.timeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertReservationTime("1:00");
        insertReservationTime("2:00");
    }

    private void initDatabase() {
        jdbcTemplate.execute("ALTER TABLE reservation_time SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
    }

    private void insertReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void should_save_reservation_time() {
        ReservationTimeDto reservationTimeDto = new ReservationTimeDto(LocalTime.of(3, 0));
        ReservationTime time = ReservationTime.from(reservationTimeDto);
        reservationTimeDao.save(time);
        assertThat(reservationTimeDao.findAll()).hasSize(INITIAL_TIME_COUNT + 1);
    }

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void should_find_all_reservation_times() {
        List<ReservationTime> reservationTimes = reservationTimeDao.findAll();
        assertThat(reservationTimes).hasSize(INITIAL_TIME_COUNT);
    }

    @DisplayName("특정 id의 예약 시간을 조회한다.")
    @Test
    void should_find_reservation_time_by_id() {
        Optional<ReservationTime> actual = reservationTimeDao.findById(1);

        ReservationTime expected = new ReservationTime(1, LocalTime.of(1, 0));
        assertThat(actual).isNotEmpty();
        assertThat(actual).hasValue(expected);
    }

    @DisplayName("예약 시간을 삭제한다.")
    @Test
    void should_delete_reservation_time() {
        reservationTimeDao.deleteById(1);
        assertThat(reservationTimeDao.findAll()).hasSize(INITIAL_TIME_COUNT - 1);
    }

    @DisplayName("특정 id의 예약 시간이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_id() {
        Boolean isExist = reservationTimeDao.isExistById(1);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 id의 예약 시간이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_id() {
        Boolean isExist = reservationTimeDao.isExistById(999);
        assertThat(isExist).isFalse();
    }

    @DisplayName("특정 startAt의 예약 시간이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_startAt() {
        Boolean isExist = reservationTimeDao.isExistByStartAt(LocalTime.of(1, 0));
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 startAt의 예약 시간이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_startAt() {
        Boolean isExist = reservationTimeDao.isExistByStartAt(LocalTime.of(9, 0));
        assertThat(isExist).isFalse();
    }
}
