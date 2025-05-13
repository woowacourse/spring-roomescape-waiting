package roomescape.integration.fixture;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;

@Component
public class ThemeDbFixture {
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ThemeDbFixture(final JdbcTemplate jdbcTemplate) {
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme 공포() {
        return createTheme("공포", "공포 테마", "공포.jpg");
    }

    public Theme 로맨스() {
        return createTheme("로멘스", "로멘스 테마", "로멭스.jpg");
    }

    public Theme 커스텀_테마(final String name) {
        return createTheme(name, name + "테마", name + ".jpg");
    }

    public Theme createTheme(
            final String name,
            final String description,
            final String thumbnail
    ) {
        Long id = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("description", description)
                .addValue("thumbnail", thumbnail)
        ).longValue();

        return new Theme(
                id,
                new ThemeName(name),
                new ThemeDescription(description),
                new ThemeThumbnail(thumbnail)
        );
    }
}
