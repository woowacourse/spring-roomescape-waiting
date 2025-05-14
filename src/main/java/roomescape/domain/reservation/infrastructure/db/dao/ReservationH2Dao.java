package roomescape.domain.reservation.infrastructure.db.dao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
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
import roomescape.domain.reservation.model.entity.Reservation;
import roomescape.domain.reservation.model.entity.ReservationTheme;
import roomescape.domain.reservation.model.entity.ReservationTime;

@Component
@RequiredArgsConstructor
public class ReservationH2Dao implements ReservationDao {

    private static final RowMapper<Reservation> DEFAULT_ROW_MAPPER = (resultSet, rowNum) ->
            Reservation.builder()
                    .id(resultSet.getLong("id"))
                    .name(resultSet.getString("name"))
                    .date(resultSet.getDate("date").toLocalDate())
                    .memberId(resultSet.getLong("member_id"))
                    .time(
                            ReservationTime.builder()
                                    .id(resultSet.getLong("time_id"))
                                    .startAt(resultSet.getTime("start_at").toLocalTime())
                                    .build()
                    )
                    .theme(
                            ReservationTheme.builder()
                                    .id(resultSet.getLong("theme_id"))
                                    .name(resultSet.getString("th_name"))
                                    .description(resultSet.getString("th_description"))
                                    .thumbnail(resultSet.getString("th_thumbnail"))
                                    .build()
                    )
                    .build();

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Reservation> selectAll() {
        String selectAllQuery = """
                SELECT r.id, r.name, r.date, r.time_id, r.theme_id, r.member_id, rt.start_at, 
                       th.name AS th_name, th.description AS th_description, th.thumbnail AS th_thumbnail
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                INNER JOIN theme th ON r.theme_id = th.id
                """;
        return jdbcTemplate.query(selectAllQuery, new MapSqlParameterSource(), DEFAULT_ROW_MAPPER);
    }

    @Override
    public Reservation insertAndGet(Reservation reservation) {
        String insertQuery = """
                INSERT INTO reservation (name, date, time_id, theme_id, member_id)
                VALUES (:name, :date, :timeId, :themeId, :memberId)
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", reservation.getName())
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("memberId", reservation.getMemberId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(insertQuery, params, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return reservation.assignId(id);
    }

    @Override
    public Optional<Reservation> selectById(Long id) {
        String selectQuery = """
                SELECT r.id, r.name, r.date, r.time_id, r.theme_id, r.member_id, rt.start_at, 
                       th.name AS th_name, th.description AS th_description, th.thumbnail AS th_thumbnail
                FROM reservation r
                INNER JOIN reservation_time rt ON r.time_id = rt.id
                INNER JOIN theme th ON r.theme_id = th.id
                WHERE r.id = :id
                """;

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    selectQuery,
                    Map.of("id", id),
                    DEFAULT_ROW_MAPPER
            ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(Long id) {
        String deleteQuery = "DELETE FROM reservation WHERE id = :id";
        jdbcTemplate.update(deleteQuery, Map.of("id", id));
    }

    @Override
    public boolean existDuplicatedDateTime(LocalDate date, Long timeId, Long themeId) {
        String query = """
                SELECT count(*)
                FROM reservation
                WHERE time_id = :timeId AND date = :date AND theme_id = :themeId
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("timeId", timeId);
        params.put("date", date);
        params.put("themeId", themeId);

        Integer count = jdbcTemplate.queryForObject(query, params, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByThemeId(Long reservationThemeId) {
        String sql = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM reservation
                        WHERE theme_id = :themeId
                    )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", reservationThemeId);

        Boolean exists = jdbcTemplate.queryForObject(sql, params, Boolean.class);
        return exists != null && exists;
    }

    @Override
    public boolean existsByTimeId(Long reservationTimeId) {
        String sql = """
                    SELECT EXISTS (
                        SELECT 1
                        FROM reservation
                        WHERE time_id = :timeId
                    )
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("timeId", reservationTimeId);

        Boolean exists = jdbcTemplate.queryForObject(sql, params, Boolean.class);
        return exists != null && exists;
    }

    @Override
    public List<Reservation> selectByFilter(Long themeId, Long memberId, LocalDate from, LocalDate to) {
        StringBuilder sql = new StringBuilder("""
                    SELECT
                    r.id,
                    r.name,
                    r.date,
                    r.member_id,
                    th.id as theme_id,
                    th.name as th_name,
                    th.description as th_description,
                    th.thumbnail as th_thumbnail,
                    rt.id as time_id,
                    rt.start_at
                FROM reservation as r
                INNER JOIN reservation_time as rt
                on r.time_id = rt.id
                INNER JOIN theme as th
                on r.theme_id = th.id
                """);

        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = new ArrayList<>();

        if (memberId != null) {
            conditions.add("r.member_id = :memberId");
            params.addValue("memberId", memberId);
        }
        if (themeId != null) {
            conditions.add("r.theme_id = :themeId");
            params.addValue("themeId", themeId);
        }
        if (from != null) {
            conditions.add("r.date >= :from");
            params.addValue("from", from);
        }
        if (to != null) {
            conditions.add("r.date <= :to");
            params.addValue("to", to);
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        return jdbcTemplate.query(sql.toString(), params, DEFAULT_ROW_MAPPER);
    }
}
