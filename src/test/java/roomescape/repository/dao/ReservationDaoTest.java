package roomescape.repository.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.repository.dto.ReservationRowDto;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ReservationDaoTest {

    private static final int INITIAL_RESERVATION_COUNT = 2;

    private final JdbcTemplate jdbcTemplate;
    private final ReservationDao reservationDao;
    private final SimpleJdbcInsert themeInsertActor;
    private final SimpleJdbcInsert reservationTimeInsertActor;
    private final SimpleJdbcInsert memberInsertActor;
    private final SimpleJdbcInsert reservationInsertActor;

    @Autowired
    public ReservationDaoTest(JdbcTemplate jdbcTemplate, ReservationDao reservationDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationDao = reservationDao;
        this.memberInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("member")
                .usingGeneratedKeyColumns("id");
        this.themeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
        this.reservationTimeInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
        this.reservationInsertActor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @BeforeEach
    void setUp() {
        initDatabase();
        insertMember("에버", "treeboss@gmail.com", "treeboss123!", "USER");
        insertMember("우테코", "wtc@gmail.com", "wtc123!", "ADMIN");
        insertTheme("n1", "d1", "t1");
        insertTheme("n2", "d2", "t2");
        insertReservationTime("1:00");
        insertReservationTime("2:00");
        insertReservation("2000-01-01", 1, 1, 1);
        insertReservation("2000-01-02", 2, 2, 2);
    }

    private void initDatabase() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE theme RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation_time RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE reservation RESTART IDENTITY");
    }

    private void insertMember(String name, String email, String password, String role) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("name", name);
        parameters.put("email", email);
        parameters.put("password", password);
        parameters.put("role", role);
        memberInsertActor.execute(parameters);
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
        reservationTimeInsertActor.execute(parameters);
    }

    private void insertReservation(String date, long timeId, long themeId, long memberId) {
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("date", date);
        parameters.put("time_id", timeId);
        parameters.put("theme_id", themeId);
        parameters.put("member_id", memberId);
        reservationInsertActor.execute(parameters);
    }

    @DisplayName("예약을 저장한다.")
    @Test
    void should_save_reservation() {
        ReservationRowDto reservationRowDto = new ReservationRowDto(LocalDate.of(2000, 1, 3), 1L, 1L, 1L);

        reservationDao.save(reservationRowDto);

        assertThat(reservationDao.findAll()).hasSize(INITIAL_RESERVATION_COUNT + 1);
    }

    @DisplayName("모든 예약을 조회한다.")
    @Test
    void should_find_all_reservations() {
        List<ReservationRowDto> reservations = reservationDao.findAll();
        assertThat(reservations).hasSize(INITIAL_RESERVATION_COUNT);
    }

    @DisplayName("특정 id의 예약을 조회한다.")
    @Test
    void should_find_reservation_by_id() {
        ReservationRowDto expected = new ReservationRowDto(1L, LocalDate.of(2000, 1, 1), 1L, 1L, 1L);

        Optional<ReservationRowDto> actual = reservationDao.findById(1L);

        assertThat(actual).isNotEmpty();
        assertThat(actual).hasValue(expected);
    }

    @DisplayName("존재하지 않는 id의 예약을 조회할 경우 빈 optional을 반환한다.")
    @Test
    void should_not_find_reservation_by_invalid_id() {
        Optional<ReservationRowDto> actual = reservationDao.findById(999L);
        assertThat(actual).isEmpty();
    }

    @DisplayName("특정 date 와 theme_id의 예약을 조회한다.")
    @Test
    void should_find_reservation_by_date_and_themeId() {
        ReservationRowDto expected = new ReservationRowDto(1L, LocalDate.of(2000, 1, 1), 1L, 1L, 1L);

        List<ReservationRowDto> actual = reservationDao.findByDateAndThemeId(LocalDate.of(2000, 1, 1), 1L);

        assertThat(actual).containsExactly(expected);
    }

    @DisplayName("두 date 사이의 예약을, 테마의 개수로 내림차순 정렬하여, 특정 개수의 테마 id를 조회한다.")
    @Test
    void should_find_themeId_between_date_and_order_by_themeId_count_and_limit() {
        List<Long> themeIds = reservationDao.findThemeIdByDateAndOrderByThemeIdCountAndLimit(
                LocalDate.of(2000, 1, 1), LocalDate.of(2000, 1, 2), 10);
        assertThat(themeIds).hasSizeLessThanOrEqualTo(10);
        assertThat(themeIds).containsExactly(1L, 2L);
    }

    @DisplayName("예약을 삭제한다.")
    @Test
    void should_delete_reservation() {
        reservationDao.deleteById(1L);
        assertThat(reservationDao.findAll()).hasSize(INITIAL_RESERVATION_COUNT - 1);
    }

    @DisplayName("특정 id를 가진 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_id() {
        boolean isExist = reservationDao.isExistById(1L);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 id를 가진 예약이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_id() {
        boolean isExist = reservationDao.isExistById(999L);
        assertThat(isExist).isFalse();
    }

    @DisplayName("특정 time_id를 가진 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_timeId() {
        boolean isExist = reservationDao.isExistByTimeId(1L);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 time_id를 가진 예약이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_timeId() {
        boolean isExist = reservationDao.isExistByTimeId(999L);
        assertThat(isExist).isFalse();
    }

    @DisplayName("특정 date와 time_id와 theme_id를 가진 예약이 존재하면 참을 반환한다.")
    @Test
    void should_return_true_when_exist_date_and_timeId() {
        boolean isExist = reservationDao.isExistByDateAndTimeIdAndThemeId(LocalDate.of(2000, 1, 1), 1L, 1L);
        assertThat(isExist).isTrue();
    }

    @DisplayName("특정 date와 time_id와 theme_id를 가진 예약이 존재하지 않으면 거짓을 반환한다.")
    @Test
    void should_return_false_when_not_exist_date_and_timeId() {
        boolean isExist = reservationDao.isExistByDateAndTimeIdAndThemeId(LocalDate.of(2000, 1, 1), 999L, 1L);
        assertThat(isExist).isFalse();
    }
}
