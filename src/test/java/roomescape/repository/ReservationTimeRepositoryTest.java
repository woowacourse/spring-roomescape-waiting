package roomescape.repository;

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
public class ReservationTimeRepositoryTest {

    private static final int INITIAL_TIME_COUNT = 2;

    private final JdbcTemplate jdbcTemplate;
    private final ReservationTimeRepository reservationTimeRepository;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert timeInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;

    @Autowired
    public ReservationTimeRepositoryTest(JdbcTemplate jdbcTemplate, ReservationTimeRepository reservationTimeRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.timeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.reservationInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertTheme("n1", "d1", "t1");
        insertTheme("n2", "d2", "t2");
        insertReservationTime("1:00");
        insertReservationTime("2:00");
        insertReservation("2000-01-01", 1, 1, 1);
        insertReservation("2000-01-02", 2, 2, 2);
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
    }

    private void insertTheme(String name, String description, String thumbnail) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("description", description);
        parameters.put("thumbnail", thumbnail);
        themeInsertActor.execute(parameters);
    }

    private void insertReservationTime(String startAt) {
        Map<String, Object> parameters = new HashMap<>(1);
        parameters.put("start_at", startAt);
        timeInsertActor.execute(parameters);
    }

    private void insertReservation(String date, long timeId, long themeId, long memberId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("member_id", memberId);
        reservationInsertActor.execute(parameters);
    }

    @DisplayName("모든 예약 시간을 조회한다.")
    @Test
    void should_find_all_reservation_times() {
        List<ReservationTime> times = reservationTimeRepository.findAllReservationTimes();
        assertThat(times).hasSize(INITIAL_TIME_COUNT);
    }

    @DisplayName("특정 id의 예약 시간을 조회한다.")
    @Test
    void should_find_reservation_time_by_id() {
        ReservationTime expected = new ReservationTime(1, LocalTime.of(1, 0));

        Optional<ReservationTime> actual = reservationTimeRepository.findReservationById(1);

        assertThat(actual).isNotEmpty();
        assertThat(actual).hasValue(expected);
    }

    @DisplayName("예약 시간을 저장한 후 저장된 시간을 반환한다.")
    @Test
    void should_save_reservation_time() {
        ReservationTimeDto reservationTimeDto = new ReservationTimeDto(LocalTime.of(3, 0));
        ReservationTime before = ReservationTime.from(reservationTimeDto);

        Optional<ReservationTime> actual = reservationTimeRepository.saveReservationTime(before);

        ReservationTime after = new ReservationTime(3, LocalTime.of(3, 0));
        assertThat(actual).isNotEmpty();
        assertThat(actual).hasValue(after);
    }

    @DisplayName("예약 시간을 삭제한다.")
    @Test
    void should_delete_reservation_time() {
        reservationTimeRepository.deleteReservationTimeById(1);
        assertThat(reservationTimeRepository.findAllReservationTimes()).hasSize(INITIAL_TIME_COUNT - 1);
    }

    @DisplayName("특정 id를 가진 예약 시간이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_id() {
        boolean isExist = reservationTimeRepository.isExistReservationTimeById(1);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 id를 가진 예약 시간이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_id() {
        boolean isExist = reservationTimeRepository.isExistReservationTimeById(999);
        assertThat(isExist).isFalse();
    }

    @DisplayName("특정 startAt을 가진 예약 시간이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_startAt() {
        boolean isExist = reservationTimeRepository.isExistReservationTimeByStartAt(LocalTime.of(1, 0));
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 startAt을 가진 예약 시간이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_startAt() {
        boolean isExist = reservationTimeRepository.isExistReservationTimeByStartAt(LocalTime.of(9, 0));
        assertThat(isExist).isFalse();
    }

    @DisplayName("특정 time_id를 가진 예약 시간이 존재하는 경우 참을 반환한다.")
    @Test
    void should_return_true_when_exist_timeId() {
        boolean isExist = reservationTimeRepository.isExistReservationByTimeId(1);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 time_id를 가진 예약 시간이 존재하지 않는 경우 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_timeId() {
        boolean isExist = reservationTimeRepository.isExistReservationByTimeId(999);
        assertThat(isExist).isFalse();
    }
}
