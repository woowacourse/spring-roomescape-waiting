package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationSearchFilter;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private static final String selectAndJoinClause = """
        select R.id,
                   R.user_id as user_id,        
                   U.name as user_name,        
                   U.role as user_role,        
                   U.email as user_email,        
                   U.password as user_password,        
                   R.date, 
                   R.time_id, 
                   RT.start_at, 
                   R.theme_id, 
                   T.name as theme_name, 
                   T.description as theme_description, 
                   T.thumbnail as theme_thumbnail 
            from RESERVATION R
            left join USERS U on R.user_id = U.id
            left join RESERVATION_TIME RT on R.time_id = RT.id
            left join THEME T on T.id = R.theme_id
        
        """;

    private final JdbcTemplate jdbcTemplate;

    public ReservationJdbcRepository(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        var sql = selectAndJoinClause + "where R.id = ?";
        var reservationList = jdbcTemplate.query(sql, RowMappers.RESERVATION, id);
        return reservationList.stream().findAny();
    }

    @Override
    public long save(final Reservation reservation) {
        var insert = new SimpleJdbcInsert(jdbcTemplate);
        var generatedId = insert.withTableName("RESERVATION")
            .usingGeneratedKeyColumns("id")
            .executeAndReturnKey(Map.of(
                "user_id", reservation.user().id(),
                "date", reservation.date(),
                "time_id", reservation.timeSlot().id(),
                "theme_id", reservation.theme().id()
            ));
        return generatedId.longValue();
    }

    @Override
    public boolean removeById(final long id) {
        var sql = "delete from RESERVATION where id = ?";

        var removedRowsCount = jdbcTemplate.update(sql, id);
        return removedRowsCount > 0;
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(selectAndJoinClause, RowMappers.RESERVATION);
    }

    @Override
    public List<Reservation> findByTimeSlotId(final long id) {
        var sql = selectAndJoinClause + "WHERE R.time_id = ?";
        return jdbcTemplate.query(sql, RowMappers.RESERVATION, id);
    }

    @Override
    public List<Reservation> findByThemeId(final long id) {
        var sql = selectAndJoinClause + "WHERE R.theme_id = ?";
        return jdbcTemplate.query(sql, RowMappers.RESERVATION, id);
    }

    @Override
    public List<Reservation> findBySearchFilter(final ReservationSearchFilter filter) {
        var whereClause = toWhereClause(filter);
        var sql = selectAndJoinClause + whereClause.clause;;
        return jdbcTemplate.query(sql, RowMappers.RESERVATION, whereClause.params);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(final LocalDate date, final long themeId) {
        var sql = selectAndJoinClause + "WHERE R.date = ? AND R.theme_id = ?";
        return jdbcTemplate.query(sql, RowMappers.RESERVATION, date, themeId);
    }

    @Override
    public Optional<Reservation> findByDateAndTimeSlotAndThemeId(final LocalDate date, final long timeSlotId, final long themeId) {
        var sql = selectAndJoinClause + "where R.date = ? AND R.time_id = ? AND R.theme_id = ?";
        var reservationList = jdbcTemplate.query(sql, RowMappers.RESERVATION, date, timeSlotId, themeId);
        return reservationList.stream().findAny();
    }

    private WhereClause toWhereClause(final ReservationSearchFilter filter) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (filter.themeId() != null) {
            conditions.add("R.theme_id = ?");
            params.add(filter.themeId());
        }
        if (filter.userId() != null) {
            conditions.add("R.user_id = ?");
            params.add(filter.userId());
        }
        if (filter.dateFrom() != null) {
            conditions.add("R.date >= ?");
            params.add(filter.dateFrom());
        }
        if (filter.dateTo() != null) {
            conditions.add("R.date <= ?");
            params.add(filter.dateTo());
        }

        if (conditions.isEmpty()) {
            return new WhereClause("", null);
        }
        var clause = "where " + String.join(" and ", conditions);
        return new WhereClause(clause, params.toArray());
    }

    private record WhereClause(
        String clause,
        Object[] params
    ) {

    }
}
