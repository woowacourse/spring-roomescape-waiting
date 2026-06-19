package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.domain.Schedule;
import roomescape.repository.ScheduleDao;

@JdbcTest
@Import(ScheduleDao.class)
class ScheduleDaoTest {

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("스케줄을 저장하고 ID로 조회하면 날짜, 시간, 테마를 함께 매핑한다.")
    @Test
    void saveAndFindById() {
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        Long themeId = insertTheme("잠긴 방");
        Long scheduleId = scheduleDao.save(LocalDate.of(2026, 7, 1), timeId, themeId);

        Schedule schedule = scheduleDao.findById(scheduleId).orElseThrow();

        assertThat(schedule.getId()).isEqualTo(scheduleId);
        assertThat(schedule.getDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(schedule.getTime().getId()).isEqualTo(timeId);
        assertThat(schedule.getTime().getStartAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(schedule.getTheme().getId()).isEqualTo(themeId);
        assertThat(schedule.getTheme().getName()).isEqualTo("잠긴 방");
    }

    @DisplayName("날짜, 시간, 테마 조합으로 스케줄을 조회한다.")
    @Test
    void findByDateAndTimeIdAndThemeId() {
        Long timeId = insertReservationTime(LocalTime.of(11, 0));
        Long themeId = insertTheme("우주선");
        Long scheduleId = scheduleDao.save(LocalDate.of(2026, 7, 1), timeId, themeId);

        assertThat(scheduleDao.findByDateAndTimeIdAndThemeId(LocalDate.of(2026, 7, 1), timeId, themeId))
                .isPresent()
                .get()
                .extracting(Schedule::getId)
                .isEqualTo(scheduleId);
        assertThat(scheduleDao.findByDateAndTimeIdAndThemeId(LocalDate.of(2099, 1, 1), timeId, themeId))
                .isEmpty();
    }

    @DisplayName("날짜, 시간, 테마 조합은 유니크 제약조건을 가진다.")
    @Test
    void uniqueSchedule() {
        Long timeId = insertReservationTime(LocalTime.of(12, 0));
        Long themeId = insertTheme("마법 학교");
        scheduleDao.save(LocalDate.of(2026, 7, 1), timeId, themeId);

        assertThatThrownBy(() -> scheduleDao.save(LocalDate.of(2026, 7, 1), timeId, themeId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("스케줄 ID로 해당 schedule 행만 잠글 수 있다.")
    @Test
    void lockById() {
        Long timeId = insertReservationTime(LocalTime.of(12, 0));
        Long themeId = insertTheme("마법 학교");
        Long scheduleId = scheduleDao.save(LocalDate.of(2026, 7, 1), timeId, themeId);

        assertThat(scheduleDao.lockById(scheduleId)).isTrue();
        assertThat(scheduleDao.lockById(999L)).isFalse();
    }

    @DisplayName("시간 또는 테마를 참조하는 스케줄 존재 여부를 조회한다.")
    @Test
    void existsByTimeIdAndThemeId() {
        Long timeId = insertReservationTime(LocalTime.of(13, 0));
        Long unusedTimeId = insertReservationTime(LocalTime.of(14, 0));
        Long themeId = insertTheme("탐정 사무소");
        Long unusedThemeId = insertTheme("빈 테마");
        scheduleDao.save(LocalDate.of(2026, 7, 1), timeId, themeId);

        assertThat(scheduleDao.existsByTimeId(timeId)).isTrue();
        assertThat(scheduleDao.existsByTimeId(unusedTimeId)).isFalse();
        assertThat(scheduleDao.existsByThemeId(themeId)).isTrue();
        assertThat(scheduleDao.existsByThemeId(unusedThemeId)).isFalse();
    }

    private Long insertReservationTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt);
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)",
                name,
                name + " 설명",
                "https://example.com/" + name + ".jpg",
                20000
        );
        return jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, name);
    }
}
