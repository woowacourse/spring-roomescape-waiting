package roomescape.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;

@DataJpaTest
class JdbcThemeRepositoryTest {

    private static final String THEME_NAME = "방탈출 제목";
    private static final String THEME_DESCRIPTION = "방탈출 설명";
    private static final String THEME_THUMBNAIL = "thumbnail.png";

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void 테마를_저장한다() {
        Long saveId = themeRepository.save(new Theme(
            THEME_NAME,
            THEME_DESCRIPTION,
            THEME_THUMBNAIL)
        ).getId();

        Optional<Theme> theme = themeRepository.findById(saveId);

        assertThat(theme).isPresent();

        assertThat(theme.get().getId()).isNotNull();
        assertThat(theme.get().getName()).isEqualTo(THEME_NAME);
        assertThat(theme.get().getDescription()).isEqualTo(THEME_DESCRIPTION);
        assertThat(theme.get().getThumbnailImageUrl()).isEqualTo(THEME_THUMBNAIL);
    }

    @Test
    void 모든_테마를_조회한다() {
        Long saveId = themeRepository.save(new Theme(
            THEME_NAME,
            THEME_DESCRIPTION,
            THEME_THUMBNAIL)
        ).getId();

        List<Theme> themes = themeRepository.findAll();

        assertThat(themes).hasSize(1);

        Theme firstResult = themes.getFirst();
        assertThat(firstResult.getId()).isEqualTo(saveId);
        assertThat(firstResult.getName()).isEqualTo(THEME_NAME);
        assertThat(firstResult.getDescription()).isEqualTo(THEME_DESCRIPTION);
        assertThat(firstResult.getThumbnailImageUrl()).isEqualTo(THEME_THUMBNAIL);
    }

    @Test
    void 예약시간을_삭제한다() {
        Long saveId = themeRepository.save(new Theme(
            THEME_NAME,
            THEME_DESCRIPTION,
            THEME_THUMBNAIL)
        ).getId();

        themeRepository.deleteById(saveId);

        assertThat(themeRepository.findById(saveId)).isEmpty();
    }

    @Test
    @Sql("/data_relative_dates.sql")
    void 최근_일주일_동안_예약_수가_많은_인기_테마_탑10_조회() {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now().minusDays(1);
        List<Theme> popularTop10Themes = themeRepository.getPopularTop10Themes(start, end);

        assertThat(popularTop10Themes)
            .hasSize(10)
            .extracting(Theme::getName)
            .containsExactly(
                "미스터리 저택",
                "해적선의 보물",
                "마법사의 탑",
                "좀비 아포칼립스",
                "고대 이집트",
                "우주 정거장",
                "시간 여행자의 실험실",
                "폐쇄 병동",
                "침몰하는 잠수함",
                "은행 금고"
            );
    }
}
