package roomescape.reservation.infra.jdbc;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateRepository;
import roomescape.common.exception.InternalServerException;
import roomescape.common.exception.errors.RoomescapeErrors;

@Repository
@RequiredArgsConstructor
public class JdbcReservationDateRepository implements ReservationDateRepository {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DATE = "date";

    private static final String INSERT_SQL = "insert into reservation_date(`date`) values (?)";
    private static final String FIND_BY_ID_SQL = "select id, `date` from reservation_date where id = ?";
    private static final String FIND_BY_DATE_SQL = "select id, `date` from reservation_date where `date` = ?";
    private static final String FIND_ALL_SQL = "select id, `date` from reservation_date order by id";
    private static final String DELETE_BY_ID_SQL = "delete from reservation_date where id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Optional<ReservationDate> findById(Long id) {
        List<ReservationDate> result = jdbcTemplate.query(FIND_BY_ID_SQL, reservationDateRowMapper(), id);
        return result.stream().findFirst();
    }

    @Override
    public List<ReservationDate> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, reservationDateRowMapper());
    }

    @Override
    public ReservationDate save(ReservationDate reservationDate) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setDate(1, Date.valueOf(reservationDate.getDate()));
            return ps;
        }, keyHolder);
        long id = extractId(keyHolder);
        return ReservationDate.of(
            id,
            reservationDate.getDate()
        );
    }

    @Override
    public int deleteById(Long id) {
        return jdbcTemplate.update(DELETE_BY_ID_SQL, id);
    }

    @Override
    public Optional<ReservationDate> findByDate(LocalDate startWhen) {
        List<ReservationDate> result = jdbcTemplate.query(FIND_BY_DATE_SQL, reservationDateRowMapper(), startWhen);
        return result.stream().findFirst();
    }

    private RowMapper<ReservationDate> reservationDateRowMapper() {
        return (rs, rowNum) -> ReservationDate.of(
            rs.getLong(COLUMN_ID),
            rs.getDate(COLUMN_DATE).toLocalDate()
        );
    }

    private long extractId(KeyHolder keyHolder) {
        if (keyHolder.getKey() == null) {
            throw new InternalServerException(RoomescapeErrors.INVALID_GENERATED_KEY);
        }
        return keyHolder.getKey().longValue();
    }
}
