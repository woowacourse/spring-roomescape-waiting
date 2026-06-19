package roomescape.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.common.Page;
import roomescape.common.Pageable;
import roomescape.controller.client.api.dto.condition.ReservationSearchCondition;
import roomescape.controller.client.api.dto.response.ReservationSearchResponse;
import roomescape.controller.client.api.dto.response.ReservationDetailResponse;
import roomescape.controller.client.api.query.ReservationQuery;
import roomescape.exception.EntityNotFoundException;
import roomescape.persistence.jdbc.mapper.ReservationQueryRowMapper;

@Component
@RequiredArgsConstructor
public class JdbcReservationQuery implements ReservationQuery {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ReservationDetailResponse findByReservationId(long reservationId) {
        String sql = """
                SELECT r.id AS slot_id,
                       r.date AS slot_date,
                       t.id AS theme_id,
                       t.name AS theme_name,
                       t.description AS theme_description,
                       t.thumbnail_image_url AS theme_thumbnail_image_url,
                       t.price AS theme_price,
                       rt.id AS time_id,
                       rt.start_at AS time_start_at,
                       re.id AS reservation_id,
                       re.name AS reservation_name,
                       re.status AS reservation_status,
                       re.created_at AS reservation_created_at
                FROM reservation re
                JOIN reservation_slot r ON re.slot_id = r.id
                JOIN theme t ON r.theme_id = t.id
                JOIN reservation_time rt ON r.time_id = rt.id
                WHERE re.id = ?
                  AND re.status = 'RESERVED'
                  AND re.active_status = 'ACTIVE'
                """;

        return jdbcTemplate.query(sql, ReservationQueryRowMapper.RESERVATION_DETAIL_ROW_MAPPER, reservationId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("예약 정보를 찾을 수 없습니다."));
    }

    @Override
    public Page<ReservationSearchResponse> search(ReservationSearchCondition condition, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (condition.name() != null && !condition.name().isBlank()) {
            whereClause.append(" AND re.name = ?");
            params.add(condition.name());
        }

        String joinClause = """
            FROM reservation_slot r
            JOIN reservation re ON re.slot_id = r.id AND re.active_status = 'ACTIVE'
            JOIN theme t ON r.theme_id = t.id
            JOIN reservation_time rt ON r.time_id = rt.id
            """;

        String countSql = "SELECT COUNT(*) " + joinClause + whereClause;
        long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());

        StringBuilder contentSql = new StringBuilder("""
                SELECT re.id AS res_id,
                       re.name AS res_name,
                       r.date AS res_date,
                       rt.start_at AS res_start_at,
                       t.name AS theme_name,
                       t.price AS theme_price,
                       re.status AS res_status,
                       CASE WHEN re.status = 'WAITING'
                            THEN (SELECT COUNT(*) + 1
                                  FROM reservation re2
                                  WHERE re2.slot_id = re.slot_id
                                    AND re2.status = 'WAITING'
                                    AND re2.active_status = 'ACTIVE'
                                    AND re2.created_at < re.created_at)
                            ELSE NULL
                       END AS waiting_rank
                """);
        contentSql.append(joinClause);
        contentSql.append(whereClause);

        contentSql.append(" ORDER BY r.date DESC, rt.start_at DESC");
        contentSql.append(" LIMIT ? OFFSET ?");
        params.add(pageable.size());
        params.add(pageable.offset());

        return Page.of(totalElements, pageable.size(), jdbcTemplate.query(
                contentSql.toString(),
                ReservationQueryRowMapper.RESERVATION_SEARCH_ROW_MAPPER,
                params.toArray()
        ));
    }
}
