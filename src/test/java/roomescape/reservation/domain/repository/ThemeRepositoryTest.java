package roomescape.reservation.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.fixture.ThemeFixture.getTheme1;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.reservation.domain.Theme;
import roomescape.util.RepositoryTest;

@DisplayName("테마 레포지토리 테스트")
class ThemeRepositoryTest extends RepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @DisplayName("테마를 저장한다.")
    @Test
    void save() {
        //given & when
        Theme theme = themeRepository.save(getTheme1());

        //then
        assertAll(() -> assertThat(theme.getId()).isNotNull(),
                () -> assertThat(theme.getName()).isEqualTo(getTheme1().getName()),
                () -> assertThat(theme.getDescription()).isEqualTo(getTheme1().getDescription()),
                () -> assertThat(theme.getThumbnail()).isEqualTo(getTheme1().getThumbnail())
        );
    }
}
