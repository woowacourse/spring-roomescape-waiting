package roomescape.infrastructure;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Reservation> reservationRowMapper = (resultSet, rowNum) -> new Reservation(
            resultSet.getLong("reservation_id"),
            new Member(
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getString("member_email"),
                    resultSet.getString("member_password"),
                    Role.findByName(resultSet.getString("member_role"))
            ),
            LocalDate.parse(resultSet.getString("date")),
            new ReservationTime(
                    resultSet.getLong("time_id"),
                    LocalTime.parse(resultSet.getString("time_start_at"))
            ),
            new Theme(
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail")
            )
    );
    private static final String BASIC_SELECT_QUERY = """
            SELECT 
                r.id AS reservation_id, 
                r.date,
                t.id AS time_id, 
                t.start_at AS time_start_at, 
                th.id AS theme_id, 
                th.name AS theme_name, 
                th.description AS theme_description, 
                th.thumbnail AS theme_thumbnail,
                m.id AS member_id,
                m.name AS member_name,
                m.email AS member_email,
                m.role AS member_role,
                m.password AS member_password
            FROM reservation AS r 
            INNER JOIN reservation_time AS t ON r.time_id = t.id  
            INNER JOIN theme AS th ON r.theme_id = th.id
            INNER JOIN member AS m ON r.member_id = m.id
            """;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public List<Reservation> findAll() {
        List<Reservation> reservations = jdbcTemplate.query(BASIC_SELECT_QUERY, reservationRowMapper);

        return Collections.unmodifiableList(reservations);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = BASIC_SELECT_QUERY + "WHERE r.id = ?";

        try {
            Reservation reservation = jdbcTemplate.queryForObject(sql, reservationRowMapper, id);
            return Optional.ofNullable(reservation);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> params = Map.of(
                "member_id", reservation.getMember().getId(),
                "date", reservation.getDate(),
                "time_id", reservation.getTime().getId(),
                "theme_id", reservation.getTheme().getId()
        );
        Long id = jdbcInsert.executeAndReturnKey(params).longValue();

        return new Reservation(id, reservation.getMember(), reservation.getDate(), reservation.getTime(),
                reservation.getTheme());
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existByReservationTimeId(Long id) {
        String sql = BASIC_SELECT_QUERY + "WHERE t.id = ?";
        List<Reservation> reservations = jdbcTemplate.query(sql, reservationRowMapper, id);

        return !reservations.isEmpty();
    }

    @Override
    public boolean existByThemeId(Long id) {
        String sql = BASIC_SELECT_QUERY + "WHERE th.id = ?";
        List<Reservation> reservations = jdbcTemplate.query(sql, reservationRowMapper, id);

        return !reservations.isEmpty();
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        String sql = BASIC_SELECT_QUERY + "WHERE r.date = ? AND t.id = ? AND th.id = ?";
        List<Reservation> reservations = jdbcTemplate.query(sql, reservationRowMapper, date, timeId, themeId);

        return !reservations.isEmpty();
    }

    @Override
    public List<Reservation> filter(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        String sql = makeQueryForFilter(themeId, memberId, dateFrom, dateTo);

        return jdbcTemplate.query(sql, reservationRowMapper);
    }

    private String makeQueryForFilter(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        String sql = BASIC_SELECT_QUERY;
        List<String> wherePhrase = new LinkedList<>();

        if (themeId != null) {
            wherePhrase.add(" th.id = " + themeId);
        }
        if (memberId != null) {
            wherePhrase.add(" m.id = " + memberId);
        }
        if (dateFrom != null) {
            wherePhrase.add(" r.date >= '" + dateFrom + "'");
        }
        if (dateTo != null) {
            wherePhrase.add(" r.date <= '" + dateTo + "'");
        }

        if (wherePhrase.isEmpty()) {
            return sql;
        }

        return sql + " WHERE " + String.join(" AND ", wherePhrase);
    }
}
