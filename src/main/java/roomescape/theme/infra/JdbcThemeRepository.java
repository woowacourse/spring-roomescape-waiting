package roomescape.theme.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> Theme.builder()
            .id(resultSet.getLong("id"))
            .name(resultSet.getString("name"))
            .description(resultSet.getString("description"))
            .thumbnailImageUrl(resultSet.getString("thumbnail_image_url"))
            .durationTime(resultSet.getTime("duration_time").toLocalTime())
            .build();

    @Override
    public Theme save(final Theme theme) {
        String sql = "INSERT INTO theme(name, thumbnail_image_url, description, duration_time) "
                + "VALUES(:name, :thumbnailImageUrl, :description, :durationTime)";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", theme.getName())
                .addValue("thumbnailImageUrl", theme.getThumbnailImageUrl())
                .addValue("description", theme.getDescription())
                .addValue("durationTime", theme.getDurationTime());

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(sql, params, keyHolder);
        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        return theme.withId(generatedId);
    }

    @Override
    public int delete(final Theme theme) {
        String sql = "UPDATE theme SET deleted_at=:date WHERE id = :id";
        return jdbcTemplate.update(sql, Map.of("id", theme.getId(), "date", theme.getDeletedAt()));
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        String sql = "SELECT id, name, description, thumbnail_image_url, duration_time FROM theme WHERE id = :id AND deleted_at IS NULL";
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper).stream().findFirst();
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT id, name, description, thumbnail_image_url, duration_time FROM theme WHERE deleted_at IS NULL";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public List<Theme> findByReservationCountWithLimit(final LocalDate startDate, final LocalDate endDate, final int limit) {
        String sql = "SELECT t.id, t.name, t.description, t.thumbnail_image_url, t.duration_time "
                + "FROM theme t "
                + "INNER JOIN reservation r ON t.id = r.theme_id "
                + "WHERE r.date BETWEEN :startDate AND :endDate AND t.deleted_at IS NULL AND r.is_deleted = 0 "
                + "GROUP BY t.id, t.name, t.description, t.thumbnail_image_url, t.duration_time "
                + "ORDER BY COUNT(r.id) DESC "
                + "LIMIT :limit";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public boolean existsByName(final String name) {
        String sql = "SELECT EXISTS (SELECT 1 FROM theme WHERE name=:name AND deleted_at IS NULL)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("name", name), Boolean.class));
    }
}
