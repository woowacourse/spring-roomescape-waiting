package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.service.dto.PopularTheme;

@JdbcTest
@Import(JdbcThemeRepository.class)
@Sql(scripts = "/schema.sql")
class JdbcThemeRepositoryTest {

    @Autowired
    private JdbcThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("테마를 저장하면 id가 채번되어 반환된다")
    void save() {
        Theme saved = themeRepository.save(
                new Theme(null, "무인도 탈출", "설명", "https://example.com/thumb.jpg"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("무인도 탈출");
    }

    @Test
    @DisplayName("모든 테마를 조회한다")
    void findAll() {
        insertTheme("무인도 탈출");
        insertTheme("우주 정거장");

        List<Theme> themes = themeRepository.findAll();

        assertThat(themes).hasSize(2);
    }

    @Test
    @DisplayName("id로 테마를 조회한다")
    void findById() {
        long id = insertTheme("무인도 탈출");

        assertThat(themeRepository.findById(id)).isPresent();
        assertThat(themeRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("id로 테마를 삭제한다")
    void deleteById() {
        long id = insertTheme("무인도 탈출");

        themeRepository.deleteById(id);

        assertThat(themeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("id 존재 여부를 확인한다")
    void existsById() {
        long id = insertTheme("무인도 탈출");

        assertThat(themeRepository.existsById(id)).isTrue();
        assertThat(themeRepository.existsById(999L)).isFalse();
    }

    @Test
    @DisplayName("이름 존재 여부를 확인한다")
    void existsByName() {
        insertTheme("무인도 탈출");

        assertThat(themeRepository.existsByName("무인도 탈출")).isTrue();
        assertThat(themeRepository.existsByName("우주 정거장")).isFalse();
    }

    @Test
    @DisplayName("최근 일주일간 예약된 인기 테마를 예약 수 내림차순으로 반환한다")
    void findPopular() {
        long popularThemeId = insertTheme("무인도 탈출");
        long lessPopularThemeId = insertTheme("우주 정거장");
        long timeId = insertTime(LocalTime.of(10, 0));
        LocalDate recent = LocalDate.now().minusDays(1);
        insertReservation("브라운", recent, timeId, popularThemeId);
        insertReservation("리사", recent, timeId, popularThemeId);
        insertReservation("모아", recent, timeId, lessPopularThemeId);

        List<PopularTheme> popular = themeRepository.findPopular();

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).theme().getId()).isEqualTo(popularThemeId);
        assertThat(popular.get(0).reservationCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("취소된 예약은 인기 테마 집계에서 제외된다")
    void findPopularExcludesCanceled() {
        long themeId = insertTheme("무인도 탈출");
        long timeId = insertTime(LocalTime.of(10, 0));
        LocalDate recent = LocalDate.now().minusDays(1);
        insertReservation("브라운", recent, timeId, themeId, "CONFIRMED");
        insertReservation("리사", recent, timeId, themeId, "CANCELED");
        insertReservation("모아", recent, timeId, themeId, "CANCELED");

        List<PopularTheme> popular = themeRepository.findPopular();

        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).reservationCount()).isEqualTo(1);
    }

    private long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, "설명", "https://example.com/thumb.jpg"
        );
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);
    }

    private long insertTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt.toString());
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);
    }

    private void insertReservation(String name, LocalDate date, long timeId, long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (reserver_name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, date.toString(), timeId, themeId
        );
    }

    private void insertReservation(String name, LocalDate date, long timeId, long themeId, String status) {
        jdbcTemplate.update(
                "INSERT INTO reservation (reserver_name, date, time_id, theme_id, status) VALUES (?, ?, ?, ?, ?)",
                name, date.toString(), timeId, themeId, status
        );
    }
}
