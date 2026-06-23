package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;

@JdbcTest
@Import({JdbcThemeRepository.class})
@Sql(scripts = {"/truncate.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('10:00', '11:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('14:00', '15:00')");
        jdbcTemplate.update("INSERT INTO reservation_time (start_at, finish_at) VALUES ('18:00', '19:00')");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url, price) VALUES ('테마A', '설명A', 'https://a.com', 10000)");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url, price) VALUES ('테마B', '설명B', 'https://b.com', 20000)");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url, price) VALUES ('테마C', '설명C', 'https://c.com', 30000)");
        jdbcTemplate.update("INSERT INTO theme (name, description, image_url, price) VALUES ('테마D', '설명D', 'https://d.com', 40000)");

        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u1', ?, 1, 1)", LocalDate.now().minusDays(1));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u2', ?, 1, 1)", LocalDate.now().minusDays(2));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u3', ?, 1, 1)", LocalDate.now().minusDays(3));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u1', ?, 2, 2)", LocalDate.now().minusDays(1));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u2', ?, 2, 2)", LocalDate.now().minusDays(2));
        jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('u1', ?, 3, 3)", LocalDate.now().minusDays(1));
    }

    @Test
    @DisplayName("테마 저장 성공")
    void 테마_저장_성공() {
        Theme saved = themeRepository.save(Theme.of("테마5", "설명", "https://image.com", 15000));
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPrice()).isEqualTo(15000);
    }

    @Test
    @DisplayName("id로 테마 조회 성공")
    void id로_테마_조회_성공() {
        assertThat(themeRepository.findById(1L)).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 id 조회 시 빈 Optional 반환")
    void 존재하지_않는_id_조회() {
        assertThat(themeRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("전체 테마 조회")
    void 전체_테마_조회() {
        assertThat(themeRepository.findAll()).hasSize(4);
    }

    @Test
    @DisplayName("테마 삭제 성공")
    void 테마_삭제_성공() {
        Theme saved = themeRepository.save(Theme.of("테마5", "설명", "https://image.com", 15000));
        themeRepository.deleteById(saved.getId());
        assertThat(themeRepository.findAll()).hasSize(4);
    }

    @Test
    @DisplayName("인기 테마 id 조회")
    void 인기_테마_id_조회() {
        List<Long> ids = themeRepository.findTopThemeIds(LocalDate.now().minusDays(6), LocalDate.now().minusDays(1), 10);
        assertThat(ids).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("ids로 테마 일괄 조회")
    void ids로_테마_일괄_조회() {
        List<Theme> themes = themeRepository.findAllByIds(List.of(1L, 2L, 3L));
        assertThat(themes).hasSize(3);
    }
}