package roomescape.theme.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.entity.ThemeEntity;

@Repository
public class JdbcThemeRepository implements ThemeRepository {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (resultSet, rowNum) -> {
        ThemeEntity entity = new ThemeEntity(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_url")
        );
        return ThemeMapper.toDomain(entity);
    };

    private final JdbcTemplate jdbcTemplate;

    public JdbcThemeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Theme save(Theme theme) {
        ThemeEntity entity = ThemeMapper.toEntity(theme);

        String sql = """
               INSERT INTO theme (name, description, thumbnail_url)
               VALUES (?, ?, ?)
               """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, entity.getName());
            ps.setString(2, entity.getDescription());
            ps.setString(3, entity.getThumbnailUrl());
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();

        ThemeEntity savedEntity = new ThemeEntity(
                id,
                entity.getName(),
                entity.getDescription(),
                entity.getThumbnailUrl()
        );

        return ThemeMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Theme> findById(Long id) {
        String sql = """
               SELECT *
               FROM theme
               WHERE id = ?
               """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public boolean existsByName(String name) {
        String sql = """
               SELECT EXISTS(
                   SELECT 1
                   FROM theme
                   WHERE name = ?   
               )
               """;

        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, name);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public List<Theme> findAll() {
        String sql = """
                SELECT id, name, description, thumbnail_url
                FROM theme
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    @Override
    public int deleteById(Long id) {
        String sql = """
               DELETE FROM theme
               WHERE id = ?
               """;

        return jdbcTemplate.update(sql, id);
    }
}
