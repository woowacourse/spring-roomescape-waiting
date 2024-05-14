package roomescape.reservation.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryReservationTimeRepository implements ReservationTimeRepository {
    private final NamedParameterJdbcTemplate template;

    public InMemoryReservationTimeRepository(final NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    @Override
    public List<ReservationTime> findAll() {
        final String sql = "SELECT id, start_at FROM reservation_time";

        return template.query(sql, itemRowMapper());
    }

    private RowMapper<ReservationTime> itemRowMapper() {
        return ((rs, rowNum) -> new ReservationTime(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime()
        ));
    }

    @Override
    public Optional<ReservationTime> findById(final Long reservationTimeId) {
        final String sql = "SELECT id, start_at FROM reservation_time WHERE id = :id";
        try {
            final MapSqlParameterSource param = new MapSqlParameterSource()
                    .addValue("id", reservationTimeId);
            final ReservationTime reservationTime = template.queryForObject(sql, param, itemRowMapper());

            return Optional.of(reservationTime);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public ReservationTime save(final ReservationTime reservationTime) {
        final String sql = "INSERT INTO reservation_time(start_at) VALUES(:startAt)";
        final SqlParameterSource param = new BeanPropertySqlParameterSource(reservationTime);
        final KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, param, keyHolder);

        final long savedReservationTimeId = keyHolder.getKey().longValue();

        return reservationTime.initializeIndex(savedReservationTimeId);
    }

    @Override
    public int deleteById(final Long reservationTimeId) {
        final String sql = "DELETE FROM reservation_time WHERE id = :id";
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", reservationTimeId);

        return template.update(sql, param);
    }

    @Override
    public boolean existByStartAt(final LocalTime startAt) {
        final String sql = "SELECT EXISTS(SELECT 1 FROM reservation_time WHERE start_at = :startAt)";
        final MapSqlParameterSource param = new MapSqlParameterSource()
                .addValue("startAt", startAt);

        return Boolean.TRUE.equals(template.queryForObject(sql, param, Boolean.class));
    }
}
