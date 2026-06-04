package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.domain.Store;

@Repository
public class StoreJdbcRepository implements StoreRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Store> storeRowMapper = (rs, rowNum) -> new Store(
            rs.getLong("id"),
            rs.getString("name")
    );

    public StoreJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Store> findById(Long id) {
        String sql = "SELECT id, name FROM store WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, storeRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByStoreIdAndUserId(Long storeId, Long userId) {
        String sql = "SELECT exists(SELECT 1 FROM store_managers WHERE store_id = ? AND user_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, storeId, userId));
    }

    @Override
    public List<Long> findStoreIdsByUserId(Long userId) {
        String sql = "SELECT store_id FROM store_managers WHERE user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("store_id"), userId);
    }
}