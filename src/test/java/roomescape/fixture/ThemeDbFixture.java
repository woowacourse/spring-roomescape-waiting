package roomescape.fixture;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.domain.Theme;

@Component
public class ThemeDbFixture {

    private final SimpleJdbcInsert jdbcInsert;

    public ThemeDbFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    public Theme 공포() {
        String name = "공포";
        String description = "공포 테마";
        String thumbnail = "공포.jpg";

        Long id = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("name", name)
                .addValue("description", description)
                .addValue("thumbnail", thumbnail)
        ).longValue();

        return Theme.create(id, name, description, thumbnail);
    }

    public Theme 커스텀_테마(String customName) {
        String description = customName + "테마";
        String thumbnail = customName + ".jpg";

        Long id = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("name", customName)
                .addValue("description", description)
                .addValue("thumbnail", thumbnail)
        ).longValue();

        return Theme.create(id, customName, description, thumbnail);
    }
}
