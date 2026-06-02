package roomescape.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.Theme;
import roomescape.dto.PopularThemeProjection;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@ActiveProfiles("test")
@Import(ThemeDao.class)
public class ThemeDaoTest {

    private static final String INSERT_SINGLE_THEME_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나오는 공포 테마',
                    'https://images.example.com/themes/horror-house.jpg');
            """;

    private static final String INSERT_THREE_THEMES_SQL = """
            INSERT INTO theme (id, name, description, img_url)
            VALUES (1, '이든의 공포 하우스', '이든이 귀신으로 나오는 공포 테마',
                    'https://images.example.com/themes/horror-house.jpg'),
                   (2, '정콩이의 방탈출', '정콩이가 지키는 미스터리 방탈출',
                    'https://images.example.com/themes/jungkong-room.jpg'),
                   (3, '우주 정거장 탈출', '고장 난 우주 정거장에서 귀환하는 SF 테마',
                    'https://images.example.com/themes/space-station.jpg');
            """;

    @Autowired
    private ThemeDao themeDao;

    @Test
    @Sql(statements = INSERT_SINGLE_THEME_SQL)
    void ID에_해당하는_테마를_조회한다() {
        long themeId = 1L;
        Theme theme = themeDao.findById(themeId);

        assertThat(theme).isNotNull();
        assertThat(theme)
                .extracting(
                        Theme::getId,
                        Theme::getName,
                        Theme::getDescription,
                        Theme::getImgUrl
                )
                .containsExactly(
                        1L,
                        "이든의 공포 하우스",
                        "이든이 귀신으로 나오는 공포 테마",
                        "https://images.example.com/themes/horror-house.jpg"
                );
    }

    @Test
    @Sql(statements = INSERT_THREE_THEMES_SQL)
    void 모든_테마를_조회한다() {
        List<Theme> themes = themeDao.findAllThemes();

        assertThat(themes).hasSize(3);
        assertThat(themes)
                .extracting(
                        Theme::getId,
                        Theme::getName
                )
                .containsExactlyInAnyOrder(
                        tuple(1L, "이든의 공포 하우스"),
                        tuple(2L, "정콩이의 방탈출"),
                        tuple(3L, "우주 정거장 탈출")
                );
    }

    @Test
    void 테마를_추가한다() {
        long id = themeDao.insertTheme(
                "심해 탈출",
                "가라앉는 잠수함에서 탈출하는 테마",
                "https://images.example.com/themes/deep-sea.jpg"
        );

        Theme theme = themeDao.findById(id);

        assertThat(id).isPositive();
        assertThat(theme)
                .extracting(
                        Theme::getId,
                        Theme::getName,
                        Theme::getDescription,
                        Theme::getImgUrl
                )
                .containsExactly(
                        id,
                        "심해 탈출",
                        "가라앉는 잠수함에서 탈출하는 테마",
                        "https://images.example.com/themes/deep-sea.jpg"
                );
    }

    @Test
    @Sql(statements = INSERT_SINGLE_THEME_SQL)
    void 테마를_삭제한다() {
        assertThat(themeDao.delete(1L)).isEqualTo(1);
    }

    @Test
    @Sql("/popular-themes.sql")
    void 최근_1주동안_예약이_많았던_테마_상위_10개를_조회한다() {
        List<PopularThemeProjection> popularThemeProjections = themeDao.findPopularThemes(
                LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 7));

        assertThat(popularThemeProjections).hasSize(10);
        assertThat(popularThemeProjections)
                .extracting(PopularThemeProjection::name)
                .containsExactly(
                        "이든의 공포 하우스",
                        "정콩이의 방탈출",
                        "우주 정거장 탈출",
                        "고대 유적의 비밀",
                        "마법사의 서재",
                        "좀비 연구소",
                        "해적선의 보물",
                        "미스터리 호텔",
                        "시간의 문",
                        "사라진 탐정"
                );
        assertThat(popularThemeProjections)
                .extracting(PopularThemeProjection::rank)
                .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
    }
}
