package roomescape.infrastructure;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.business.model.entity.Reservation;
import roomescape.business.model.entity.ReservationTime;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.entity.User;
import roomescape.business.model.repository.Reservations;
import roomescape.business.model.vo.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcReservations implements Reservations {

    private static final RowMapper<Reservation> ROW_MAPPER = (resultSet, rowNum) -> Reservation.restore(
            resultSet.getString("reservation_id"),
            User.restore(
                    resultSet.getString("user_id"),
                    resultSet.getString("user_role"),
                    resultSet.getString("user_name"),
                    resultSet.getString("user_email"),
                    resultSet.getString("user_password")
            ),
            resultSet.getDate("date").toLocalDate(),
            ReservationTime.restore(
                    resultSet.getString("time_id"),
                    resultSet.getTime("time_value").toLocalTime()
            ),
            Theme.restore(
                    resultSet.getString("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail")
            )
    );

    private static final String FIND_ALL_QUERY = """
            SELECT
             r.id as reservation_id,
             r.date,
             rt.id as time_id,
             rt.start_at as time_value,
             t.id as theme_id,
             t.name as theme_name,
             t.description as theme_description,
             t.thumbnail as theme_thumbnail,
             u.id as user_id,
             u.role as user_role,
             u.name as user_name,
             u.email as user_email,
             u.password as user_password
            FROM reservation as r
            INNER JOIN reservation_time as rt ON r.time_id = rt.id
            INNER JOIN theme as t ON r.theme_id = t.id
            INNER JOIN users as u ON r.user_id = u.id
            WHERE 1 = 1
            """;

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insert;

    public JdbcReservations(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation");
    }

    @Override
    public void save(final Reservation reservation) {
        insert.execute(Map.of(
                "id", reservation.getId(),
                "date", reservation.getDate().value(),
                "time_id", reservation.getTime().getId().value(),
                "theme_id", reservation.getTheme().getId().value(),
                "user_id", reservation.getUser().getId().value()
        ));
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, ROW_MAPPER);
    }

    @Override
    public List<Reservation> findAllWithFilter(final Id themeId, final Id userId, final LocalDate dateFrom, final LocalDate dateTo) {
        final StringBuilder sql = new StringBuilder(FIND_ALL_QUERY);

        List<Object> params = new ArrayList<>();

        if (themeId != null) {
            sql.append(" AND t.id = ?");
            params.add(themeId.value());
        }

        if (userId != null) {
            sql.append(" AND u.id = ?");
            params.add(userId.value());
        }

        if (dateFrom != null) {
            sql.append(" AND r.date >= ?");
            params.add(dateFrom);
        }

        if (dateTo != null) {
            sql.append(" AND r.date <= ?");
            params.add(dateTo);
        }

        return jdbcTemplate.query(sql.toString(), ROW_MAPPER, params.toArray());
    }

    @Override
    public List<Reservation> findAllByUserId(final Id userId) {
        return findAllWithFilter(null, userId, null, null);
    }

    @Override
    public Optional<Reservation> findById(final Id reservationId) {
        try {
            final String sql = FIND_ALL_QUERY + " AND r.id = ?";
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, ROW_MAPPER, reservationId.value()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existById(final Id reservationId) {
        final String sql = "SELECT COUNT(*) FROM reservation WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reservationId.value());
        return count != null && count > 0;
    }

    @Override
    public boolean existByTimeId(final Id timeId) {
        final String sql = "SELECT COUNT(*) FROM reservation WHERE time_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, timeId.value());
        return count != null && count > 0;
    }

    @Override
    public boolean existByThemeId(final Id themeId) {
        final String sql = "SELECT COUNT(*) FROM reservation WHERE theme_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, themeId.value());
        return count != null && count > 0;
    }

    @Override
    public boolean isDuplicateDateAndTimeAndTheme(LocalDate date, LocalTime time, Id themeId) {
        final String sql = """
                SELECT COUNT(*)
                FROM reservation as r
                INNER JOIN reservation_time as t
                ON t.id = r.time_id
                WHERE r.date = ?
                AND t.start_at = ?
                AND r.theme_id = ?
                """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, date, time, themeId.value());
        return count != null && count > 0;
    }

    @Override
    public void deleteById(final Id reservationId) {
        final String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, reservationId.value());
    }
}
