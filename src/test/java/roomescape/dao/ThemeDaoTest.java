package roomescape.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import roomescape.domain.AvailableTime;
import roomescape.domain.Theme;

class ThemeDaoTest {

    private EmbeddedDatabase dataSource;
    private JdbcTemplate jdbcTemplate;
    private ThemeDao themeDao;

    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:data.sql")
                .build();
        jdbcTemplate = new JdbcTemplate(dataSource);
        themeDao = new ThemeDao(jdbcTemplate);
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    @Test
    void findAll_전체_테마_조회() {
        assertThat(themeDao.findAll()).isNotEmpty();
    }

    @Test
    void findById_존재하는_id이면_반환() {
        Optional<Theme> result = themeDao.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("공포의 저택");
    }

    @Test
    void findById_존재하지_않는_id이면_empty() {
        Optional<Theme> result = themeDao.findById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void findPopularThemes_인기_테마_순위_조회() {
        clearReservations();
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now().minusDays(1);
        saveReservation("김철수", from, 3, 1);
        saveReservation("이영희", from.plusDays(1), 5, 1);
        saveReservation("박민준", from.plusDays(2), 7, 1);
        saveReservation("최수진", from.plusDays(3), 4, 1);
        saveReservation("정다은", from.plusDays(4), 8, 1);
        saveReservation("강현수", from.plusDays(1), 6, 4);
        saveReservation("윤지원", from.plusDays(2), 9, 4);
        saveReservation("임서준", from.plusDays(3), 11, 4);
        saveReservation("한지아", from.plusDays(4), 3, 4);
        saveReservation("김철수", from.plusDays(2), 2, 3);
        saveReservation("이영희", from.plusDays(5), 6, 3);
        saveReservation("박민준", to, 10, 3);
        saveReservation("최수진", from.plusDays(4), 4, 2);
        saveReservation("강현수", to, 8, 2);

        List<Theme> themes = themeDao.findPopularThemes(4, from, to);

        assertThat(themes).hasSize(4);
        assertThat(themes.get(0).getName()).isEqualTo("공포의 저택");
        assertThat(themes.get(1).getName()).isEqualTo("탐정 사무소");
        assertThat(themes.get(2).getName()).isEqualTo("마법사의 연구실");
        assertThat(themes.get(3).getName()).isEqualTo("우주 정거장");
    }

    @Test
    void findAvailableTimeById_예약된_시간은_false_나머지는_true() {
        clearReservations();
        LocalDate date = LocalDate.now().plusDays(1);
        saveReservation("김철수", date, 3, 1);

        List<AvailableTime> times = themeDao.findAvailableTimeById(1L, date);

        AvailableTime bookedSlot = times.stream()
                .filter(t -> t.time().getStartAt().getHour() == 12)
                .findFirst()
                .orElseThrow();
        assertThat(bookedSlot.available()).isFalse();
        assertThat(times.stream().filter(AvailableTime::available).count())
                .isEqualTo(times.size() - 1);
    }

    @Test
    void save_테마_저장() {
        long id = themeDao.save("새로운 테마", "설명", "https://example.com/img.jpg");

        assertThat(themeDao.findById(id)).isPresent();
    }

    @Test
    void delete_테마_삭제() {
        long id = themeDao.save("임시 테마", "설명", "https://example.com/img.jpg");

        int deletedCount = themeDao.delete(id);

        assertThat(deletedCount).isEqualTo(1);
        assertThat(themeDao.findById(id)).isEmpty();
    }

    @Test
    void delete_존재하지_않는_테마이면_0_반환() {
        int deletedCount = themeDao.delete(999L);

        assertThat(deletedCount).isZero();
    }

    private void clearReservations() {
        jdbcTemplate.update("DELETE FROM reservation");
    }

    private void saveReservation(String name, LocalDate date, long timeId, long themeId) {
        jdbcTemplate.update(
                """
                        INSERT INTO reservation (name, date, created_at, time_id, theme_id)
                        VALUES (?, ?, ?, ?, ?)
                        """,
                name, date, LocalDateTime.now(), timeId, themeId
        );
    }
}
