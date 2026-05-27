package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.PopularThemeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeSortType;

@Repository
public class PopularThemeJdbcTemplateRepository implements PopularThemeRepository {

    private static final String FIND_TOP_N_BY_PERIOD_QUERY = """
            SELECT
                theme.id,
                theme.name,
                theme.description,
                theme.thumbnail_url
            FROM theme
            JOIN reservation ON reservation.theme_id = theme.id
            WHERE reservation.date BETWEEN ? AND ?
            GROUP BY theme.id, theme.name
            ORDER BY %s
            LIMIT ?
            """;
    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) -> Theme.createRow(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("thumbnail_url")
    );

    private final JdbcTemplate jdbcTemplate;

    public PopularThemeJdbcTemplateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Theme> findTopNByPeriod(LocalDate startAt, LocalDate endAt, ThemeSortType sortType, Long limit) {
        return jdbcTemplate.query(
                FIND_TOP_N_BY_PERIOD_QUERY.formatted(getSortSql(sortType)),
                THEME_ROW_MAPPER,
                startAt,
                endAt,
                limit
        );
    }

    private String getSortSql(ThemeSortType sortType) {
        if (sortType.equals(ThemeSortType.POPULAR)) {
            return "COUNT(reservation.id) DESC";
        }
        return "theme.id ASC";
    }
}
