package roomescape.reservation.repository;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;
import roomescape.reservation.dto.SearchReservationsParams;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationStatus;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;
import roomescape.reservation.repository.param.QueryGenerator;

import java.util.List;

@Repository
public class CustomReservationRepositoryImpl implements CustomReservationRepository {

    private final NamedParameterJdbcTemplate template;

    public CustomReservationRepositoryImpl(final NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<Reservation> searchReservations(final SearchReservationsParams searchReservationsParams) {
        final String sql = QueryGenerator.generateQueryWithSearchReservationsParams(
                searchReservationsParams,
                """
                    SELECT
                        r.id AS reservation_id, r.date AS reservation_date, r.status AS reservation_status,
                        rt.id AS time_id, rt.start_at AS reservation_time,
                        th.id AS theme_id, th.name AS theme_name, th.description AS theme_description, th.thumbnail AS theme_thumbnail,
                        m.id AS member_id, m.name AS member_name, m.email AS member_email, m.password AS member_password, m.role AS member_role
                    FROM reservation AS r
                    INNER JOIN reservation_time AS rt on r.time_id = rt.id
                    INNER JOIN theme AS th ON r.theme_id = th.id
                    INNER JOIN member AS m ON r.member_id = m.id
                """
        );

        return template.query(sql, itemRowMapper());
    }

    private RowMapper<Reservation> itemRowMapper() {
        return ((rs, rowNum) -> Reservation.of(
                rs.getLong("reservation_id"),
                ReservationStatus.valueOf(rs.getString("reservation_status")),
                rs.getDate("reservation_date").toLocalDate(),
                new ReservationTime(rs.getLong("time_id"), rs.getTime("reservation_time").toLocalTime()),
                Theme.of(
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("theme_description"),
                        rs.getString("theme_thumbnail")),
                Member.createMemberWithId(
                        rs.getLong("member_id"),
                        MemberRole.valueOf(rs.getString("member_role")),
                        rs.getString("member_password"),
                        rs.getString("member_name"),
                        rs.getString("member_email")
                )
        ));
    }
}
