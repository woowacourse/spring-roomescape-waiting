package roomescape.store.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcStoreRepository implements StoreRepository {

    private final NamedParameterJdbcTemplate template;

    @Override
    public boolean existsStoreById(long storeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM store WHERE id = :storeId)";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("storeId", storeId);
        return Boolean.TRUE.equals(template.queryForObject(sql, params, Boolean.class));
    }
}
