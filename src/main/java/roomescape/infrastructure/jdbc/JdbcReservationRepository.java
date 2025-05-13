package roomescape.infrastructure.jdbc;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.dto.ReservationSearchFilter;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER =
            (rs, rowNum) -> {
                Long id = rs.getLong("reservation_id");
                Long memberId = rs.getLong("member_id");
                String memberName = rs.getString("member_name");
                String email = rs.getString("member_email");
                String password = rs.getString("member_password");
                LocalDate date = rs.getObject("reservation_date", LocalDate.class);
                Long timeId = rs.getLong("time_id");
                LocalTime time = rs.getObject("time_value", LocalTime.class);
                Long themeId = rs.getLong("theme_id");
                String themeName = rs.getString("theme_name");
                String themeDescription = rs.getString("theme_description");
                String themeThumbnail = rs.getString("theme_thumbnail");
                return Reservation.of(
                        id,
                        Member.of(memberId, memberName, email, password),
                        Theme.of(themeId, themeName, themeDescription, themeThumbnail),
                        date,
                        ReservationTime.of(timeId, time)
                );
            };
    private final String findAllSql = """
            SELECT
                r.id as reservation_id,
                m.id as member_id,
                m.name as member_name,
                m.email as member_email,
                m.password as member_password,
                r.date as reservation_date,
                t.id as time_id,
                t.start_at as time_value,
                th.id as theme_id,
                th.name as theme_name,
                th.description as theme_description,
                th.thumbnail as theme_thumbnail
            FROM reservation as r
            join reservation_time as t
                on r.time_id = t.id
            join theme as th 
                on r.theme_id = th.id
            join member as m
                on r.member_id = m.id
            """;
    private final JdbcTemplate jdbcTemplate;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(findAllSql, RESERVATION_ROW_MAPPER);
    }

    @Override
    public Long save(Reservation reservation) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = Map.of(
                "member_id", reservation.getMember().getId(),
                "date", Date.valueOf(reservation.getReservationDate()),
                "time_id", reservation.getReservationTime().getId(),
                "theme_id", reservation.getTheme().getId()
        );

        Number key = simpleJdbcInsert.executeAndReturnKey(parameters);
        return key.longValue();
    }

    @Override
    public boolean deleteById(Long id) {
        String deleteSql = "DELETE FROM reservation WHERE id=?";
        return jdbcTemplate.update(deleteSql, id) > 0;
    }

    @Override
    public List<Reservation> searchWith(ReservationSearchFilter reservationSearchFilter) {
        if (reservationSearchFilter.isEmpty()) {
            return jdbcTemplate.query(findAllSql, RESERVATION_ROW_MAPPER);
        }

        String sql = findAllSql + " WHERE ";
        List<String> whereStatements = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (reservationSearchFilter.themeId() != null) {
            whereStatements.add("theme_id = ?");
            params.add(reservationSearchFilter.themeId());
        }

        if (reservationSearchFilter.memberId() != null) {
            whereStatements.add("member_id = ?");
            params.add(reservationSearchFilter.memberId());
        }

        if (reservationSearchFilter.dateFrom() != null) {
            whereStatements.add("date >= ?");
            params.add(reservationSearchFilter.dateFrom());
        }

        if (reservationSearchFilter.dateTo() != null) {
            whereStatements.add("date <= ?");
            params.add(reservationSearchFilter.dateTo());
        }

        return jdbcTemplate.query(
                sql + String.join(" AND ", whereStatements), RESERVATION_ROW_MAPPER, params.toArray()
        );
    }
}
