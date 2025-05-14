package roomescape.reservation.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Repository
public class ReservationDaoImpl implements ReservationDao {

    private final SimpleJdbcInsert simpleJdbcInsert;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public ReservationDaoImpl(DataSource dataSource) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, 
                       m.id AS member_id, m.name AS member_name,
                       m.email, m.password, m.role, 
                       t.id AS time_id, t.start_at AS time_value, 
                       e.id AS theme_id, e.name AS theme_name, 
                       e.description AS theme_description, 
                       e.thumbnail AS theme_thumbnail 
                FROM reservation AS r 
                INNER JOIN reservation_time AS t 
                ON r.time_id = t.id 
                INNER JOIN theme AS e 
                ON r.theme_id = e.id 
                INNER JOIN member AS m
                ON r.member_id = m.id
                """;

        return namedParameterJdbcTemplate.query(sql, (resultSet, rowNum) -> createReservation(resultSet));
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, 
                       m.id AS member_id, m.name AS member_name,
                       m.email, m.password, m.role, 
                       t.id AS time_id, t.start_at AS time_value, 
                       e.id AS theme_id, e.name AS theme_name, 
                       e.description AS theme_description, 
                       e.thumbnail AS theme_thumbnail 
                FROM reservation AS r 
                INNER JOIN reservation_time AS t 
                ON r.time_id = t.id 
                INNER JOIN theme AS e 
                ON r.theme_id = e.id 
                INNER JOIN member AS m
                ON r.member_id = m.id 
                WHERE m.id = :memberId
                """;
        Map<String, Object> parameter = Map.of("memberId", memberId);

        return namedParameterJdbcTemplate.query(sql, parameter, (resultSet, rowNum) -> createReservation(resultSet));
    }

    @Override
    public List<Reservation> findAllByMemberAndThemeAndDate(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, 
                       m.id AS member_id, 
                       m.name AS member_name,
                       m.email, m.password, m.role, 
                       t.id AS time_id, t.start_at AS time_value, 
                       e.id AS theme_id, e.name AS theme_name, 
                       e.description AS theme_description, 
                       e.thumbnail AS theme_thumbnail 
                FROM reservation AS r 
                INNER JOIN reservation_time AS t 
                ON r.time_id = t.id 
                INNER JOIN theme AS e 
                ON r.theme_id = e.id 
                INNER JOIN member AS m 
                ON r.member_id = m.id 
                WHERE m.id = :memberId AND e.id = :themeId 
                AND r.date BETWEEN :dateFrom AND :dateTo
                """;

        Map<String, Object> parameter = Map.of(
                "memberId", memberId,
                "themeId", themeId,
                "dateFrom", dateFrom,
                "dateTo", dateTo
        );

        return namedParameterJdbcTemplate.query(sql, parameter, (resultSet, rowNum) -> createReservation(resultSet));
    }

    @Override
    public Optional<Reservation> findById(final Long id) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, 
                       m.id AS member_id, 
                       m.name AS member_name, 
                       m.email, m.password, m.role, 
                       t.id AS time_id, t.start_at AS time_value, 
                       e.id AS theme_id, e.name AS theme_name, 
                       e.description AS theme_description, 
                       e.thumbnail AS theme_thumbnail 
                FROM reservation AS r 
                INNER JOIN reservation_time AS t 
                ON r.time_id = t.id 
                INNER JOIN theme AS e 
                ON r.theme_id = e.id 
                INNER JOIN member AS m
                ON r.member_id = m.id 
                WHERE r.id = :id 
                """;
        Map<String, Object> parameter = Map.of("id", id);

        try {
            return Optional.of(namedParameterJdbcTemplate.queryForObject(sql, parameter,
                    (resultSet, rowNum) -> createReservation(resultSet)));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Boolean existsByDateAndTimeId(final LocalDate date, final Long timeId) {
        String sql = "SELECT COUNT(*) FROM reservation WHERE date = :date AND time_id = :timeId";
        Map<String, Object> parameter = Map.of("date", date, "timeId", timeId);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, parameter, Integer.class);
        return count != 0;
    }

    @Override
    public Reservation add(final Reservation reservation) {
        Map<String, Object> parameters = new HashMap<>(5);
        parameters.put("name", reservation.getMemberName());
        parameters.put("date", reservation.getDate());
        parameters.put("time_id", reservation.getTime().getId());
        parameters.put("theme_id", reservation.getTheme().getId());
        parameters.put("member_id", reservation.getMember().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();

        return new Reservation(
                id,
                reservation.getMember(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    @Override
    public void deleteById(final Long id) {
        String sql = "DELETE FROM reservation WHERE id = :id";
        Map<String, Object> parameter = Map.of("id", id);

        namedParameterJdbcTemplate.update(sql, parameter);
    }

    private Reservation createReservation(final ResultSet resultSet) throws SQLException {
        return new Reservation(
                resultSet.getLong("reservation_id"),
                new Member(
                        resultSet.getLong("member_id"),
                        resultSet.getString("member_name"),
                        resultSet.getString("email"),
                        resultSet.getString("password"),
                        Role.from(resultSet.getString("role"))
                ),
                resultSet.getDate("date").toLocalDate(),
                new ReservationTime(
                        resultSet.getLong("time_id"),
                        resultSet.getTime("time_value").toLocalTime()
                ),
                new Theme(
                        resultSet.getLong("theme_id"),
                        resultSet.getString("theme_name"),
                        resultSet.getString("theme_description"),
                        resultSet.getString("theme_thumbnail")
                ));
    }
}
