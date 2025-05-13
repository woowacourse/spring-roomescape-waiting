package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.DateRange;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;

@Repository
public class ThemeRepositoryImpl implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public ThemeRepositoryImpl(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Theme save(
            final ThemeName name,
            final ThemeDescription description,
            final ThemeThumbnail thumbnail) {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");

        SqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("name", name.name())
                .addValue("description", description.description())
                .addValue("thumbnail", thumbnail.thumbnail());

        Long id = jdbcInsert.executeAndReturnKey(parameters).longValue();

        return new Theme(id, name, description, thumbnail);
    }

    public void deleteById(final Long id) {
        String sql = "delete from theme where id = ?";
        jdbcTemplate.update(sql, id);
    }

    public List<Theme> findAll() {
        String sql = "select * from theme";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new Theme(
                resultSet.getLong("id"),
                new ThemeName(resultSet.getString("name")),
                new ThemeDescription(resultSet.getString("description")),
                new ThemeThumbnail(resultSet.getString("thumbnail"))
        ));
    }

    public Optional<Theme> findById(final Long id) {
        String sql = "select * from theme where id = ?";
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new Theme(
                        resultSet.getLong("id"),
                        new ThemeName(resultSet.getString("name")),
                        new ThemeDescription(resultSet.getString("description")),
                        new ThemeThumbnail(resultSet.getString("thumbnail"))
                ), id)
                .stream()
                .findFirst();
    }

    public List<Theme> findPopularThemeDuringAWeek(final long limit, final DateRange dateRange) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail,
                    COUNT(r.id) AS reservation_count
                FROM
                    theme t
                LEFT JOIN
                    reservation r ON t.id = r.theme_id
                WHERE
                    r.date IS NOT NULL AND
                    PARSEDATETIME(r.date, 'yyyy-MM-dd') >= PARSEDATETIME(?, 'yyyy-MM-dd')
                    AND PARSEDATETIME(r.date, 'yyyy-MM-dd') < PARSEDATETIME(?, 'yyyy-MM-dd')
                GROUP BY
                    t.id
                ORDER BY
                    reservation_count DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> new Theme(
                resultSet.getLong("id"),
                new ThemeName(resultSet.getString("name")),
                new ThemeDescription(resultSet.getString("description")),
                new ThemeThumbnail(resultSet.getString("thumbnail"))
        ), dateRange.getStartDate(), dateRange.getEndDate(), limit);
    }
}
