package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public class JdbcThemeDao implements ThemeRepository {

    private static final RowMapper<Theme> rowMapper = ((rs, rowNum) -> {
        String name = rs.getString("name");
        String description = rs.getString("description");
        String thumbnail = rs.getString("thumbnail");
        return new Theme(rs.getLong("id"), name, description, thumbnail);
    });

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public JdbcThemeDao(JdbcTemplate jdbcTemplate, DataSource source) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(source)
                .withTableName("theme")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Theme save(Theme theme) {
        try{
            String name = theme.name();
            String description = theme.description();
            String thumbnail = theme.thumbnail();
            SqlParameterSource params = new MapSqlParameterSource()
                    .addValue("name", name)
                    .addValue("description", description)
                    .addValue("thumbnail", thumbnail);
            long id = jdbcInsert.executeAndReturnKey(params).longValue();
            return new Theme(id,name,description,thumbnail);
        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("[ERROR] 이미 동일한 테마 이름이 존재하여 생성에 실패햐였습니다." + theme.name());
        }
    }

    @Override
    public List<Theme> findAll() {
        String sql = "select * from theme";

        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Theme> findById(long id) {
        String sql = "select * from theme where id = ?";
        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Theme> findPopular(LocalDate start, LocalDate end) {
        String sql = """
                SELECT
                th.id,
                th.name,
                th.description,
                th.thumbnail
                FROM theme as th
                INNER JOIN reservation as r on r.theme_id = th.id
                WHERE r.date >= ? and r.date < ?
                GROUP BY th.id
                ORDER BY COUNT(th.id) DESC
                LIMIT 10
                """;
        return jdbcTemplate.query(sql, rowMapper, start, end);
    }

    @Override
    public int deleteById(long id) {
        String sql = "delete from theme where id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
