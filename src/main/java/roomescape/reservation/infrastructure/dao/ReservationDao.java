package roomescape.reservation.infrastructure.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.application.dto.CreateReservationRequest;
import roomescape.reservation.application.repository.ReservationRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@Repository
public class ReservationDao implements ReservationRepository {
    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> {
        return new Reservation(
                resultSet.getLong("reservation_id"),
                new Member(
                        resultSet.getLong("member_id"),
                        resultSet.getString("member_email"),
                        resultSet.getString("member_password"),
                        resultSet.getString("member_name"),
                        Role.valueOf(resultSet.getString("member_role"))
                ),
                new Theme(
                        resultSet.getLong("theme_id"),
                        resultSet.getString("theme_name"),
                        resultSet.getString("theme_description"),
                        resultSet.getString("theme_thumbnail")
                ),
                new ReservationDate(
                        resultSet.getDate("reservation_date").toLocalDate()
                ),
                new ReservationTime(
                        resultSet.getLong("time_id"),
                        resultSet.getTime("time_value").toLocalTime()
                ));
    };

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation insert(final CreateReservationRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("member_id", request.member().getId());
        params.put("theme_id", request.theme().getId());
        params.put("date", request.date().getReservationDate().toString());
        params.put("time_id", request.time().getId());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(id, request.member(), request.theme(), request.date(), request.time());
    }

    @Override
    public List<Reservation> findAllReservations() {
        String sql = """
                    SELECT
                    r.id as reservation_id,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description as theme_description,
                    t.thumbnail as theme_thumbnail,
                    r.date as reservation_date,
                    rt.id as time_id,
                    rt.start_at as time_value
                FROM reservation as r
                inner join reservation_time as rt
                on r.time_id = rt.id
                inner join theme as t
                on r.theme_id = t.id
                inner join member as m
                on r.member_id = m.id
                """;

        return jdbcTemplate.query(
                sql, RESERVATION_ROW_MAPPER);
    }

    @Override
    public List<Reservation> findReservationsBy(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                    r.id as reservation_id,
                    m.id as member_id,
                    m.name as member_name,
                    m.email as member_email,
                    m.password as member_password,
                    m.role as member_role,
                    t.id as theme_id,
                    t.name as theme_name,
                    t.description as theme_description,
                    t.thumbnail as theme_thumbnail,
                    r.date as reservation_date,
                    rt.id as time_id,
                    rt.start_at as time_value
                FROM reservation as r
                inner join reservation_time as rt
                on r.time_id = rt.id
                inner join theme as t
                on r.theme_id = t.id
                inner join member as m
                on r.member_id = m.id
                """);

        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (memberId != null) {
            conditions.add("r.member_id = ?");
            params.add(memberId);
        }
        if (themeId != null) {
            conditions.add("r.theme_id = ?");
            params.add(themeId);
        }
        if (dateFrom != null) {
            conditions.add("r.date >= ?");
            params.add(dateFrom);
        }
        if (dateTo != null) {
            conditions.add("r.date <= ?");
            params.add(dateTo);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        return jdbcTemplate.query(sql.toString(), RESERVATION_ROW_MAPPER, params.toArray());
    }

    @Override
    public int delete(final Long id) {
        String sql = "delete from reservation where id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE time_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation
                    WHERE theme_id = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, themeId));
    }

    @Override
    public boolean existsByDateTime(LocalDateTime reservationDateTime) {
        LocalDate date = reservationDateTime.toLocalDate();
        LocalTime time = reservationDateTime.toLocalTime();
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM reservation r
                    INNER JOIN reservation_time rt
                    ON r.time_id = rt.id
                    WHERE r.date = ?
                    AND rt.start_at = ?
                )
                """;

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, date.toString(), time.toString()));
    }

    @Override
    public List<Long> findBookedTimeIds(LocalDate date, Long themeId) {
        String sql = """
                    SELECT r.time_id
                    FROM reservation r
                    WHERE r.date = ?
                    AND r.theme_id = ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("time_id"), date, themeId);
    }
}
