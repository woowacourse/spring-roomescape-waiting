package roomescape.reservationtime.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.reservationtime.ReservationTime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReservationTimeRepository implements ReservationTimeRepository {
    private final NamedParameterJdbcTemplate template;
    private final RowMapper<ReservationTime> reservationTimeRowMapper = (resultSet, rowNum) ->
            new ReservationTime(
                    resultSet.getLong("id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    @Override
    public ReservationTime save(ReservationTime time) {
        String sql = "INSERT INTO reservation_time(start_at) VALUES (:start_at)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("start_at", time.getStartAt());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(sql, params, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("reservation_time 저장 후 생성된 ID를 반환받지 못했습니다.");
        }

        return new ReservationTime(keyHolder.getKey().longValue(), time.getStartAt());
    }

    @Override
    public List<ReservationTime> findAll() {
        String sql = "SELECT id, start_at FROM reservation_time";

        return template.query(sql, reservationTimeRowMapper);
    }

    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM reservation_time WHERE id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", id);

        template.update(sql, params);
    }

    public Optional<ReservationTime> findById(long timeId) {
        String sql = "SELECT * FROM reservation_time WHERE id = :timeId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("timeId", timeId);

        return template.query(sql, params, reservationTimeRowMapper).stream().findFirst();
    }


    @Override
    public List<ReservationTime> findTimesByDateAndThemeId(LocalDate date, long themeId) {
        String sql = "SELECT rt.id, rt.start_at FROM schedule s " +
                "JOIN reservation_time rt ON s.time_id = rt.id " +
                "WHERE s.date = :date AND s.theme_id = :themeId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);

        return template.query(sql, params, reservationTimeRowMapper);
    }

    @Override
    public boolean existsAlreadyTime(LocalTime startAt) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation_time rt WHERE start_at = :startAt)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startAt", startAt);

        return Boolean.TRUE.equals(template.queryForObject(sql, params, Boolean.class));
    }
}
