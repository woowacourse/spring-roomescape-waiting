package roomescape.manager.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.manager.Manager;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcManagerRepository implements ManagerRepository {

    private final NamedParameterJdbcTemplate template;

    @Override
    public Optional<Manager> findByMemberId(long memberId) {
        String sql = "SELECT id, member_id, store_id FROM manager WHERE member_id = :memberId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);

        return template.query(sql, params, (rs, rowNum) -> new Manager(
                rs.getLong("id"),
                rs.getLong("member_id"),
                rs.getLong("store_id")
        )).stream().findFirst();
    }
}
