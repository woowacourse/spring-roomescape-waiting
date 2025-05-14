package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.entity.MemberEntity;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.entity.ReservationEntity;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;
import roomescape.reservationtime.entity.ReservationTimeEntity;
import roomescape.theme.entity.ThemeEntity;

@Repository
public class JDBCReservationRepository implements ReservationRepository {

    private static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (resultSet, rowNum) -> {
        ReservationTimeEntity timeEntity = new ReservationTimeEntity(
                resultSet.getLong("time_id"),
                resultSet.getString("time_value"));

        ThemeEntity themeEntity = new ThemeEntity(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail"));

        MemberEntity memberEntity = new MemberEntity(
                resultSet.getLong("member_id"),
                resultSet.getString("member_name"),
                resultSet.getString("member_email"),
                null,
                resultSet.getString("member_role")
        );

        ReservationEntity entity = new ReservationEntity(
                resultSet.getLong("reservation_id"),
                resultSet.getString("date"),
                memberEntity,
                timeEntity,
                themeEntity
        );
        return entity.toReservation();
    };
    private static final String RESERVATION_TIME_ALREADY_BOOKED = "SELECT rt.id, rt.start_at, "
            + "CASE WHEN r.id IS NOT NULL THEN true ELSE false END AS already_booked "
            + "FROM reservation_time AS rt "
            + "LEFT JOIN reservation r ON rt.id = r.time_id AND r.date = ? AND r.theme_id = ? "
            + "ORDER BY rt.start_at";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public JDBCReservationRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findFilteredReservations(final Long themeId, final Long memberId,
                                                      final LocalDate startDate,
                                                      final LocalDate endDate) {
        StringBuilder sql = new StringBuilder("select ")
                .append("r.id as reservation_id, ")
                .append("r.date, ")
                .append("t.id as time_id, ")
                .append("t.start_at as time_value, ")
                .append("th.id as theme_id, ")
                .append("th.name as theme_name, ")
                .append("th.description, ")
                .append("th.thumbnail, ")
                .append("m.id as member_id, ")
                .append("m.name as member_name, ")
                .append("m.email as member_email, ")
                .append("m.role as member_role ")
                .append("FROM reservation as r ")
                .append("inner join reservation_time as t on r.time_id = t.id ")
                .append("inner join theme as th on r.theme_id = th.id ")
                .append("inner join member as m ")
                .append("on r.member_id = m.id ")
                .append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (themeId != null) {
            sql.append("AND r.theme_id = ? ");
            params.add(themeId);
        }

        if (memberId != null) {
            sql.append("AND r.member_id = ? ");
            params.add(memberId);
        }

        if (startDate != null) {
            sql.append("AND r.date >= ? ");
            params.add(startDate);
        }

        if (endDate != null) {
            sql.append("AND r.date <= ? ");
            params.add(endDate);
        }

        return jdbcTemplate.query(
                sql.toString(),
                RESERVATION_ROW_MAPPER,
                params.toArray()
        );
    }

    @Override
    public Reservation save(final Reservation reservation) {
        Long generatedId = simpleJdbcInsert.executeAndReturnKey(
                Map.of("date", reservation.getDate(), "time_id", reservation.getTime().getId(),
                        "theme_id", reservation.getTheme().getId(), "member_id", reservation.getMember().getId())
        ).longValue();

        return Reservation.of(generatedId, reservation.getDate(), reservation.getMember(), reservation.getTime(),
                reservation.getTheme());
    }

    @Override
    public boolean deleteById(final Long id) {
        return jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id) != 0;
    }

    @Override
    public boolean existsByTimeId(final Long id) {
        try {
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM reservation WHERE time_id = ?)",
                    Boolean.class,
                    id
            ));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public boolean existsByThemeId(final Long id) {
        try {
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM reservation WHERE theme_id = ?)",
                    Boolean.class,
                    id
            ));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        try {
            return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                    "SELECT EXISTS (SELECT 1 FROM reservation WHERE (date, time_id, theme_id) = (?, ?, ?))",
                    Boolean.class,
                    date,
                    timeId,
                    themeId
            ));
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }

    @Override
    public List<AvailableReservationTimeResponse> findAvailableTimesByDateAndThemeId(final LocalDate date,
                                                                                     final Long themeId) {
        return jdbcTemplate.query(RESERVATION_TIME_ALREADY_BOOKED, (rs, rowNum) -> new AvailableReservationTimeResponse(
                rs.getLong("id"),
                LocalTime.parse(rs.getString("start_at")),
                rs.getBoolean("already_booked")
        ), date, themeId);
    }
}
