package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import roomescape.domain.ReservationStatus;
import roomescape.domain.Theme;
import roomescape.repository.ThemeDao;

@JdbcTest
@Import(ThemeDao.class)
class ThemeDaoTest {

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("테마를 저장하고 전체 조회하면 도메인 객체로 매핑된다.")
    @Test
    void saveAndFindAll() {
        Long id = themeDao.save("잠긴 방", "닫힌 문을 여는 테마", "https://example.com/theme.jpg", 20000);

        List<Theme> themes = themeDao.findAll();

        assertThat(id).isNotNull().isPositive();
        assertThat(themes).hasSize(1);
        assertThat(themes.get(0).getName()).isEqualTo("잠긴 방");
        assertThat(themes.get(0).getDescription()).isEqualTo("닫힌 문을 여는 테마");
        assertThat(themes.get(0).getThumbnailUrl()).isEqualTo("https://example.com/theme.jpg");
    }

    @DisplayName("테마 이름은 유니크 제약조건을 가진다.")
    @Test
    void uniqueName() {
        themeDao.save("중복 테마", "설명", "https://example.com/theme.jpg", 20000);

        assertThatThrownBy(() -> themeDao.save("중복 테마", "다른 설명", "https://example.com/other.jpg", 20000))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @DisplayName("이름으로 테마 존재 여부를 조회한다.")
    @Test
    void existsByName() {
        themeDao.save("탐정 사무소", "사건을 해결하는 테마", "https://example.com/detective.jpg", 20000);

        assertThat(themeDao.existsByName("탐정 사무소")).isTrue();
        assertThat(themeDao.existsByName("없는 테마")).isFalse();
    }

    @DisplayName("인기 테마는 기간 내 RESERVED 예약 수 내림차순, 동률이면 ID 오름차순으로 조회한다.")
    @Test
    void findPopularThemes() {
        Long firstThemeId = insertTheme("첫 번째 테마");
        Long secondThemeId = insertTheme("두 번째 테마");
        Long thirdThemeId = insertTheme("세 번째 테마");
        Long timeId = insertReservationTime(LocalTime.of(10, 0));
        Long firstScheduleId = insertSchedule(LocalDate.of(2026, 7, 1), timeId, firstThemeId);
        Long secondScheduleId = insertSchedule(LocalDate.of(2026, 7, 2), timeId, secondThemeId);
        Long thirdScheduleId = insertSchedule(LocalDate.of(2026, 7, 3), timeId, thirdThemeId);

        insertReservation("예약1", firstScheduleId, ReservationStatus.RESERVED);
        insertReservation("예약2", firstScheduleId, ReservationStatus.RESERVED);
        insertReservation("예약3", secondScheduleId, ReservationStatus.RESERVED);
        insertReservation("취소", secondScheduleId, ReservationStatus.CANCELED);
        insertReservation("기간밖", thirdScheduleId, ReservationStatus.RESERVED);

        List<Theme> popularThemes = themeDao.findPopularThemes(
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                ReservationStatus.RESERVED,
                10
        );

        assertThat(popularThemes)
                .extracting(Theme::getName)
                .containsExactly("첫 번째 테마", "두 번째 테마");
    }

    @DisplayName("테마를 삭제하면 조회 결과에서 제외된다.")
    @Test
    void delete() {
        Long id = themeDao.save("삭제할 테마", "설명", "https://example.com/delete.jpg", 20000);

        themeDao.delete(id);

        assertThat(themeDao.findAll()).isEmpty();
    }

    private Long insertTheme(String name) {
        return themeDao.save(name, name + " 설명", "https://example.com/" + name + ".jpg", 20000);
    }

    private Long insertReservationTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class, startAt);
    }

    private Long insertSchedule(LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update("INSERT INTO schedule (date, time_id, theme_id) VALUES (?, ?, ?)", date, timeId, themeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM schedule WHERE date = ? AND time_id = ? AND theme_id = ?",
                Long.class,
                date,
                timeId,
                themeId
        );
    }

    private void insertReservation(String name, Long scheduleId, ReservationStatus status) {
        Long userId = insertUser(name);
        jdbcTemplate.update(
                "INSERT INTO reservation (user_id, schedule_id, status, updated_at) VALUES (?, ?, ?, ?)",
                userId,
                scheduleId,
                status.name(),
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );
    }

    private Long insertUser(String name) {
        String loginId = name + "-" + System.nanoTime();
        jdbcTemplate.update(
                "INSERT INTO users (login_id, name, password, role) VALUES (?, ?, ?, ?)",
                loginId,
                name,
                "password",
                "USER"
        );
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE login_id = ?", Long.class, loginId);
    }
}
