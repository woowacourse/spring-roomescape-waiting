package roomescape.query;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.common.Page;
import roomescape.common.Pageable;

@Component
@RequiredArgsConstructor
public class ReservationQuery {

    private static final RowMapper<ReservationSearchResponse> searchMapper = (rs, rowNum) ->
            new ReservationSearchResponse(
                    rs.getLong("res_id"),
                    rs.getString("res_name"),
                    rs.getDate("res_date").toLocalDate(),
                    rs.getTime("res_start_at").toLocalTime(),
                    rs.getString("theme_name")
            );

    private final JdbcTemplate jdbcTemplate;

    public Page<ReservationSearchResponse> search(ReservationSearchCondition condition, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (condition.name() != null && !condition.name().isBlank()) {
            whereClause.append(" AND r.name = ?");
            params.add(condition.name());
        }

        String joinClause = """
            FROM reservation r
            JOIN theme t ON r.theme_id = t.id
            JOIN reservation_time rt ON r.time_id = rt.id
            """;

        String countSql = "SELECT COUNT(*) " + joinClause + whereClause;
        long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        StringBuilder contentSql = new StringBuilder("""
                SELECT r.id AS res_id,
                       r.name AS res_name,
                       r.date AS res_date,
                       rt.start_at AS res_start_at,
                       t.name AS theme_name
                """);
        contentSql.append(joinClause);
        contentSql.append(whereClause);

        contentSql.append(" ORDER BY r.date DESC, rt.start_at DESC");
        contentSql.append(" LIMIT ? OFFSET ?");
        params.add(pageable.size());
        params.add(pageable.offset());

        return Page.of(totalElements, pageable.size(),
                jdbcTemplate.query(contentSql.toString(), searchMapper, params.toArray()));
    }
}
