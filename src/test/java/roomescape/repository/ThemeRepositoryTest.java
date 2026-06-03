package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;

@JdbcTest
@Import(ThemeRepository.class)
@Sql(scripts = "/clear.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ThemeRepositoryTest {

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired ThemeRepository themeRepository;

    @Nested
    class save {

        @Test
        void 저장_후_id가_부여된_객체를_반환() {
            // given
            Theme theme = Theme.create("방탈출 테마", "테스트용 테마 설명입니다.", "https://img.com");

            // when
            Theme saved = themeRepository.save(theme);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("방탈출 테마");
            assertThat(saved.getDescription()).isEqualTo("테스트용 테마 설명입니다.");
        }
    }

    @Nested
    class findAll {

        @Test
        void 저장된_모든_테마를_반환() {
            // given
            themeRepository.save(Theme.create("테마A", "테마A 설명입니다.", "https://a.com"));
            themeRepository.save(Theme.create("테마B", "테마B 설명입니다.", "https://b.com"));

            // when
            List<Theme> result = themeRepository.findAll();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        void 저장된_테마가_없으면_빈_목록을_반환() {
            // when
            List<Theme> result = themeRepository.findAll();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findById {

        @Test
        void 존재하는_id면_테마를_반환() {
            // given
            Theme saved = themeRepository.save(Theme.create("방탈출 테마", "테스트용 테마 설명입니다.", "https://img.com"));

            // when
            Optional<Theme> result = themeRepository.findById(saved.getId());

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getName()).isEqualTo("방탈출 테마");
        }

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환() {
            // when
            Optional<Theme> result = themeRepository.findById(999L);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class findPopularThemes {

        @Test
        void 예약이_많은_테마_순으로_반환() {
            // given
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                    "인기테마", "인기 테마 설명입니다.", "https://popular.com");
            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                    "비인기테마", "비인기 테마 설명입니다.", "https://unpopular.com");
            jdbcTemplate.update("INSERT INTO reservation_time (start_at, end_at) VALUES (?, ?)",
                    LocalTime.of(10, 0), LocalTime.of(11, 0));
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                    "검프", LocalDate.now().minusDays(3), 1, 1);
            jdbcTemplate.update("INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                    "검프", LocalDate.now().minusDays(2), 1, 1);

            // when
            List<Theme> result = themeRepository.findPopularThemes(LocalDate.now().minusDays(7), LocalDate.now());

            // then
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getName()).isEqualTo("인기테마");
        }

        @Test
        void 예약이_없으면_이름순으로_반환() {
            // given
            themeRepository.save(Theme.create("나테마", "나 테마 설명입니다.", "https://n.com"));
            themeRepository.save(Theme.create("가테마", "가 테마 설명입니다.", "https://g.com"));

            // when
            List<Theme> result = themeRepository.findPopularThemes(LocalDate.now().minusDays(7), LocalDate.now());

            // then
            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getName()).isEqualTo("가테마");
        }
    }

    @Nested
    class deleteById {

        @Test
        void 존재하는_id면_삭제_후_true를_반환() {
            // given
            Theme saved = themeRepository.save(Theme.create("방탈출 테마", "테스트용 테마 설명입니다.", "https://img.com"));

            // when
            boolean result = themeRepository.deleteById(saved.getId());

            // then
            assertThat(result).isTrue();
            assertThat(themeRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        void 존재하지_않는_id면_false를_반환() {
            // when
            boolean result = themeRepository.deleteById(999L);

            // then
            assertThat(result).isFalse();
        }
    }
}
