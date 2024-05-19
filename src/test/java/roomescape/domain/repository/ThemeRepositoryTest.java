package roomescape.domain.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import roomescape.domain.Theme;

@SpringBootTest
class ThemeRepositoryTest {
    @Autowired
    private ThemeRepository themeRepository;

    @AfterEach
    void tearDown() {
        themeRepository.deleteAll();
    }

    @Test
    @DisplayName("테마를 저장한다")
    void save_ShouldThemePersistence() {
        //given
        Theme theme = new Theme("name", "description", "thumbnail");

        // when
        themeRepository.save(theme);

        // then
        Assertions.assertThat(themeRepository.findAll())
                .hasSize(1);
    }

    @Test
    @DisplayName("테마의 영속성을 id로 조회할 수 있다 - 영속화 되어있는 경우")
    void findById_ShouldGetPersistence() {
        //given
        Theme theme = new Theme("name", "description", "thumbnail");
        themeRepository.save(theme);

        // when & then
        Assertions.assertThat(themeRepository.findById(theme.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("테마의 영속성을 id로 조회할 수 있다 - 영속화 되어있지 않은 경우")
    void findById_ShouldGetPersistence_WhenPersistenceDoesNotExists() {
        // when & then
        Assertions.assertThat(themeRepository.findById(0L))
                .isEmpty();
    }

    @Test
    @DisplayName("테마의 영속성을 삭제한다")
    void delete_ShouldRemovePersistence() {
        // given
        Theme theme1 = new Theme("name", "description", "thumbnail");
        Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        themeRepository.save(theme1);
        themeRepository.save(theme2);

        // when
        themeRepository.delete(theme1);

        // then
        Assertions.assertThat(themeRepository.findAll())
                .hasSize(1)
                .containsExactly(theme2);
    }

    @Test
    @DisplayName("모든 테마의 영속성을 삭제한다")
    void deleteAll_ShouldRemoveAllPersistence() {
        // given
        Theme theme1 = new Theme("name1", "description1", "thumbnail1");
        Theme theme2 = new Theme("name2", "description2", "thumbnail2");
        Theme theme3 = new Theme("name3", "description3", "thumbnail3");
        themeRepository.save(theme1);
        themeRepository.save(theme2);
        themeRepository.save(theme3);

        // when
        themeRepository.deleteAll();

        // then
        Assertions.assertThat(themeRepository.findAll())
                .isEmpty();
    }
}
