package roomescape.theme.repository.jdbc;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.exception.BadRequestException;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

@Repository
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jdbc")
public class JdbcThemeRepository implements ThemeRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    @Autowired
    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("theme")
            .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        Map<String, Object> parameters = Map.ofEntries(
            Map.entry("name", theme.getName()),
            Map.entry("description", theme.getDescription()),
            Map.entry("thumbnail", theme.getThumbnail())
        );

        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        return Theme.generateWithPrimaryKey(theme, id);
    }

    @Override
    public List<Theme> findAll() {
        final String query = "SELECT id, name, description, thumbnail FROM theme";
        return jdbcTemplate.query(query, (resultSet, rowNum) ->
            new Theme(resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail"))
        );
    }

    @Override
    public List<Theme> findTopByReservationCountDesc(Long listNum) {
        final String query = """
            SELECT t.id, t.name, t.description, t.thumbnail, rc.reservation_count
            FROM (
                SELECT r.theme_id, COUNT(*) AS reservation_count
                FROM reservation r
                WHERE r.date BETWEEN DATEADD('DAY', -7, CURRENT_DATE) AND DATEADD('DAY', -1, CURRENT_DATE)
                GROUP BY r.theme_id
                ORDER BY reservation_count DESC
                LIMIT ?
            ) rc
            INNER JOIN theme t ON rc.theme_id = t.id
            """;

        List<Theme> listedTheme = jdbcTemplate.query(
            query,
            (resultSet, rowNum) -> new Theme(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail")
            ),
            listNum
        );

        return listedTheme;
    }

    @Override
    public Optional<Theme> findById(Long id) {
        final String query = "SELECT id, name, description, thumbnail FROM theme WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(query, (resultSet, rowNum) ->
                    new Theme(resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("thumbnail"))
                , id));
        } catch (DataAccessException exception) {
            return Optional.empty();
        }
    }

    public void deleteById(Long id) {
        final String query = "DELETE FROM theme WHERE id = ?";
        int affectedRows = jdbcTemplate.update(query, id);
        if (affectedRows == 0) {
            throw new BadRequestException("테마를 삭제할 수 없습니다. 존재하지 않는 ID입니다.");
        }
    }
}
