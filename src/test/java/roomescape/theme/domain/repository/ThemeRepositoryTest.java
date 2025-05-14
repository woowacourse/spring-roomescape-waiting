package roomescape.theme.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.theme.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
class ThemeRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("테마를 저장한다")
    @Test
    void save() {
        // given
        String name = "무서운방";
        String description = "덜덜";
        String thumbnail = "무서운 사진";
        Theme theme = new Theme(name, description, thumbnail);

        // when
        themeRepository.save(theme);
        Iterable<Theme> themes = themeRepository.findAll();

        // then
        assertThat(themes).extracting(Theme::getName, Theme::getDescription, Theme::getThumbnail)
                .containsExactlyInAnyOrder(tuple(name, description, thumbnail));
    }
}
