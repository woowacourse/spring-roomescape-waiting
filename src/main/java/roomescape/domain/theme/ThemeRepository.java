package roomescape.domain.theme;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public ThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Theme> rowMapper = (resultSet, rowNum) -> Theme.of(
        resultSet.getLong("id"),
        resultSet.getString("name"),
        resultSet.getString("description"),
        resultSet.getString("image_url")
    );

    public Optional<Theme> findById(Long id) {
        String query = "select * from theme where id = ?";
        return jdbcTemplate.query(query, rowMapper, id)
                .stream()
                .findFirst();
    }

    public List<Theme> findAll() {
        String query = "select * from theme";
        return jdbcTemplate.query(query, rowMapper);
    }
}
