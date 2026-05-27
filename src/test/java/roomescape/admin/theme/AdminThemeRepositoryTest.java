package roomescape.admin.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.domain.theme.Theme;

@JdbcTest
@Import(AdminThemeRepository.class)
class AdminThemeRepositoryTest {

    @Autowired
    private AdminThemeRepository adminThemeRepository;

    @Nested
    @DisplayName("테마 저장")
    class Save {

        @Test
        void 저장하면_id가_부여된다() {
            Theme theme = Theme.of("테마1", "설명", "https://example.com/image.jpg");

            Theme saved = adminThemeRepository.save(theme);

            assertAll(
                () -> assertThat(saved.getId()).isNotNull(),
                () -> assertThat(saved.getName()).isEqualTo("테마1"),
                () -> assertThat(saved.getDescription()).isEqualTo("설명"),
                () -> assertThat(saved.getImageUrl()).isEqualTo("https://example.com/image.jpg")
            );
        }
    }

    @Nested
    @DisplayName("id로 테마 삭제")
    class DeleteById {

        @Test
        void 삭제하면_해당_테마가_조회되지_않는다() {
            Theme saved = adminThemeRepository.save(Theme.of("테마1", "설명", "https://example.com/image.jpg"));

            adminThemeRepository.deleteById(saved.getId());

            assertThat(adminThemeRepository.existsById(saved.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("id로 테마 조회")
    class FindById {

        @Test
        void 존재하는_id면_테마를_반환한다() {
            Theme saved = adminThemeRepository.save(Theme.of("테마1", "설명", "https://example.com/image.jpg"));

            Optional<Theme> result = adminThemeRepository.findById(saved.getId());

            assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result.get().getName()).isEqualTo("테마1"),
                () -> assertThat(result.get().getDescription()).isEqualTo("설명"),
                () -> assertThat(result.get().getImageUrl()).isEqualTo("https://example.com/image.jpg")
            );
        }

        @Test
        void 존재하지_않는_id면_빈_Optional을_반환한다() {
            Optional<Theme> result = adminThemeRepository.findById(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("테마 전체 조회")
    class FindAll {

        @Test
        void 저장된_테마를_전체_조회한다() {
            Theme first = adminThemeRepository.save(Theme.of("테마1", "설명1", "https://example.com/1.jpg"));
            Theme second = adminThemeRepository.save(Theme.of("테마2", "설명2", "https://example.com/2.jpg"));

            List<Theme> result = adminThemeRepository.findAll();

            assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result).extracting(Theme::getId)
                    .containsExactly(first.getId(), second.getId()),
                () -> assertThat(result).extracting(Theme::getName)
                    .containsExactly("테마1", "테마2")
            );
        }
    }

    @Nested
    @DisplayName("id로 존재 여부 조회")
    class ExistsById {

        @Test
        void 존재하는_id면_true를_반환한다() {
            Theme saved = adminThemeRepository.save(Theme.of("테마1", "설명", "https://example.com/image.jpg"));

            boolean result = adminThemeRepository.existsById(saved.getId());

            assertThat(result).isTrue();
        }

        @Test
        void 존재하지_않는_id면_false를_반환한다() {
            boolean result = adminThemeRepository.existsById(999L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("이름으로 존재 여부 조회")
    class ExistsByName {

        @Test
        void 존재하는_이름이면_true를_반환한다() {
            adminThemeRepository.save(Theme.of("테마1", "설명", "https://example.com/image.jpg"));

            boolean result = adminThemeRepository.existsByName("테마1");

            assertThat(result).isTrue();
        }

        @Test
        void 존재하지_않는_이름이면_false를_반환한다() {
            adminThemeRepository.save(Theme.of("테마1", "설명", "https://example.com/image.jpg"));

            boolean result = adminThemeRepository.existsByName("테마2");

            assertThat(result).isFalse();
        }
    }
}
