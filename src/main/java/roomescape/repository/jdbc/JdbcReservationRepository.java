package roomescape.repository.jdbc;

import static roomescape.repository.jdbc.ReservationEntityMapper.RESERVATION_ROW_MAPPER;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;
import roomescape.repository.util.RepositoryExceptionTranslator;

@Repository
@Slf4j
@RequiredArgsConstructor
public class JdbcReservationRepository implements ReservationRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Reservation save(Reservation reservation) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO reservation (name, date, theme_id, time_id) VALUES (?, ?, ?, ?)";

        RepositoryExceptionTranslator.execute(() -> {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, reservation.getName());
                ps.setDate(2, Date.valueOf(reservation.getDate()));
                ps.setLong(3, reservation.getTheme().getId());
                ps.setLong(4, reservation.getTime().getId());
                return ps;
            }, keyHolder);
        }, "이미 예약이 존재하는 시간입니다.");

        Long id = keyHolder.getKey().longValue();
        return new Reservation(id,
                reservation.getName(),
                reservation.getDate(),
                reservation.getTheme(),
                reservation.getTime());
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id);
    }

    @Override
    public boolean existByDateAndThemeIdAndTimeId(LocalDate date, long themeId, long timeId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM reservation WHERE date = ? AND theme_id = ? AND time_id = ?)";
        Boolean result = jdbcTemplate.queryForObject(sql, Boolean.class, date, themeId, timeId);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public List<Reservation> findAll() {
        String sql = """
                SELECT r.id AS res_id, r.name AS res_name, r.date AS res_date,
                       rt.id AS time_id, rt.start_at AS time_start, rt.status AS time_status,
                       t.id AS theme_id, t.name AS theme_name, t.description, t.thumbnail_image_url, t.is_active
                FROM reservation r
                JOIN reservation_time rt ON r.time_id = rt.id
                JOIN theme t ON r.theme_id = t.id
            """;
        return jdbcTemplate.query(sql, RESERVATION_ROW_MAPPER);
    }

    @Override
    public Optional<Reservation> findById(long id) {
        try {
            String sql = """
                    SELECT r.id AS res_id, r.name AS res_name, r.date AS res_date,
                           rt.id AS time_id, rt.start_at AS time_start, rt.status AS time_status,
                           t.id AS theme_id, t.name AS theme_name, t.description, t.thumbnail_image_url, t.is_active
                    FROM reservation r
                    JOIN reservation_time rt ON r.time_id = rt.id
                    JOIN theme t ON r.theme_id = t.id
                    WHERE r.id = ?
                """;
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, RESERVATION_ROW_MAPPER, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void update(Reservation reservation) {
        String sql = """
        UPDATE reservation
        SET name = ?, date = ?, theme_id = ?, time_id = ?
        WHERE id = ?
        """;

        RepositoryExceptionTranslator.execute(() -> {
            jdbcTemplate.update(
                    sql,
                    reservation.getName(),
                    Date.valueOf(reservation.getDate()),
                    reservation.getTheme().getId(),
                    reservation.getTime().getId(),
                    reservation.getId()
            );
        }, "이미 예약이 존재하는 시간입니다.");
    }
}
