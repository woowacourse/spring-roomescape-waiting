package roomescape.repository;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Member;
import roomescape.domain.MemberRole;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTheme;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationV2;
import roomescape.repository.jpa.ReservationJpaRepository;

@Repository
public class ReservationRepositoryImpl implements ReservationRepository {

    private final JdbcTemplate template;
    private final ReservationJpaRepository reservationJpaRepository;

    public ReservationRepositoryImpl(final JdbcTemplate template,
                                     final ReservationJpaRepository reservationJpaRepository) {
        this.template = template;
        this.reservationJpaRepository = reservationJpaRepository;
    }

    @Override
    public Optional<Reservation> findById(final long id) {
        String sql = joinReservationAndTime("WHERE r.id = ?");
        final List<Reservation> reservations = template.query(sql, reservationRowMapper(), id);
        if (reservations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(reservations.getFirst());
    }

    @Override
    public List<Reservation> findByDate(final LocalDate date) {
        String sql = joinReservationAndTime("WHERE r.date = ?");
        return template.query(sql, reservationRowMapper(), date.toString());
    }

    @Override
    public List<Reservation> findAll() {
        String sql = joinReservationAndTime("");
        return template.query(sql, reservationRowMapper());
    }

    @Override
    public List<ReservationV2> findAllReservationsV2() {
        String sql = joinReservationV2AndRelatedTables("");
        return template.query(sql, reservationV2RowMapper());
    }

    @Override
    public Reservation save(final Reservation reservation) {
        String sql = "insert into reservation (name, date, time_id, theme_id) values (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, reservation.getName());
            ps.setString(2, reservation.getDate().toString());
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return reservation.toEntity(id);
    }

    @Override
    public ReservationV2 saveWithMember(final ReservationV2 reservation) {
        String sql = "insert into reservation_v2 (member_id,date, time_id, theme_id) values (? ,?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getMemberId());
            ps.setString(2, reservation.getDate().toString());
            ps.setLong(3, reservation.getTime().getId());
            ps.setLong(4, reservation.getTheme().getId());
            return ps;
        }, keyHolder);

        long id = keyHolder.getKey().longValue();
        return reservation.toEntity(id);
    }

    @Override
    public int deleteById(final long id) {
        String sql = "delete from reservation_v2 where id = ?";
        return template.update(sql, id);
    }

    @Override
    public boolean existsByDateAndTime(final LocalDate date, final ReservationTime time) {
        String sql = wrapExistsQuery(joinReservationAndTime("WHERE r.date = ? and t.start_at = ?"));
        return template.queryForObject(sql, Boolean.class, date.toString(), time.getStartAt().toString());
    }

    @Override
    public List<ReservationV2> findByMemberIdAndThemeIdAndDateFromAndDateTo(final long memberId, final long themeId,
                                                                            final LocalDate dateFrom,
                                                                            final LocalDate dateTo) {
        StringBuilder queryBuilder = new StringBuilder(joinReservationV2AndRelatedTables(""));
        List<Object> params = new ArrayList<>();
        List<String> conditions = new ArrayList<>();

        if (memberId > 0) {
            conditions.add("rv.member_id = ?");
            params.add(memberId);
        }

        if (themeId > 0) {
            conditions.add("rv.theme_id = ?");
            params.add(themeId);
        }

        if (dateFrom != null && dateTo != null) {
            conditions.add("rv.date BETWEEN ? AND ?");
            params.add(dateFrom.toString());
            params.add(dateTo.toString());
        } else if (dateFrom != null) {
            conditions.add("rv.date >= ?");
            params.add(dateFrom.toString());
        } else if (dateTo != null) {
            conditions.add("rv.date <= ?");
            params.add(dateTo.toString());
        }

        if (!conditions.isEmpty()) {
            queryBuilder.append(" WHERE ");
            queryBuilder.append(String.join(" AND ", conditions));
        }

        return template.query(queryBuilder.toString(), reservationV2RowMapper(), params.toArray());
    }

    private RowMapper<Reservation> reservationRowMapper() {
        return (rs, rowNum) -> {
            ReservationTime reservationTime = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getString("time_value")
            );

            ReservationTheme reservationTheme = new ReservationTheme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail")
            );
            return new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("name"),
                    rs.getString("date"),
                    reservationTime,
                    reservationTheme
            );
        };
    }

    private RowMapper<ReservationV2> reservationV2RowMapper() {
        return (rs, rowNum) -> {
            // ReservationTime 생성
            ReservationTime reservationTime = new ReservationTime(
                    rs.getLong("time_id"),
                    rs.getString("time_value")
            );

            // ReservationTheme 생성
            ReservationTheme reservationTheme = new ReservationTheme(
                    rs.getLong("theme_id"),
                    rs.getString("theme_name"),
                    rs.getString("theme_description"),
                    rs.getString("theme_thumbnail")
            );

            Member member = new Member(
                    rs.getLong("member_id"),
                    rs.getString("member_email"),
                    rs.getString("member_password"),
                    rs.getString("member_name"),
                    rs.getString("member_session_id"),
                    MemberRole.fromName(rs.getString("member_role"))
                    );

            return new ReservationV2(
                    rs.getLong("reservation_id"),
                    rs.getString("date"),
                    reservationTime,
                    reservationTheme,
                    member
            );
        };
    }

    private String joinReservationV2AndRelatedTables(String where) {
        String sql = """
                SELECT 
                    rv.id AS reservation_id,
                    rv.date, 
                    m.id AS member_id, 
                    m.email AS member_email, 
                    m.password AS member_password, 
                    m.name AS member_name, 
                    m.session_id AS member_session_id,
                    m.role AS member_role,
                    t.id AS time_id, 
                    t.start_at AS time_value, 
                    th.id AS theme_id, 
                    th.name AS theme_name, 
                    th.description AS theme_description, 
                    th.thumbnail AS theme_thumbnail
                FROM reservation_v2 AS rv 
                INNER JOIN member AS m
                ON rv.member_id = m.id
                INNER JOIN reservation_time AS t
                ON rv.time_id = t.id
                INNER JOIN reservation_theme AS th
                ON rv.theme_id = th.id
                """;
        return sql + where;
    }

    private String wrapExistsQuery(String sql) {
        return "SELECT EXISTS(" + sql + ")";
    }

    private String joinReservationAndTime(String where) {
        String sql = """
                SELECT r.id AS reservation_id, r.name, r.date, t.id AS time_id, t.start_at AS time_value, th.name AS theme_name, th.description AS theme_description, th.thumbnail AS theme_thumbnail, th.id AS theme_id
                FROM reservation as r 
                INNER JOIN reservation_time AS t
                ON r.time_id = t.id
                INNER JOIN reservation_theme AS th
                ON r.theme_id = th.id
                """;
        return sql + where;
    }
}
