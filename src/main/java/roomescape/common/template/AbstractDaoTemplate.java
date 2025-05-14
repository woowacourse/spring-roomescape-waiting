package roomescape.common.template;

import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public abstract class AbstractDaoTemplate<T> {

    private final String tableName;
    protected final NamedParameterJdbcTemplate jdbcTemplate;
    protected final SimpleJdbcInsert jdbcInsert;

    protected AbstractDaoTemplate(
            final NamedParameterJdbcTemplate jdbcTemplate,
            final String tableName,
            final DataSource dataSource
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName(tableName)
                .usingGeneratedKeyColumns("id");
    }

    protected abstract RowMapper<T> rowMapper();

    protected Optional<T> executeQueryForObject(final String sql, final Map<String, Object> params) {
        try {
            T entity = jdbcTemplate.queryForObject(sql, params, rowMapper());
            return Optional.ofNullable(entity);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public int deleteById(final Long id) {
        String sql = String.format("delete from %s where id = :id", tableName);
        return jdbcTemplate.update(sql, Map.of("id", id));
    }

    protected boolean existsBy(final String column, final Object value) {
        String sql = String.format("select count(*) from %s where %s = :value", tableName, column);
        int count = jdbcTemplate.queryForObject(sql, Map.of("value", value), Integer.class);
        return count > 0;
    }
}
