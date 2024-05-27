package roomescape.reservation.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.Theme;

import java.util.List;

@Repository
public class CustomThemeRepositoryImpl implements CustomThemeRepository {

    private final NamedParameterJdbcTemplate template;

    public CustomThemeRepositoryImpl(final NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<Theme> findPopularThemes(
            final ReservationDate startAt,
            final ReservationDate endAt,
            final int maximumThemeCount
    ) {
        final String sql = """
                    SELECT
                        th.id, th.name, th.description, th.thumbnail
                    FROM theme AS th
                    INNER JOIN reservation AS r
                    ON r.theme_id = th.id
                    WHERE r.date BETWEEN :startAt AND :endAt
                    GROUP BY r.theme_id
                    ORDER BY COUNT(r.theme_id) DESC
                    LIMIT :maximumThemeCount
                """;

        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("startAt", startAt.getValue())
                .addValue("endAt", endAt.getValue())
                .addValue("maximumThemeCount", maximumThemeCount);

        return template.query(sql, param, itemRowMapper());
    }

    private RowMapper<Theme> itemRowMapper() {
        return ((rs, rowNum) -> new Theme(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail")
        ));
    }
}
