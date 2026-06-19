package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Theme;
import roomescape.exception.DuplicateEntityException;
import roomescape.support.IntegrationTest;

@IntegrationTest
class JdbcThemeRepositoryIntegrationTest {
    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 테마를_저장하고_ID로_조회할_수_있다() {
        // given
        Theme theme = Theme.create("바니의 집", "바니의 집입니다", "http://image.png/image.com", 30000L);

        // when
        Theme saved = themeRepository.save(theme);

        // then
        assertThat(themeRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void 활성화된_테마_조회가_있는지_확인() {
        // given
        Theme theme = Theme.create("바니의 집", "바니의 집입니다", "http://image.png/image.com", 30000L);
        themeRepository.save(theme);

        // when
        boolean result = themeRepository.isActiveByName("바니의 집");

        // then
        assertThat(result).isTrue();
    }

    @Test
    void 활성화된_테마가_있을_때_같은_테마를_추가하면_제약_위반() {
        // given
        Theme theme = Theme.create("바니의 집", "바니의 집입니다", "http://image.png/image.com", 30000L);
        themeRepository.save(theme);

        // when & then: 무결성 위반 예외를 비즈니스 예외로 변경
        assertThatThrownBy(() -> themeRepository.save(theme))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining("이미 존재하는 테마 정보입니다.");
    }

    @Test
    void 테마_정보가_수정이_된다() {
        // given
        Theme theme = Theme.create("바니의 집", "바니의 집입니다", "http://image.png/image.com", 30000L);
        Theme savedTheme = themeRepository.save(theme);
        savedTheme.deactivate();

        // when
        themeRepository.update(savedTheme);

        // then
        Optional<Theme> found = themeRepository.findById(savedTheme.getId());
        assertThat(found).isPresent();
        assertThat(found.get().isActive()).isFalse();
    }

}
