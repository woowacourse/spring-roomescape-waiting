package roomescape.reservation.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;
import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.ReservationWaiting;
import roomescape.reservation.model.ReservationWaitingWithOrder;
import roomescape.reservation.model.Theme;

import java.util.List;

@Repository
public class CustomReservationWaitingRepositoryImpl implements CustomReservationWaitingRepository {

    private final NamedParameterJdbcTemplate template;

    public CustomReservationWaitingRepositoryImpl(final NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<ReservationWaitingWithOrder> findAllReservationWaitingWithOrdersByMemberId(final Long memberId) {
        final String sql = """
                    SELECT
                        rw.id AS reservation_waiting_id, rw.date AS reservation_waiting_date, rw.created_at AS reservation_waiting_created_at,
                        rt.id AS time_id, rt.start_at AS reservation_time,
                        th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail AS theme_thumbnail,
                        m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role,
                        COUNT(rw_sub.id) + 1 AS waiting_order
                    FROM
                        reservation_waiting rw
                    LEFT JOIN
                        reservation_waiting rw_sub
                    ON
                        rw_sub.created_at < rw.created_at
                        AND rw_sub.theme_id = rw.theme_id
                        AND rw_sub.time_id = rw.time_id
                        AND rw_sub.date = rw.date
                    INNER JOIN reservation_time AS rt ON rt.id = rw.time_id
                    INNER JOIN theme AS th ON th.id = rw.theme_id
                    INNER JOIN member AS m ON m.id = rw.member_id
                    WHERE rw.member_id = :memberId
                    GROUP BY
                        rw.id,
                        rw.date,
                        rw.created_at,
                        rw.member_id,
                        rw.theme_id,
                        rw.time_id;
                """;
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("memberId", memberId);

        return template.query(sql, param, itemRowMapperToReservationWaitingWithOrder());
    }

    private RowMapper<ReservationWaitingWithOrder> itemRowMapperToReservationWaitingWithOrder() {
        return ((rs, rowNum) -> new ReservationWaitingWithOrder(
                new ReservationWaiting(
                        rs.getLong("reservation_waiting_id"),
                        new ReservationTime(
                                rs.getLong("time_id"),
                                rs.getTime("reservation_time").toLocalTime()
                        ),
                        new Theme(
                                rs.getLong("theme_id"),
                                rs.getString("theme_name"),
                                rs.getString("theme_description"),
                                rs.getString("theme_thumbnail")
                        ),
                        new Member(
                                rs.getLong("member_id"),
                                MemberRole.valueOf(rs.getString("member_role")),
                                rs.getString("member_password"),
                                rs.getString("member_name"),
                                rs.getString("member_email")
                        ),
                        new ReservationDate(rs.getDate("reservation_waiting_date").toLocalDate()),
                        rs.getTimestamp("reservation_waiting_created_at").toLocalDateTime()
                ),
                rs.getInt("waiting_order")
        ));
    }
}
