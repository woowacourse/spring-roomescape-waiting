package roomescape.repository;

import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.exception.ReservationNotFoundException;
import roomescape.repository.mapper.DomainRowMapperFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class JdbcReservationRepository implements ReservationRepository {

    private static final String BASE_SQL = """
            SELECT
                r.id AS r_id,
                r.name,
                r.amount,
                s.id AS session_id,
                s.date,
                t.id AS t_id,
                t.start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail_url AS theme_thumbnail_url
            FROM reservation r
            INNER JOIN session s ON r.session_id = s.id
            INNER JOIN time_slot t ON s.time_id = t.id
            INNER JOIN theme th ON s.theme_id = th.id
            """;
    private static final String FIND_ALL_SQL = BASE_SQL;
    private static final String FIND_BY_ID_SQL = BASE_SQL + " WHERE r.id = ?";
    private static final String FIND_BY_NAME_SQL = BASE_SQL + " WHERE r.name = ?";
    private static final String FIND_BY_CONDITIONS_SQL = BASE_SQL + " WHERE s.date = ? AND s.time_id = ? AND s.theme_id = ?";
    private static final String DELETE_SQL = "DELETE FROM reservation WHERE id = ?";
    private static final String UPDATE_SQL = "UPDATE reservation SET name = ?, session_id = ?, amount = ? WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final RowMapper<Reservation> rowMapper;

    public JdbcReservationRepository(JdbcTemplate jdbcTemplate, DomainRowMapperFactory mapperFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        this.rowMapper = (rs, rowNum) -> new Reservation(
                rs.getLong("r_id"),
                rs.getString("name"),
                mapperFactory.mapSession(rs),
                rs.getLong("amount")
        );
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, rowMapper);
    }

    @Override
    public Optional<Reservation> findById(long reservationId) {
        List<Reservation> reservations = jdbcTemplate.query(FIND_BY_ID_SQL, rowMapper, reservationId);
        return Optional.ofNullable(DataAccessUtils.singleResult(reservations));
    }

    @Override
    public List<Reservation> findByName(String name) {
        return jdbcTemplate.query(FIND_BY_NAME_SQL, rowMapper, name);
    }

    public Optional<Reservation> findByDateAndTimeIdAndThemeId(java.time.LocalDate date, Long timeId, Long themeId) {
        return jdbcTemplate.query(FIND_BY_CONDITIONS_SQL, rowMapper, date, timeId, themeId).stream().findAny();
    }

    @Override
    public Reservation save(Reservation reservation) {
        Map<String, Object> params = Map.of(
                "name", reservation.getName(),
                "session_id", reservation.getSession().getId(),
                "amount", reservation.getAmount() != null ? reservation.getAmount() : 0L
        );
        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return new Reservation(id, reservation.getName(), reservation.getSession(), reservation.getAmount());
    }

    @Override
    public void deleteById(long reservationId) {
        jdbcTemplate.update(DELETE_SQL, reservationId);
    }

    @Override
    public Reservation update(Reservation reservation) {
        int columns = jdbcTemplate.update(UPDATE_SQL,
                reservation.getName(), reservation.getSession().getId(),
                reservation.getAmount() != null ? reservation.getAmount() : 0L,
                reservation.getId());
        checkUpdateResult(columns, reservation.getId());
        return reservation;
    }

    private void checkUpdateResult(int columns, long id) {
        if (columns == 0) {
            throw new ReservationNotFoundException(id);
        }
    }
}
