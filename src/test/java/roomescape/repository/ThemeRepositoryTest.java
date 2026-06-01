package roomescape.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(ThemeRepository.class)
class ThemeRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void 테마를_저장하면_생성된_id와_테마_정보를_반환한다() {
        Theme saved = themeRepository.save(new Theme("공포방", "무서운방입니다.", "image-url"));

        assertThat(saved.getId()).isPositive();
        assertThat(saved.getName()).isEqualTo("공포방");
        assertThat(saved.getDescription()).isEqualTo("무서운방입니다.");
        assertThat(saved.getThumbnail()).isEqualTo("image-url");
    }

    @Test
    void 존재하는_id로_조회하면_테마를_반환한다() {
        Theme saved = themeRepository.save(new Theme("공포방", "무서운방입니다.", "image-url"));

        Theme result = themeRepository.findById(saved.getId()).orElseThrow();

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("공포방");
        assertThat(result.getDescription()).isEqualTo("무서운방입니다.");
        assertThat(result.getThumbnail()).isEqualTo("image-url");
    }

    @Test
    void 존재하지_않는_id로_조회하면_빈_Optional을_반환한다() {
        assertThat(themeRepository.findById(999L)).isEmpty();
    }

    @Test
    void 여러_테마를_저장한_뒤_전체_조회하면_모든_테마를_반환한다() {
        themeRepository.save(new Theme("공포방", "무서운방입니다.", "image-url"));
        themeRepository.save(new Theme("추리방", "단서를 찾아 탈출합니다.", "mystery-image-url"));

        List<Theme> result = themeRepository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void 지난_7일간_인기_테마_10개를_조회한다() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");

        for (int i = 1; i <= 12; i++) {
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                    "테마" + i, "설명" + i, "thumb" + i + ".png");

            int reservationCount = 13 - i;
            for (int j = 0; j < reservationCount; j++) {
                String date = LocalDate.now().minusDays(1L + (j % 7)).toString();
                long timeId = 1L + (j / 7);
                jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                        "예약자" + i + "_" + j, date, timeId, (long) i);
            }
        }

        List<Theme> themes = themeRepository.findPopularThemes(LocalDate.now().minusDays(7L), LocalDate.now(), 10);

        assertThat(themes).hasSize(10);
        assertThat(themes.get(0).getName()).isEqualTo("테마1");
        assertThat(themes.get(9).getName()).isEqualTo("테마10");
    }

    @Test
    void 예약_가능한_시간을_조회한다() {
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail) VALUES (?, ?, ?)",
                "공포", "무서움", "thumb1.png");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "11:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "12:00");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "13:00");

        String recentDate = LocalDate.now().toString();
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "A", recentDate, 1L, 1L);

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "B", recentDate, 3L, 1L);

        List<Long> reservedTimeIds = themeRepository.findReservedTimeIds(1L, LocalDate.now());

        assertThat(reservedTimeIds.size()).isEqualTo(2L);
        assertThat(reservedTimeIds.get(0)).isEqualTo(1L);
        assertThat(reservedTimeIds.get(1)).isEqualTo(3L);
    }

    @Test
    void 존재하는_id로_삭제하면_해당_테마가_삭제된다() {
        Theme saved = themeRepository.save(new Theme("공포방", "무서운방입니다.", "image-url"));

        themeRepository.deleteById(saved.getId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM theme WHERE id = ?", Integer.class, saved.getId());
        assertThat(count).isZero();
    }
}
