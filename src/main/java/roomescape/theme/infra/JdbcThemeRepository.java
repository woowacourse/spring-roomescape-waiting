package roomescape.theme.infra;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DuplicateException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeRepository;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> Theme.restore(
            resultSet.getLong("id"),
            resultSet.getString("name"),
            resultSet.getString("thumbnail_image_url"),
            resultSet.getString("description"),
            resultSet.getBoolean("is_active")
    );

    @Override
    public Theme save(Theme theme) {
        try {
            String sql = """
                    INSERT INTO theme(name, thumbnail_image_url, description, is_active)
                    VALUES(:name, :thumbnailImageUrl, :description, :isActive)
                    """;

            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", theme.getName())
                    .addValue("thumbnailImageUrl", theme.getThumbnailImageUrl())
                    .addValue("description", theme.getDescription())
                    .addValue("isActive", theme.isActive());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(sql, params, keyHolder, new String[]{"id"});
            long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
            return Theme.restore(
                    generatedId,
                    theme.getName(),
                    theme.getThumbnailImageUrl(),
                    theme.getDescription(),
                    theme.isActive()
            );
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e.getMessage());
        }
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = """
                SELECT id, name, description, thumbnail_image_url, is_active
                FROM theme
                WHERE id = :id
                """;
        return jdbcTemplate.query(sql, Map.of("id", id), rowMapper).stream().findFirst();
    }

    @Override
    public List<Theme> findAll(int page, int size) {
        String sql = """
                SELECT id, name, description, thumbnail_image_url, is_active FROM theme
                WHERE is_active = 1
                ORDER BY id ASC
                LIMIT :size OFFSET :offset
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("size", size)
                .addValue("offset", page * size);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public List<Theme> findByReservationCountWithLimit(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                SELECT t.id, t.name, t.description, t.thumbnail_image_url, t.is_active
                FROM theme t
                INNER JOIN reservation r ON t.id = r.theme_id
                WHERE r.date BETWEEN :startDate AND :endDate
                    AND t.is_active = 1
                    AND r.status = 'RESERVED'
                GROUP BY t.id, t.name, t.description, t.thumbnail_image_url, t.is_active
                ORDER BY COUNT(r.id) DESC
                LIMIT :limit
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("startDate", startDate)
                .addValue("endDate", endDate)
                .addValue("limit", limit);

        return jdbcTemplate.query(sql, params, rowMapper);
    }

    @Override
    public boolean existsByName(String name) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM theme
                    WHERE name = :name
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Map.of("name", name), Boolean.class));
    }

    @Override
    public void update(Theme theme) {
        try {
            String sql = """
                    UPDATE theme
                    SET name = :name,
                        thumbnail_image_url = :thumbnailImageUrl,
                        description = :description,
                        is_active = :isActive
                    WHERE id = :id
                    """;

            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("id", theme.getId())
                    .addValue("name", theme.getName())
                    .addValue("thumbnailImageUrl", theme.getThumbnailImageUrl())
                    .addValue("description", theme.getDescription())
                    .addValue("isActive", theme.isActive());

            jdbcTemplate.update(sql, params);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e.getMessage());
        }
    }
}
