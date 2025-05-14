package roomescape.repository.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.reservation.infrastructure.db.dao.ReservationTimeH2Dao;
import roomescape.reservation.model.entity.ReservationTime;
import roomescape.support.JdbcTestSupport;

@Import(ReservationTimeH2Dao.class)
class ReservationTimeH2DaoTest extends JdbcTestSupport {

    @Autowired
    private ReservationTimeH2Dao reservationTimeH2Dao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("저장한 모든 예약 시간을 조회할 수 있다.")
    @Test
    void selectAll() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "11:00");

        // when
        List<ReservationTime> reservationTimes = reservationTimeH2Dao.selectAll();

        // then
        assertThat(reservationTimes).hasSize(2);
    }

    @DisplayName("ReservationTime객체로 데이터를 삽입할 수 있다.")
    @Test
    void insert() {
        // given
        LocalTime startAt = LocalTime.of(10, 20);
        ReservationTime reservationTime = new ReservationTime(startAt);

        // when
        reservationTimeH2Dao.insertAndGet(reservationTime);

        // then
        List<ReservationTime> reservationTimes = reservationTimeH2Dao.selectAll();

        assertSoftly(softly -> {
            softly.assertThat(reservationTimes).hasSize(1);
            softly.assertThat(reservationTimes.getFirst().getStartAt()).isEqualTo(startAt);
        });
    }

    @DisplayName("ReservationTime객체로 데이터를 삽입 후 삽입된 데이터를 가져올 수 있다.")
    @Test
    void insertAndGet() {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(10, 20));

        // when
        ReservationTime insertedReservationTime = reservationTimeH2Dao.insertAndGet(reservationTime);

        // then
        assertSoftly(softly -> {
            softly.assertThat(insertedReservationTime.getId()).isEqualTo(reservationTime.getId());
            softly.assertThat(insertedReservationTime.getStartAt()).isEqualTo(reservationTime.getStartAt());
        });
    }

    @DisplayName("id에 해당하는 예약 시간이 없다면 빈 값을 반환한다.")
    @Test
    void noneSelect() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "11:00");

        // when
        assertThatThrownBy(() -> reservationTimeH2Dao.selectById(3L).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("id에 해당하는 예약 정보를 조회할 수 있다.")
    @Test
    void selectByGetGetId() {
        // given
        LocalTime firstTime = LocalTime.parse("10:00");
        LocalTime secondTime = LocalTime.parse("11:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", firstTime);
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", secondTime);

        // when
        Optional<ReservationTime> selectedReservationTime = reservationTimeH2Dao.selectById(2L);

        // then
        assertThatCode(selectedReservationTime::get).doesNotThrowAnyException();
        ReservationTime reservationTime = selectedReservationTime.get();

        assertThat(reservationTime.getStartAt()).isEqualTo(secondTime);
    }

    @DisplayName("id에 해당하는 예약 시간을 삭제할 수 있다.")
    @Test
    void deleteByGetGetId() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "11:00");

        // when
        reservationTimeH2Dao.deleteById(2L);

        // then
        assertThatThrownBy(() -> reservationTimeH2Dao.selectById(2L).get())
                .isInstanceOf(NoSuchElementException.class);
    }

    @DisplayName("입력한 테마id와 날짜에 해당하는 예약 시간을 모두 조회한다")
    @Test
    void selectAllByGetThemeGetGetIdAndGetDate() {
        // given
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:10");
        jdbcTemplate.update("INSERT INTO reservation_time(start_at) VALUES (?)", "10:20");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)", "이름", "설명", "썸네일");


        LocalDate date = LocalDate.now().plusDays(20);
        LocalDate anotherDate = LocalDate.now().plusDays(22);
        Long themeId = 1L;
        jdbcTemplate.update("INSERT INTO RESERVATION(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, 1L, themeId);
        jdbcTemplate.update("INSERT INTO RESERVATION(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                anotherDate, 2L, themeId);
        jdbcTemplate.update("INSERT INTO RESERVATION(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)", "1번사람",
                date, 3L, themeId);

        // when
        List<ReservationTime> reservationTimes = reservationTimeH2Dao.selectAllByThemeIdAndDate(themeId, date);

        // then
        assertSoftly(softly -> {
            softly.assertThat(reservationTimes).hasSize(2);
            softly.assertThat(reservationTimes.getFirst().getId()).isEqualTo(1L);
            softly.assertThat(reservationTimes.get(1).getId()).isEqualTo(3L);
        });
    }
}
