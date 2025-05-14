package roomescape.theme.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.theme.domain.Theme;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 전체_테마_조회() {
        // when & then
        assertThat(themeRepository.findAll()).hasSize(0);
    }

    @Test
    void 새로운_테마_저장() {
        // given
        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        themeRepository.save(theme);

        // when & then
        assertThat(themeRepository.findAll()).hasSize(1);
    }

    @Test
    void 아이디를_기준으로_테마조회() {
        // given
        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        Theme savedTheme = themeRepository.save(theme);

        // when
        Theme foundTheme = themeRepository.findById(savedTheme.getId()).orElseThrow();

        // then
        assertThat(foundTheme.getId()).isEqualTo(savedTheme.getId());
    }


    @Test
    void 아이디를_기준으로_테마삭제() {
        // given
        final Theme theme = new Theme("테마1", "설명1", "썸네일1");
        Theme savedTheme = themeRepository.save(theme);

        // when
        themeRepository.deleteById(theme.getId());

        // then
        assertThat(themeRepository.findById(savedTheme.getId())).isEmpty();
    }

    // TODO: 최근 일주일동안 가장 인기있는 테마 10개 조회
    @Test
    void 최근_일주일동안_가장인기있는_테마_10개_조회() {

    }

}
