package roomescape.dao;

import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.domain.Store;

import java.util.List;

@Repository
public class StoreDao {

    private final JdbcTemplate jdbcTemplate;

    public StoreDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Store> findAllStores() {
        String sql = "SELECT id, name FROM store";
        return jdbcTemplate.query(sql, new DataClassRowMapper<>(Store.class));
    }

    public Store findById(Long id) {
        String sql = "SELECT id, name FROM store WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new DataClassRowMapper<>(Store.class), id);
    }
}
