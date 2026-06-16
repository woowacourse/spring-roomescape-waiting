package roomescape.query;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.service.command.ReservationSearchCommand;
import roomescape.service.result.ReservationEntryResult;
import roomescape.service.result.ReservationResult;
import roomescape.service.result.ReservationSearchResult;
import roomescape.service.result.ReservationTimeResult;
import roomescape.service.result.ThemeRegisterResult;

@Repository
@RequiredArgsConstructor
public class ReservationQueryRepository {

    private static final RowMapper<ReservationResult> RESERVATION_ROW_MAPPER = (rs, rowNum) ->
            new ReservationResult(
                    rs.getLong("reservation_id"),
                    rs.getDate("reservation_date").toLocalDate(),
                    new ThemeRegisterResult(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("theme_description"),
                            rs.getString("theme_thumbnail_image_url"),
                            rs.getLong("theme_price"),
                            rs.getBoolean("theme_is_active")
                    ),
                    new ReservationTimeResult(
                            rs.getLong("time_id"),
                            rs.getTime("time_start_at").toLocalTime(),
                            rs.getString("time_status")
                    ),
                    new ReservationEntryResult(
                            rs.getLong("entry_id"),
                            rs.getString("entry_name"),
                            rs.getString("entry_status"),
                            rs.getTimestamp("entry_created_at").toLocalDateTime()
                    )
            );

    private static final RowMapper<ReservationSearchResult> SEARCH_ROW_MAPPER = (rs, rowNum) ->
            new ReservationSearchResult(
                    rs.getLong("res_id"),
                    rs.getString("res_name"),
                    rs.getDate("res_date").toLocalDate(),
                    rs.getTime("res_start_at").toLocalTime(),
                    rs.getString("theme_name"),
                    rs.getString("res_status"),
                    rs.getObject("waiting_rank", Integer.class)
            );

    private final JdbcTemplate jdbcTemplate;

    public List<ReservationResult> getAllReservations() {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.date AS reservation_date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_image_url AS theme_thumbnail_image_url,
                    t.price AS theme_price,
                    t.is_active AS theme_is_active,
                    rt.id AS time_id,
                    rt.start_at AS time_start_at,
                    rt.status AS time_status,
                    re.id AS entry_id,
                    re.name AS entry_name,
                    re.status AS entry_status,
                    re.created_at AS entry_created_at
                FROM reservation r
                JOIN theme t ON r.theme_id = t.id
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN reservation_entry re ON re.reservation_id = r.id
                WHERE re.status = 'RESERVED'
                ORDER BY r.date DESC, rt.start_at DESC, re.id DESC
                """;
        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER);
    }

    public Page<ReservationSearchResult> search(ReservationSearchCommand command, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (command.name() != null && !command.name().isBlank()) {
            whereClause.append(" AND re.name = ?");
            params.add(command.name());
        }

        String joinClause = """
                FROM reservation r
                JOIN reservation_entry re ON re.reservation_id = r.id AND re.status != 'DELETED'
                JOIN theme t ON r.theme_id = t.id
                JOIN reservation_time rt ON r.time_id = rt.id
                """;

        String countSql = "SELECT COUNT(*) " + joinClause + whereClause;
        long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        String contentSql = """
                   SELECT re.id AS res_id,
                          re.name AS res_name,
                          r.date AS res_date,
                          rt.start_at AS res_start_at,
                          t.name AS theme_name,
                          re.status AS res_status,
                          CASE WHEN re.status = 'WAITING'
                               THEN (SELECT COUNT(*) + 1
                                     FROM reservation_entry re2
                                     WHERE re2.reservation_id = re.reservation_id
                                       AND re2.status = 'WAITING'
                                       AND (re2.created_at < re.created_at
                                           OR (re2.created_at = re.created_at AND re2.id < re.id)))
                               ELSE NULL
                          END AS waiting_rank
                """ + joinClause
                + whereClause
                + " ORDER BY r.date DESC, rt.start_at DESC"
                + " LIMIT ? OFFSET ?";
        params.add(pageable.size());
        params.add(pageable.offset());

        return Page.of(totalElements, pageable.size(),
                jdbcTemplate.query(contentSql, SEARCH_ROW_MAPPER, params.toArray()));
    }
}
