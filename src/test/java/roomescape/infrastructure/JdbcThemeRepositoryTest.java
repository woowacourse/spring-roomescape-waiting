package roomescape.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixtures.THEME_1;
import static roomescape.TestFixtures.THEME_2;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import roomescape.domain.reservation.Theme;
import roomescape.infrastructure.persistance.JdbcThemeRepository;

class JdbcThemeRepositoryTest extends JdbcSupportTest {

    private final JdbcThemeRepository themeRepository = new JdbcThemeRepository(
            new NamedParameterJdbcTemplate(TEST_DATASOURCE), TEST_DATASOURCE);

    @Test
    void 예약_시간을_저장할_수_있다() {
        //when
        Long createdId = themeRepository.create(THEME_1);

        //then
        assertThat(themeRepository.findById(createdId)).hasValue(THEME_1);
    }

    @Test
    void id로_예약_시간을_조회할_수_있다() {
        //given
        insertTheme(THEME_1);

        //when
        Optional<Theme> theme = themeRepository.findById(1L);

        //then
        assertThat(theme).hasValue(THEME_1);
    }

    @Test
    void id값이_없다면_빈_Optional_값이_반환된다() {
        //when
        Optional<Theme> theme = themeRepository.findById(1L);

        //then
        assertThat(theme).isEmpty();
    }

    @Test
    void 전체_예약_시간을_조회할_수_있다() {
        //given
        insertTheme(THEME_1);
        insertTheme(THEME_2);

        //when
        List<Theme> themes = themeRepository.findAll();

        //then
        assertThat(themes).isEqualTo(List.of(THEME_1, THEME_2));
    }

    @Test
    void id값으로_예약_시간을_삭제한다() {
        //given
        insertTheme();

        //when
        themeRepository.deleteById(1L);

        //then
        assertThat(themeRepository.findById(1L)).isEmpty();
    }
}