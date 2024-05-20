package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;

@DataJpaTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ThemeRepositoryTest {

    @Autowired
    ThemeRepository themeRepository;

    @Test
    void 주어진_테마_이름으로_등록된_테마가_있는지_확인() {
        //given
        String themeName = "테마명";
        Theme theme = new Theme(themeName, "테마설명테마설명테마설명", "썸네일이미지");
        themeRepository.save(theme);

        //when
        boolean result = themeRepository.existsByName(new ThemeName(themeName));

        //then
        assertThat(result).isTrue();
    }
}
