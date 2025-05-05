package roomescape.theme.infrastructure;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.exception.resource.InCorrectResultSizeException;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeCommandRepository;
import roomescape.theme.domain.ThemeQueryRepository;

@Repository
@RequiredArgsConstructor
public class JdbcThemeRepository implements ThemeQueryRepository, ThemeCommandRepository {

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) ->
            new Theme(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("thumbnail")
            );

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long save(final Theme theme) {
        final String sql = """
                INSERT INTO themes (name, description, thumbnail) VALUES (?, ?, ?)
                """;
        final KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);

        return Optional.of(keyHolder.getKey())
                .map(Number::longValue)
                .orElseThrow(() -> new IllegalStateException("테마 추가에 실패했습니다."));
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = """
                DELETE FROM themes 
                WHERE id = ?
                """;
        jdbcTemplate.update(sql, id);
    }

    @Override
    public Boolean existsByName(final String name) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM themes
                    WHERE name = ?
                    LIMIT 1
                )
                """;

        return jdbcTemplate.queryForObject(sql, Boolean.class, name);
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        final String sql = """
                SELECT
                    id,
                    name,
                    description,
                    thumbnail
                FROM themes 
                WHERE id = ?
                """;

        try {
            return Optional.of(jdbcTemplate.queryForObject(sql, THEME_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("테마가 여러 개 존재합니다.");
        }
    }

    @Override
    public Theme getById(final Long id) {
        final String sql = """
                SELECT
                    id,
                    name,
                    description,
                    thumbnail
                FROM themes 
                WHERE id = ?
                """;

        try {
            return jdbcTemplate.queryForObject(sql, THEME_ROW_MAPPER, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException("테마가 존재하지 않습니다.");
        } catch (IncorrectResultSizeDataAccessException e) {
            throw new InCorrectResultSizeException("테마가 여러 개 존재합니다.");
        }
    }

    @Override
    public List<Theme> findAll() {
        final String sql = """
                SELECT
                    id,
                    name,
                    description,
                    thumbnail
                FROM themes 
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER);
    }

    @Override
    public List<Theme> findTopNThemesByReservationCountInDateRange(
            final LocalDate dateFrom,
            final LocalDate dateTo,
            final int limit
    ) {
        final String sql = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    t.description AS description,
                    t.thumbnail AS thumbnail,
                    COUNT(r.id) AS reservation_count
                FROM themes t
                LEFT JOIN reservations r 
                    ON t.id = r.theme_id AND r.date BETWEEN ? AND ?
                GROUP BY t.id, t.name, t.description, t.thumbnail
                ORDER BY reservation_count DESC
                LIMIT ?;
                """;

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, dateFrom, dateTo, limit);
    }
}
