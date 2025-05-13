package roomescape.theme.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DataExistException;
import roomescape.common.exception.SaveException;
import roomescape.theme.domain.Theme;

@RequiredArgsConstructor
@Repository
public class JdbcThemeRepository implements ThemeRepository {

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

        final int rowAffected = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, theme.getName());
            ps.setString(2, theme.getDescription());
            ps.setString(3, theme.getThumbnail());
            return ps;
        }, keyHolder);

        if (rowAffected != 1) {
            throw new SaveException("테마 저장에 실패했습니다.");
        }

        final Number key = keyHolder.getKey();

        return key.longValue();
    }

    @Override
    public void deleteById(final Long id) {
        final String sql = """
                DELETE FROM themes 
                WHERE id = ?
                """;
        try {
            jdbcTemplate.update(sql, id);
        } catch (final DataIntegrityViolationException e) {
            throw new DataExistException("데이터 무결성 제약으로 인해 삭제할 수 없습니다." + e);
        }
    }

    @Override
    public boolean existsByName(final String name) {
        final String sql = """
                SELECT EXISTS(
                    SELECT 1
                    FROM themes
                    WHERE name = ?
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
        final List<Theme> themes = jdbcTemplate.query(sql, THEME_ROW_MAPPER, id);

        if (!themes.isEmpty()) {
            return Optional.of(themes.getFirst());
        }
        return Optional.empty();
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
    public List<Theme> findTop10ThemesByReservationCountWithin7Days(final int days, final int limit) {
        final String sql = """
                SELECT
                    t.id AS id,
                    t.name AS name,
                    t.description AS description,
                    t.thumbnail AS thumbnail,
                    COUNT(r.id) AS reservation_count
                FROM themes t
                LEFT JOIN reservations r 
                    ON t.id = r.theme_id AND r.date >= ?
                GROUP BY t.id, t.name, t.description, t.thumbnail
                ORDER BY reservation_count DESC
                LIMIT ?
                """;

        final LocalDate fromDate = LocalDate.now().minusDays(days);

        return jdbcTemplate.query(sql, THEME_ROW_MAPPER, fromDate, limit);
    }
}
