package roomescape.application.reservation.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.reservation.command.dto.CreateThemeCommand;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.infrastructure.error.exception.ThemeException;

class CreateThemeServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    private CreateThemeService createThemeService;

    @BeforeEach
    void setUp() {
        createThemeService = new CreateThemeService(themeRepository);
    }

    @Test
    void 테마를_생성할_수_있다() {
        // given
        CreateThemeCommand command = new CreateThemeCommand("방탈출", "재밌는 방", "image.png");

        // when
        Long id = createThemeService.register(command);

        // then
        assertThat(themeRepository.findById(id))
                .isPresent()
                .hasValueSatisfying(theme -> {
                    assertThat(theme.getName()).isEqualTo("방탈출");
                    assertThat(theme.getDescription()).isEqualTo("재밌는 방");
                    assertThat(theme.getThumbnail()).isEqualTo("image.png");
                });
    }

    @Test
    void 같은_이름의_테마를_생성할_경우_예외가_발생한다() {
        // given
        themeRepository.save(new Theme("테마1", "설명1", "image1.png"));
        CreateThemeCommand command = new CreateThemeCommand("테마1", "재밌는 방", "image.png");

        // when
        // then
        assertThatCode(() -> createThemeService.register(command))
                .isInstanceOf(ThemeException.class)
                .hasMessage("이미 같은 이름의 테마가 존재합니다.");
    }
}
