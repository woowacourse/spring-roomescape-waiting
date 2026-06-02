package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeAvailability;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@JdbcTest
class ReservationTimeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ReservationTimeRepository dao;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM reservation_waiting;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("DELETE FROM reservation_time;");
        this.dao = new ReservationTimeRepository(jdbcTemplate);
    }

    @Test
    void 시간_추가_테스트() {
        // given
        ReservationTime time = new ReservationTime(null, LocalTime.of(8, 0));

        // when
        ReservationTime result = dao.insert(time);

        // then
        List<ReservationTime> times = dao.findAll();
        ReservationTime savedTime = dao.findById(result.getId()).get();
        assertAll(
                () -> assertThat(result.getId()).isNotNull(),
                () -> assertThat(result.getStartAt()).isEqualTo(time.getStartAt()),
                () -> assertThat(times).hasSize(1),
                () -> assertThat(savedTime.getStartAt()).isEqualTo(time.getStartAt()));
    }

    @Test
    void 예약_삭제_테스트() {
        // given
        ReservationTime time1 = new ReservationTime(null, LocalTime.of(8, 0));
        ReservationTime time2 = new ReservationTime(null, LocalTime.of(21, 0));
        ReservationTime savedTime1 = dao.insert(time1);
        ReservationTime savedTime2 = dao.insert(time2);

        // when
        int deletedCount = dao.delete(savedTime1.getId());

        // then
        List<ReservationTime> times = dao.findAll();
        assertAll(
                () -> assertThat(deletedCount).isEqualTo(1),
                () -> assertThat(times).hasSize(1),
                () -> assertThat(dao.findById(savedTime1.getId())).isEmpty(),
                () -> assertThat(dao.findById(savedTime2.getId())).isPresent());
    }

    @Test
    void 테마와_날짜에_대한_시간별_예약_가능_여부를_조회한다() {
        // given
        LocalDate date = LocalDate.of(2026, 5, 1);
        ReservationTime time1 = dao.insert(new ReservationTime(null, LocalTime.of(8, 0)));
        ReservationTime time2 = dao.insert(new ReservationTime(null, LocalTime.of(10, 0)));
        Long themeId = insertTheme("테마");
        Long otherThemeId = insertTheme("다른 테마");
        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", date, time1.getId(), themeId);
        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "구구", date, time2.getId(), otherThemeId);
        jdbcTemplate.update(
                "INSERT INTO reservation(name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "포비", date.plusDays(1), time2.getId(), themeId);

        // when
        List<TimeAvailability> result = dao.findAvailabilitiesByThemeIdAndDate(themeId, date);

        // then
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(availability -> availability.getTime().getId())
                        .containsExactly(time1.getId(), time2.getId()),
                () -> assertThat(result).extracting(TimeAvailability::isAvailable)
                        .containsExactly(false, true));
    }

    private Long insertTheme(String name) {
        String sql = "INSERT INTO theme(name, description, thumbnail) VALUES (?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql, new String[]{"id"});
            pstmt.setString(1, name);
            pstmt.setString(2, "설명");
            pstmt.setString(3, "썸네일");
            return pstmt;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }
}
