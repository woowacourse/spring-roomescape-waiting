package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.TestFixture.ROOM_THEME1;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.Theme;
import roomescape.exception.NotFoundException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @BeforeEach
    void setUp() {
        List<Theme> themes = themeRepository.findAll();
        for (Theme theme : themes) {
            themeRepository.deleteById(theme.getId());
        }
    }

    @DisplayName("테마를 저장한다.")
    @Test
    void save() {
        // given
        Theme theme = ROOM_THEME1;
        // when
        Theme savedTheme = themeRepository.save(theme);
        // then
        assertAll(
                () -> assertThat(savedTheme.getName()).isEqualTo(theme.getName()),
                () -> assertThat(savedTheme.getDescription()).isEqualTo(
                        theme.getDescription()),
                () -> assertThat(savedTheme.getThumbnail()).isEqualTo(theme.getThumbnail())
        );
    }

    @DisplayName("저장된 모든 테마를 보여준다.")
    @Test
    void findAll() {
        // given & when
        List<Theme> themes = themeRepository.findAll();
        // then
        assertThat(themes).isEmpty();
    }

    @DisplayName("테마를 삭제한다.")
    @Test
    void deleteTheme() {
        // given
        Theme theme = ROOM_THEME1;
        Theme savedTheme = themeRepository.save(theme);
        // when
        themeRepository.deleteById(savedTheme.getId());
        // then
        assertThat(themeRepository.findAll()).isEmpty();
    }

    @DisplayName("해당 id의 테마를 보여준다.")
    @Test
    void findById() {
        // given
        Theme theme = ROOM_THEME1;
        Theme savedTheme = themeRepository.save(theme);
        // when
        Theme findTheme = themeRepository.findById(savedTheme.getId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다."));
        // then
        assertAll(
                () -> assertThat(findTheme.getId()).isEqualTo(savedTheme.getId()),
                () -> assertThat(findTheme.getName()).isEqualTo(savedTheme.getName()),
                () -> assertThat(findTheme.getDescription()).isEqualTo(
                        savedTheme.getDescription()),
                () -> assertThat(findTheme.getThumbnail()).isEqualTo(
                        savedTheme.getThumbnail())
        );
    }
}
