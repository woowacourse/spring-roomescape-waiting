package roomescape.theme.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.projection.PopularThemeResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;
    private final RowMapper<Theme> themeRowMapper = (resultSet, rowMapper) ->
            Theme.load(
                    resultSet.getLong("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("thumbnail_url"),
                    resultSet.getBoolean("is_active"),
                    resultSet.getLong("amount")
            );

    public JdbcThemeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";
        return jdbcTemplate.query(sql, themeRowMapper);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, params, themeRowMapper));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findByIsActive(boolean isActive) {
        String sql = "SELECT * FROM theme WHERE is_active = :status ORDER BY name ASC";
        MapSqlParameterSource params = new MapSqlParameterSource("status", isActive);
        return jdbcTemplate.query(sql, params, themeRowMapper);
    }

    @Override
    public Theme save(Theme theme) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail_url", theme.getThumbnailUrl())
                .addValue("is_active", theme.isActive())
                .addValue("amount", theme.getAmount());
        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Theme.load(id, theme.getName(), theme.getDescription(), theme.getThumbnailUrl(), theme.isActive(), theme.getAmount());
    }

    @Override
    public boolean updateStatus(Theme theme) {
        String sql = """
                UPDATE theme
                SET name = :name, description = :description,
                    thumbnail_url = :thumbnail_url, is_active = :is_active,
                    amount = :amount
                WHERE id = :id
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", theme.getId())
                .addValue("name", theme.getName())
                .addValue("description", theme.getDescription())
                .addValue("thumbnail_url", theme.getThumbnailUrl())
                .addValue("is_active", theme.isActive())
                .addValue("amount", theme.getAmount());

        int updateCount = jdbcTemplate.update(sql, params);
        return updateCount > 0;
    }

    @Override
    public List<PopularThemeResult> findPopularThemes(
            LocalDate startDate,
            LocalDate endDate,
            int limit
    ) {
        String sql = """
                SELECT
                    t.id,
                    t.name,
                    t.description,
                    t.thumbnail_url,
                    t.is_active,
                    t.amount,
                    COUNT(r.id) AS reservation_count
                FROM reservation r
                JOIN reservation_slot rs ON r.slot_id  = rs.id
                JOIN theme             t  ON rs.theme_id = t.id
                JOIN reservation_date  d  ON rs.date_id  = d.id
                WHERE t.is_active = true
                  AND r.status    = 'RESERVED'
                  AND d.date >= :startDate
                  AND d.date <= :endDate
                GROUP BY t.id, t.name, t.description, t.thumbnail_url, t.is_active, t.amount
                ORDER BY reservation_count DESC
                LIMIT :limit
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, (rs, rowNum) -> new PopularThemeResult(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("thumbnail_url"),
                rs.getBoolean("is_active"),
                rs.getLong("amount"),
                rs.getLong("reservation_count")
        ));
    }

    @Override
    public List<Theme> findSlotOfThemes() {
        String sql = """
                SELECT DISTINCT t.*
                FROM theme t
                JOIN reservation_slot s ON t.id = s.theme_id
                WHERE t.is_active = TRUE;
                """;
        MapSqlParameterSource params = new MapSqlParameterSource();
        return jdbcTemplate.query(sql, params, themeRowMapper);
    }

}
