package roomescape.reservation.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import roomescape.common.template.AbstractDaoTemplate;
import roomescape.reservation.domain.Theme;

@Repository
public class ThemeDao extends AbstractDaoTemplate<Theme> {

    private static final String TABLE_NAME = "theme";
    private static final String BASE_SELECT_SQL = "select id, name, description, thumbnail from theme";

    @Autowired
    public ThemeDao(final NamedParameterJdbcTemplate jdbcTemplate, final DataSource dataSource) {
        super(jdbcTemplate, TABLE_NAME, dataSource);
    }

    @Override
    protected RowMapper<Theme> rowMapper() {
        return this::mapRowToTheme;
    }

    public List<Theme> findAll() {
        return jdbcTemplate.query(BASE_SELECT_SQL, rowMapper());
    }

    public Optional<Theme> findById(final Long id) {
        String sql = BASE_SELECT_SQL + " where id = :id";
        return executeQueryForObject(sql, Map.of("id", id));
    }

    public List<Theme> findThemeRankingByReservation(
            final LocalDate startDate,
            final LocalDate endDate,
            final int rowCount
    ) {
        String sql = """
                select theme.id, theme.name, theme.description, theme.thumbnail, count(reservation.id) as reservation_count
                from theme
                inner join reservation on theme.id = reservation.theme_id and reservation.date between :start_date and :end_date
                group by theme.id
                order by reservation_count desc
                limit :row_count
                """;
        Map<String, Object> params = Map.of(
                "start_date", startDate,
                "end_date", endDate,
                "row_count", rowCount
        );
        return jdbcTemplate.query(sql, params, rowMapper());
    }

    public Theme save(final Theme theme) {
        Map<String, Object> params = Map.of(
                "name", theme.getName(),
                "description", theme.getDescription(),
                "thumbnail", theme.getThumbnail()
        );

        long themeId = jdbcInsert.executeAndReturnKey(params).longValue();
        return Theme.builder()
                .id(themeId)
                .name(theme.getName())
                .description(theme.getDescription())
                .thumbnail(theme.getThumbnail())
                .build();
    }


    protected Theme mapRowToTheme(final ResultSet resultSet, final int rowNum) throws SQLException {
        return Theme.builder()
                .id(resultSet.getLong("id"))
                .name(resultSet.getString("name"))
                .description(resultSet.getString("description"))
                .thumbnail(resultSet.getString("thumbnail"))
                .build();
    }
}
