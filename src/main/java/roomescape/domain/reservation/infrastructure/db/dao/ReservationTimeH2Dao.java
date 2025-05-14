package roomescape.domain.reservation.infrastructure.db.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.model.entity.ReservationTime;

@Component
@RequiredArgsConstructor
public class ReservationTimeH2Dao implements ReservationTimeDao {

    private static final RowMapper<ReservationTime> DEFAULT_ROW_MAPPER = (resultSet, rowNum) ->
            ReservationTime.builder()
                    .id(resultSet.getLong("id"))
                    .startAt(resultSet.getTime("start_at").toLocalTime())
                    .build();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<ReservationTime> selectAll() {
        String selectQuery = """
                SELECT id, start_at
                FROM reservation_time
                """;

        return jdbcTemplate.query(selectQuery, DEFAULT_ROW_MAPPER);
    }

    @Override
    public ReservationTime insertAndGet(ReservationTime reservationTime) {
        String insertQuery = "INSERT INTO reservation_time (start_at) VALUES (:startAt)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("startAt", reservationTime.getStartAt().toString());

        jdbcTemplate.update(insertQuery, params, keyHolder);
        Long id = keyHolder.getKey().longValue();

        return reservationTime.assignId(id);
    }

    @Override
    public Optional<ReservationTime> selectById(Long id) {
        String selectQuery = """
                SELECT id, start_at
                FROM reservation_time
                WHERE id = :id
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(selectQuery, Map.of("id", id), DEFAULT_ROW_MAPPER));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteQuery = "DELETE FROM reservation_time WHERE id = :id";
        jdbcTemplate.update(deleteQuery, Map.of("id", id));
    }

    @Override
    public List<ReservationTime> selectAllByThemeIdAndDate(Long themeId, LocalDate date) {
        String query = """
                SELECT rt.id, rt.start_at
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                WHERE r.theme_id = :themeId AND r.date = :date
                """;

        Map<String, Object> params = Map.of(
                "themeId", themeId,
                "date", date
        );

        return jdbcTemplate.query(query, params, DEFAULT_ROW_MAPPER);
    }
}
