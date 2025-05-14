package roomescape.dao;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.model.AvailableReservationTime;
import roomescape.model.ReservationTime;

@JdbcTest
@Import(ReservationTimeJdbcDao.class)
class ReservationTimeDaoTest {
    private static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    @Autowired
    private ReservationTimeJdbcDao reservationTimeJdbcDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long savedId;
    private ReservationTime reservationTime;
    private LocalTime time;

    @BeforeEach
    void setUp() {
        this.time = LocalTime.of(12, 30);

        ReservationTime tempReservationTime = new ReservationTime(this.time);
        this.savedId = saveNewReservationTime(tempReservationTime);
        this.reservationTime = new ReservationTime(savedId, tempReservationTime.getStartAt());
    }

    @Test
    @DisplayName("모든 예약 시각을 조회한다")
    void test1() {
        List<ReservationTime> reservationTimes = reservationTimeJdbcDao.findAll();
        assertThat(reservationTimes.stream()
                .map(ReservationTime::getStartAt).toList())
                .contains(this.time);
    }

    @Test
    @DisplayName("테마를 저장한다")
    void test2() {
        // given
        LocalTime newDate = this.time.plusHours(1);
        ReservationTime newReservationTime = new ReservationTime(newDate);

        // when
        Long savedId = reservationTimeJdbcDao.saveTime(newReservationTime);

        // then
        List<ReservationTime> foundReservations = jdbcTemplate.query(
                "SELECT * FROM reservation_time where id = ?",
                RESERVATION_TIME_ROW_MAPPER,
                savedId
        );

        assertAll(
                () -> assertThat(foundReservations).hasSize(1),
                () -> assertThat(foundReservations.getFirst().getStartAt()).isEqualTo(newDate)
        );

    }


    @Test
    @DisplayName("id 를 이용해 예약 시각을 찾는다")
    void test3() {
        // when
        Optional<ReservationTime> foundReservationTime = reservationTimeJdbcDao.findById(savedId);

        // then
        assertAll(
                () -> assertThat(foundReservationTime.isPresent()).isTrue(),
                () -> assertThat(foundReservationTime.get().getStartAt()).isEqualTo(this.time)
        );
    }

    @Test
    @DisplayName("id 를 이용해 시각을 삭제한다")
    void test4() {
        // when
        reservationTimeJdbcDao.deleteTimeById(savedId);

        // then
        List<ReservationTime> foundReservationTimes = jdbcTemplate.query(
                "SELECT * FROM reservation_time where id = ?",
                RESERVATION_TIME_ROW_MAPPER,
                savedId
        );

        assertThat(foundReservationTimes).hasSize(0);
    }

    @Test
    @DisplayName("중복되는 시각이 존재하는지 확인한다")
    void test5() {
        // when
        boolean result = reservationTimeJdbcDao.isDuplicatedStartAtExisted(time);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("특정 날짜의 예약 내역에 따라 시각 별로 예약 가능 여부를 반환한다")
    void test6() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);

        jdbcTemplate.update(
                "INSERT INTO reservation (date, time_id, theme_id) VALUES (?, ?, ?)",
                date, this.savedId, 1
        );

        // when
        List<AvailableReservationTime> availableTimes = reservationTimeJdbcDao.findAvailableTimes(date.toString(), 1L);

        // then
        List<AvailableReservationTime> foundAvailableTimes = availableTimes.stream()
                .filter(time -> time.getStartAt().equals(this.time))
                .toList();

        assertAll(
                () -> assertThat(foundAvailableTimes).hasSize(1),
                () -> assertThat(foundAvailableTimes.getFirst().getAlreadyBooked()).isTrue()
        );
    }

    private Long saveNewReservationTime(ReservationTime reservationTime) {
        String sql = "INSERT INTO reservation_time (start_at) values(?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTime(1, java.sql.Time.valueOf(reservationTime.getStartAt()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
