package roomescape.theme.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeFactory;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ThemeRepositoryTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ThemeFactory themeFactory;

    private Theme 테마() {
        return themeFactory.create("테마5", "설명", "https://image.com");
    }

    @Test
    @DisplayName("테마 저장 성공")
    void 테마_저장_성공() {
        Theme saved = themeRepository.save(테마());
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    @DisplayName("id로 테마 조회 성공")
    void id로_테마_조회_성공() {
        assertThat(themeRepository.findById(1L)).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 id 조회 시 빈 Optional 반환")
    void 존재하지_않는_id_조회() {
        assertThat(themeRepository.findById(999L)).isEmpty();
    }

    @Test
    @DisplayName("전체 테마 조회")
    void 전체_테마_조회() {
        assertThat(themeRepository.findAll()).hasSize(4);
    }

    @Test
    @DisplayName("테마 삭제 성공")
    void 테마_삭제_성공() {
        Theme saved = themeRepository.save(테마());
        themeRepository.deleteById(saved.getId());

        assertThat(themeRepository.findAll()).hasSize(4);
    }

    @Test
    @DisplayName("인기 테마 id 조회")
    void 인기_테마_id_조회() {
        List<Long> ids = themeRepository.findTopThemeIds(LocalDate.of(2026, 5, 3), LocalDate.of(2026, 5, 10), 10);
        assertThat(ids).containsExactly(1L, 2L, 3L);
    }

    @Test
    @DisplayName("ids로 테마 일괄 조회")
    void ids로_테마_일괄_조회() {
        List<Theme> themes = themeRepository.findAllByIds(List.of(1L, 2L, 3L));
        assertThat(themes).hasSize(3);
    }
}