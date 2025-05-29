package roomescape.theme.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.theme.domain.Theme;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/test-data.sql")
class JpaThemeRepositoryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ThemeListCrudRepository themeRepository;

    @Nested
    @DisplayName("테마 조회")
    class FindTheme {

        @DisplayName("테마 목록을 조회할 수 있다")
        @Test
        void test1() {
            // when
            List<Theme> themes = themeRepository.findAll();

            // then
            assertThat(themes.size()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("테마 생성")
    class CreateTheme {

        @DisplayName("새 테마를 저장할 수 있다")
        @Test
        void test1() {
            // given
            Theme theme = new Theme(null, "테스트 테마", ".", "https://www.naver.jpg");

            // when
            Theme newTheme = themeRepository.save(theme);

            // then
            assertThat(newTheme.getId()).isEqualTo(2L);
        }

        @DisplayName("중복되는 테마는 저장되지 않는다")
        @Test
        void test2() {
            // given
            Theme theme = new Theme(null, "테스트 테마", ".", "https://www.naver.jpg");
            entityManager.persist(theme);
            entityManager.flush();
            entityManager.clear();

            // when
            themeRepository.save(theme);
            List<Theme> themes = themeRepository.findAll();

            // then
            assertThat(themes.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("테마 삭제")
    class DeleteTheme {

        @DisplayName("저장된 테마를 삭제할 수 있다")
        @Test
        void test1() {
            // when
            themeRepository.deleteById(1L);
            Theme expected = entityManager.find(Theme.class, 1L);

            // then
            assertThat(expected).isNull();
        }
    }
}
