package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Theme save(Theme themeWithoutId) {
        String sql = "INSERT INTO theme(name, description, thumbnail_url) VALUES (?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, new String[]{"id"});
            preparedStatement.setString(1, themeWithoutId.getName());
            preparedStatement.setString(2, themeWithoutId.getDescription());
            preparedStatement.setString(3, themeWithoutId.getThumbnailUrl());

            return preparedStatement;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return Theme.of(id, themeWithoutId);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = "SELECT * FROM theme WHERE id = (?)";

        try {
            return Optional.ofNullable(
                    jdbcTemplate.queryForObject(sql, (resultSet, rowNumber) -> {
                        String name = resultSet.getString("name");
                        String description = resultSet.getString("description");
                        String thumbnailUrl = resultSet.getString("thumbnail_url");
                        return new Theme(id, name, description, thumbnailUrl);
                    }, id));
        } catch (EmptyResultDataAccessException exception) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findAll() {
        String sql = "SELECT * FROM theme";

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {
                    Long id = resultSet.getLong("id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    String thumbnailUrl = resultSet.getString("thumbnail_url");
                    return new Theme(id, name, description, thumbnailUrl);
                }
        );
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM theme WHERE id = (?)";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Theme> findRanking(LocalDate startDate, LocalDate endDate, int limit) {
        String sql = """
                SELECT th.id AS theme_id, th.name, th.description,
                       th.thumbnail_url, COUNT(r.id) AS reservation_count
                FROM theme th
                LEFT JOIN reservation r
                    ON r.theme_id = th.id
                    AND r.date BETWEEN (?) AND (?)
                GROUP BY th.id, th.name, th.description, th.thumbnail_url
                ORDER BY reservation_count DESC, th.id ASC
                LIMIT (?)
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> {
                    Long id = resultSet.getLong("theme_id");
                    String name = resultSet.getString("name");
                    String description = resultSet.getString("description");
                    String thumbnailUrl = resultSet.getString("thumbnail_url");
                    return new Theme(id, name, description, thumbnailUrl);
                },
                startDate,
                endDate,
                limit
        );
    }

    @Override
    public boolean existsById(Long id) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1 FROM theme WHERE id = (?)
                ) AS exist
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, id));
    }
}