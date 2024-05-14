package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.RoomTheme;
import roomescape.exception.BadRequestException;

@Repository
public class ReservationRepository {
    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    private final String selectSQL = """
                SELECT
                    r.ID AS reservation_id,
                    m.ID AS member_id,
                    m.NAME AS member_name,
                    m.EMAIL AS member_email,
                    m.PASSWORD AS member_password,
                    m.ROLE AS member_role,
                    r.DATE AS reservation_date,
                    t.ID AS time_id,
                    t.START_AT AS time_value,
                    th.ID AS theme_id,
                    th.NAME AS theme_name,
                    th.DESCRIPTION AS theme_description,
                    th.THUMBNAIL AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t
                ON r.time_id = t.id
                INNER JOIN theme AS th
                ON r.theme_id = th.id
                INNER JOIN member AS m
                ON r.member_id = m.id
                """;

    private final RowMapper<Reservation> reservationMapper = (rs, rowNum) -> new Reservation(
            rs.getLong("reservation_id"),
            new Member(
                    rs.getLong("member_id"),
                    rs.getString("member_name"),
                    rs.getString("member_email"),
                    rs.getString("member_password"),
                    Role.findBy(rs.getString("member_role"))
            ),
            rs.getDate("reservation_date").toLocalDate(),
            new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getTime("time_value").toLocalTime()
            ),
            new RoomTheme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail")
            )
    );

    public ReservationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation")
                .usingGeneratedKeyColumns("id");
    }

    public List<Reservation> findAll() {
        return jdbcTemplate.query(selectSQL, reservationMapper);
    }

    public List<Reservation> findByTheme(Long themeId) {
        String SELECT_SQL = """
                SELECT
                    r.ID AS reservation_id,
                    m.ID AS member_id,
                    m.NAME AS member_name,
                    m.EMAIL AS member_email,
                    m.PASSWORD AS member_password,
                    m.ROLE AS member_role,
                    r.DATE AS reservation_date,
                    t.ID AS time_id,
                    t.START_AT AS time_value,
                    th.ID AS theme_id,
                    th.NAME AS theme_name,
                    th.DESCRIPTION AS theme_description,
                    th.THUMBNAIL AS theme_thumbnail
                FROM reservation AS r
                INNER JOIN reservation_time AS t
                ON r.time_id = t.id
                INNER JOIN theme AS th
                ON r.theme_id = th.id
                INNER JOIN member AS m
                ON r.member_id = m.id
                WHERE r.THEME_ID = ?
                """;
        return jdbcTemplate.query(SELECT_SQL, reservationMapper, themeId);
    }

    public List<Reservation> findBy(Long themeId, Long memberId, LocalDate dateForm, LocalDate dateTo) {
        StringBuilder query = new StringBuilder(selectSQL);

        if (themeId != null && memberId == null && dateForm == null && dateTo == null) {
            query.append("WHERE th.ID = ?");
            return jdbcTemplate.query(query.toString(), reservationMapper, themeId);
        }

        if (themeId == null && memberId != null && dateForm == null && dateTo == null) {
            query.append("WHERE m.ID = ?");
            return jdbcTemplate.query(query.toString(), reservationMapper, memberId);
        }

        if (themeId == null && memberId == null && dateForm != null && dateTo != null) {
            query.append("WHERE ? <= r.DATE AND r.DATE <= ?");
            return jdbcTemplate.query(query.toString(), reservationMapper, dateForm, dateTo);
        }

        if (themeId != null && memberId != null && dateForm == null && dateTo == null) {
            query.append("WHERE th.ID = ? AND m.ID = ?");
            return jdbcTemplate.query(query.toString(), reservationMapper, themeId, memberId);
        }

        if (themeId != null && memberId != null && dateForm != null && dateTo != null) {
            query.append("WHERE th.ID = ? AND m.ID = ? AND ? <= r.DATE AND r.DATE <= ?");
            return jdbcTemplate.query(query.toString(), reservationMapper, themeId, memberId, dateForm, dateTo);
        }

        if (themeId == null && memberId != null && dateForm != null && dateTo != null) {
            query.append("WHERE m.ID = ? AND ? <= r.DATE AND r.DATE <= ?");
            return jdbcTemplate.query(query.toString(), reservationMapper, memberId, dateForm, dateTo);
        }

        if (themeId != null && memberId == null && dateForm != null && dateTo != null) {
            query.append("WHERE th.ID = ? AND ? <= r.DATE AND r.DATE <= ?");
            return jdbcTemplate.query(query.toString(), reservationMapper, themeId, dateForm, dateTo);
        }

        return findAll();
    }

    public boolean existsByDateTime(LocalDate date, Long timeId, Long themeId) {
        if (date == null) {
            throw new BadRequestException("날짜가 빈값일 수 없습니다.");
        }
        if (timeId == null) {
            throw new BadRequestException("시간이 빈값일 수 없습니다.");
        }
        if (themeId == null) {
            throw new BadRequestException("테마가 빈값일 수 없습니다.");
        }

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT * FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?)",
                Boolean.class, date, timeId, themeId));
    }

    public Reservation save(Reservation reservation) {
        if (reservation == null) {
            throw new BadRequestException("예약이 빈값일 수 없습니다.");
        }

        SqlParameterSource parameterSource = new MapSqlParameterSource()
                .addValue("member_id", reservation.getMember().getId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId());
        Long id = simpleJdbcInsert.executeAndReturnKey(parameterSource).longValue();
        return reservation.setId(id);
    }

    public boolean deleteById(Long id) {
        if (id == null) {
            throw new BadRequestException("id가 빈값일 수 없습니다.");
        }
        return jdbcTemplate.update("DELETE FROM reservation WHERE id = ?", id) > 0;
    }
}
