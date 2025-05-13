package roomescape.reservation.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Repository
public class JdbcReservationDao implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert reservationInserter;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) ->
            new Reservation(
                    resultSet.getLong("reservation_id"),
                    new Member(
                            resultSet.getLong("member_id"),
                            resultSet.getString("member_name"),
                            resultSet.getString("member_email"),
                            resultSet.getString("member_password"),
                            resultSet.getString("member_role")
                    ),
                    new ReservationDateTime(
                            resultSet.getDate("date").toLocalDate(),
                            new ReservationTime(
                                    resultSet.getLong("time_id"),
                                    resultSet.getTime("time_value").toLocalTime()
                            )
                    ),
                    new Theme(
                            resultSet.getLong("theme_id"),
                            resultSet.getString("theme_name"),
                            resultSet.getString("theme_description"),
                            resultSet.getString("theme_thumbnail")
                    )
            );

    public JdbcReservationDao(final JdbcTemplate jdbcTemplate, final DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.reservationInserter = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Reservation save(final Reservation reservation) {
        final Map<String, Object> parameters = Map.of(
                "date", Date.valueOf(reservation.getDateTime().getDate()),
                "member_id", reservation.getMember().getId(),
                "time_id", reservation.getDateTime().getTimeId(),
                "theme_id", reservation.getTheme().getId()
        );
        final long id = reservationInserter.executeAndReturnKey(parameters).longValue();
        return reservation.withId(id);
    }

    @Override
    public List<Reservation> findAll() {
        final String sql = """
                SELECT
                    r.id AS reservation_id,
                    r.date,
                    m.id AS member_id,
                    m.name AS member_name, 
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role,
                    t.id AS time_id,
                    t.start_at AS time_value,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation AS r 
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN member AS m ON r.member_id = m.id
                """;
        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    @Override
    public List<Reservation> findAll(
            final Long memberId,
            final Long themeId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    ) {
        StringBuilder stringBuilder = new StringBuilder("""
                SELECT2
                    r.id AS reservation_id,
                    r.date,
                    m.id AS member_id,
                    m.name AS member_name, 
                    m.email AS member_email,
                    m.password AS member_password,
                    m.role AS member_role,
                    t.id AS time_id,
                    t.start_at AS time_value,
                    th.id AS theme_id,
                    th.name AS theme_name,
                    th.description AS theme_description,
                    th.thumbnail AS theme_thumbnail
                FROM reservation AS r 
                INNER JOIN reservation_time AS t ON r.time_id = t.id
                INNER JOIN theme AS th ON r.theme_id = th.id
                INNER JOIN member AS m ON r.member_id = m.id
                WHERE 1 = 1
                """);
        final List<Object> parameters = mapOptionalParameters(memberId, themeId, dateFrom, dateTo, stringBuilder);
        final String sql = stringBuilder.toString();
        if (parameters.isEmpty()) {
            return jdbcTemplate.query(sql, reservationRowMapper);
        }
        return jdbcTemplate.query(sql, reservationRowMapper, parameters.toArray());
    }

    @Override
    public void deleteById(final long id) {
        final String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsByTimeId(final long timeId) {
        final String sql = "SELECT 1 FROM reservation WHERE time_id = ? LIMIT 1";
        final List<Integer> result = jdbcTemplate.query(sql, (resultSet, rowNumber) -> resultSet.getInt(1), timeId);
        return !result.isEmpty();
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        final String sql = "SELECT 1 FROM reservation WHERE theme_id = ? LIMIT 1";
        final List<Integer> result = jdbcTemplate.query(sql, (resultSet, rowNumber) -> resultSet.getInt(1), themeId);
        return !result.isEmpty();
    }

    @Override
    public boolean existsByDateAndTimeAndTheme(final LocalDate date, final long timeId, final long themeId) {
        final String sql = """
                SELECT 1 FROM reservation 
                WHERE  date = ? AND time_id = ? AND theme_id = ?  
                LIMIT 1
                """;
        final List<Integer> result = jdbcTemplate.query(sql, (resultSet, rowNumber) -> resultSet.getInt(1),
                date, timeId, themeId);
        return !result.isEmpty();
    }

    private List<Object> mapOptionalParameters(
            final Long memberId,
            final Long themeId,
            final LocalDate dateFrom,
            final LocalDate dateTo,
            final StringBuilder stringBuilder
    ) {
        final List<Object> parameters = new ArrayList<>();
        if (memberId != null) {
            stringBuilder.append(" AND m.id = ?");
            parameters.add(memberId);
        }
        if (themeId != null) {
            stringBuilder.append(" AND th.id = ?");
            parameters.add(themeId);
        }
        if (dateFrom != null) {
            stringBuilder.append(" AND r.date >= ?");
            parameters.add(dateFrom);
        }
        if (dateTo != null) {
            stringBuilder.append(" AND r.date <= ?");
            parameters.add(dateTo);
        }
        return parameters;
    }
}
