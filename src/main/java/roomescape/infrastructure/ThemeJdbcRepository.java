package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;
import roomescape.domain.repository.ThemeRepository;

@Repository
public class ThemeJdbcRepository implements ThemeRepository {

    private final JdbcTemplate jdbcTemplate;

    public ThemeJdbcRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Theme> findById(final long id) {
        var sql = """
            select T.id, T.name, T.description, T.thumbnail from THEME T
            where T.id = ?
            """;

        var themeList = jdbcTemplate.query(sql, RowMappers.THEME, id);
        return themeList.stream().findAny();
    }

    @Override
    public long save(final Theme theme) {
        var insert = new SimpleJdbcInsert(jdbcTemplate);
        var generatedId = insert.withTableName("THEME")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(Map.of(
                "name", theme.name(),
                "description", theme.description(),
                "thumbnail", theme.thumbnail()
            ));
        return generatedId.longValue();
    }

    @Override
    public boolean removeById(final long id) {
        var sql = "delete from THEME where id = ?";

        var removedRowsCount = jdbcTemplate.update(sql, id);
        return removedRowsCount > 0;
    }

    @Override
    public List<Theme> findAll() {
        var sql = """
            select T.id, T.name, T.description, T.thumbnail from THEME T
            """;

        return jdbcTemplate.query(sql, RowMappers.THEME);
    }

    @Override
    public List<Theme> findRankingByPeriod(final LocalDate startDate, final LocalDate endDate, final int limit) {
        var sql = """
            select T.id, T.name, T.description, T.thumbnail from THEME T
            join RESERVATION R on T.id = R.theme_id
            where R.date >= ? and R.date <= ?
            group by T.id
            order by count(R.id) desc
            limit ?
            """;
        return jdbcTemplate.query(sql, RowMappers.THEME, startDate, endDate, limit);
    }
}
