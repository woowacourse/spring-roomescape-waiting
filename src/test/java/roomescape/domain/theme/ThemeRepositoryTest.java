package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@Import(ThemeRepository.class)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long themeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, image_url) VALUES ('테마1', '설명1', 'https://example.com/1.jpg')"
        );
        themeId = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = '테마1'", Long.class);
    }

    @Nested
    @DisplayName("id로 테마 조회")
    class FindById {

        @Test
        void 존재하는_id면_테마를_반환한다() {
            Optional<Theme> result = themeRepository.findById(themeId);

            assertAll(
                    () -> assertThat(result).isPresent(),
                    () -> assertThat(result.get().getName()).isEqualTo("테마1"),
                    () -> assertThat(result.get().getDescription()).isEqualTo("설명1"),
                    () -> assertThat(result.get().getImageUrl()).isEqualTo("https://example.com/1.jpg")
            );
        }

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환한다() {
            Optional<Theme> result = themeRepository.findById(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("테마 전체 조회")
    class FindAll {

        @Test
        void 저장된_테마를_전체_조회한다() {
            jdbcTemplate.update(
                    "INSERT INTO theme (name, description, image_url) VALUES ('테마2', '설명2', 'https://example.com/2.jpg')"
            );

            List<Theme> result = themeRepository.findAll();

            assertAll(
                    () -> assertThat(result).hasSize(2),
                    () -> assertThat(result).extracting(Theme::getName)
                            .containsExactly("테마1", "테마2"),
                    () -> assertThat(result).extracting(Theme::getDescription)
                            .containsExactly("설명1", "설명2")
            );
        }
    }
}
