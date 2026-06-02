package roomescape.dao.jdbc;

import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.dao.StoreDao;
import roomescape.domain.Store;

@Repository
public class StoreJdbcDao implements StoreDao {
    private static final RowMapper<Store> ROW_MAPPER = (rs, rowNum) ->
            new Store(rs.getLong("id"), rs.getString("name"));

    private final JdbcTemplate jdbcTemplate;

    public StoreJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Store> findById(Long id) {
        try {
            Store store = jdbcTemplate.queryForObject(
                    "SELECT id, name FROM stores WHERE id = ?",
                    ROW_MAPPER, id);
            return Optional.ofNullable(store);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
