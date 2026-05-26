package roomescape.reservation.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.member.Role;
import roomescape.reservation.Reservation;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final NamedParameterJdbcTemplate template;

    private final RowMapper<ReservationDetailProjection> reservationDetailFindRowMapper = (resultSet, rowNum) ->
            new ReservationDetailProjection(
                    resultSet.getLong("reservation_id"),
                    resultSet.getLong("member_id"),
                    resultSet.getString("member_name"),
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getLong("theme_id"),
                    resultSet.getString("theme_name"),
                    resultSet.getString("theme_description"),
                    resultSet.getString("theme_thumbnail_url"),
                    resultSet.getLong("time_id"),
                    resultSet.getTime("start_at").toLocalTime()
            );

    @Override
    public Reservation save(Reservation reservation) {
        String insertReservationSql = "INSERT INTO reservation(member_id, schedule_id) VALUES (:memberId, :scheduleId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", reservation.getMemberId())
                .addValue("scheduleId", reservation.getScheduleId());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(insertReservationSql, params, keyHolder);

        Number id = keyHolder.getKey();
        if (id == null) {
            throw new IllegalStateException("reservation 저장 후 생성된 ID를 반환받지 못했습니다.");
        }

        return Reservation.of(
                keyHolder.getKey().longValue(),
                reservation.getMemberId(),
                reservation.getScheduleId()
        );
    }

    @Override
    public List<ReservationDetailProjection> findAll() {
        String sql = """
                 SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    s.date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    rt.id AS time_id,
                    rt.start_at
                FROM reservation r
                JOIN schedule s ON r.schedule_id = s.id
                JOIN theme t ON s.theme_id = t.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN member m ON r.member_id = m.id
                ORDER BY r.id
                """;

        return template.query(sql, reservationDetailFindRowMapper);
    }

    @Override
    public Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId) {
        String sql = """
                SELECT
                    s.time_id
                FROM schedule s
                LEFT JOIN reservation r ON s.id = r.schedule_id
                WHERE s.date = :date
                AND s.theme_id = :themeId
                AND r.id IS NOT NULL
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("date", date)
                .addValue("themeId", themeId);

        return Set.copyOf(template.query(sql, params,
                (rs, rowNum) -> rs.getLong("time_id")));
    }

    @Override
    public List<ReservationDetailProjection> findAllReservationDetailsByMemberId(long memberId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    s.date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    rt.id AS time_id,
                    rt.start_at
                FROM reservation r
                JOIN schedule s ON r.schedule_id = s.id
                JOIN theme t ON s.theme_id = t.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN member m ON r.member_id = m.id
                WHERE m.id = :memberId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", memberId);

        return template.query(sql, params, reservationDetailFindRowMapper);
    }

    public void deleteByIdAndMemberId(long reservationId, long memberId) {
        String sql = "DELETE FROM reservation WHERE id = :reservationId AND member_id = :memberId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId)
                .addValue("memberId", memberId);

        template.update(sql, params);
    }

    @Override
    public void deleteById(long reservationId) {
        String sql = "DELETE FROM reservation WHERE id = :reservationId";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId);
        template.update(sql, params);
    }

    @Override
    public Optional<ReservationDetailProjection> findDetailById(long reservationId) {
        String sql = """
                SELECT
                    r.id AS reservation_id,
                    m.id AS member_id,
                    m.name AS member_name,
                    s.date,
                    t.id AS theme_id,
                    t.name AS theme_name,
                    t.description AS theme_description,
                    t.thumbnail_url AS theme_thumbnail_url,
                    rt.id AS time_id,
                    rt.start_at
                FROM reservation r
                JOIN schedule s ON r.schedule_id = s.id
                JOIN theme t ON s.theme_id = t.id
                JOIN reservation_time rt ON s.time_id = rt.id
                JOIN member m ON r.member_id = m.id
                 WHERE r.id = :id
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservationId);

        return template.query(sql, params, reservationDetailFindRowMapper)
                .stream()
                .findFirst();
    }

    @Override
    public boolean existsByScheduleIdAndIdNot(long scheduleId, long reservationId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation r WHERE r.schedule_id = :scheduleId AND r.id <> :reservationId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId)
                .addValue("scheduleId", scheduleId);

        return Boolean.TRUE.equals(template.queryForObject(sql, params, Boolean.class));
    }

    @Override
    public int updateScheduleById(long reservationId, long scheduleId) {
        String sql = """ 
                UPDATE reservation
                SET schedule_id = :scheduleId
                WHERE id = :reservationId
                """;
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reservationId", reservationId)
                .addValue("scheduleId", scheduleId);

        return template.update(sql, params);
    }

    @Override
    public Optional<Reservation> findById(long reservationId) {
        String sql = "SELECT * FROM reservation WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", reservationId);

        return template.query(sql, params,
                (resultSet, rowNum) -> new Reservation(
                        resultSet.getLong("id"),
                        resultSet.getLong("member_id"),
                        resultSet.getLong("schedule_id")
                )
        ).stream().findFirst();
    }

    @Override
    public boolean existsByScheduleId(long scheduleId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE schedule_id = :scheduleId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("scheduleId", scheduleId);

        return Boolean.TRUE.equals(template.queryForObject(sql, params, Boolean.class));
    }
}
