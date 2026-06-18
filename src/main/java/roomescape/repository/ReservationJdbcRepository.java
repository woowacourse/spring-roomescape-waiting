package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import roomescape.domain.Password;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.User;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

    private static final String SELECT_BASE = """
            select r.id, r.date, r.status,
                   u.id as u_id, u.username as u_username, u.password as u_password,
                   u.name as u_name, u.role as u_role,
                   t.id as time_id, t.start_at,
                   th.id as theme_id, th.name as theme_name, th.description, th.thumbnail_image_url,
                   s.id as store_id, s.name as store_name
            from reservation r
            join users u on r.user_id = u.id
            join reservation_time t on r.time_id = t.id
            join theme th on r.theme_id = th.id
            join store s on r.store_id = s.id
            """;

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Reservation> rowMapper = (resultSet, rowNum) -> {
        User user = new User(
                resultSet.getString("u_username"),
                Password.ofHashed(resultSet.getString("u_password")),
                resultSet.getString("u_name"),
                Role.valueOf(resultSet.getString("u_role"))
        ).withId(resultSet.getLong("u_id"));

        ReservationTime time = new ReservationTime(
                resultSet.getLong("time_id"),
                resultSet.getTime("start_at").toLocalTime()
        );

        Theme theme = new Theme(
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("description"),
                resultSet.getString("thumbnail_image_url")
        );

        Store store = new Store(
                resultSet.getLong("store_id"),
                resultSet.getString("store_name")
        );

        return new Reservation(
                resultSet.getLong("id"),
                user,
                theme,
                resultSet.getDate("date").toLocalDate(),
                time,
                store,
                ReservationStatus.valueOf(resultSet.getString("status"))
        );
    };

    public ReservationJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Reservation> findAllByStoreIds(List<Long> storeIds, int limit, int offset) {
        if (storeIds.isEmpty()) {
            return List.of();
        }
        String sql = SELECT_BASE
                + " where s.id in (" + placeholders(storeIds.size()) + ")"
                + " order by r.id limit ? offset ?";
        List<Object> args = new ArrayList<>(storeIds);
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.query(sql, rowMapper, args.toArray());
    }

    @Override
    public List<Reservation> findAllByStoreIdsAndName(List<Long> storeIds, String name, int limit, int offset) {
        if (storeIds.isEmpty()) {
            return List.of();
        }
        String sql = SELECT_BASE
                + " where s.id in (" + placeholders(storeIds.size()) + ") and u.name = ?"
                + " order by r.id limit ? offset ?";
        List<Object> args = new ArrayList<>(storeIds);
        args.add(name);
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.query(sql, rowMapper, args.toArray());
    }

    @Override
    public List<Reservation> findAllByUserId(Long userId) {
        String sql = SELECT_BASE
                + """
                 where u.id = ?
                 order by case r.status
                              when 'RESERVED' then 0
                              when 'WAITING' then 1
                              else 2
                          end,
                          r.date,
                          t.start_at,
                          r.created_at,
                          r.id
                """;
        return jdbcTemplate.query(sql, rowMapper, userId);
    }

    @Override
    public Map<Reservation, Integer> findWaitingReservationsWithOrderByUserId(Long userId) {
        String sql = """
                select *
                from (
                    select r.id, r.date, r.status,
                           u.id as u_id, u.username as u_username, u.password as u_password,
                           u.name as u_name, u.role as u_role,
                           t.id as time_id, t.start_at,
                           th.id as theme_id, th.name as theme_name, th.description, th.thumbnail_image_url,
                           s.id as store_id, s.name as store_name,
                           row_number() over (
                               partition by r.date, r.time_id, r.theme_id, r.store_id
                               order by r.created_at, r.id
                           ) as waiting_order
                    from reservation r
                    join users u on r.user_id = u.id
                    join reservation_time t on r.time_id = t.id
                    join theme th on r.theme_id = th.id
                    join store s on r.store_id = s.id
                    where r.status = ?
                ) waiting_reservation
                where u_id = ?
                order by date, start_at, waiting_order
                """;

        Map<Reservation, Integer> waitingReservations = new LinkedHashMap<>();
        jdbcTemplate.query(sql, resultSet -> {
            Reservation reservation = rowMapper.mapRow(resultSet, resultSet.getRow());
            waitingReservations.put(reservation, resultSet.getInt("waiting_order"));
        }, ReservationStatus.WAITING.name(), userId);
        return waitingReservations;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = SELECT_BASE + " where r.id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private String placeholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }

    @Override
    public Long save(Reservation reservation) {
        String sql = "insert into reservation(user_id, theme_id, date, time_id, store_id, status) values(?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getUser().getId());
            ps.setLong(2, reservation.getTheme().getId());
            ps.setDate(3, Date.valueOf(reservation.getDate()));
            ps.setLong(4, reservation.getTime().getId());
            ps.setLong(5, reservation.getStore().getId());
            ps.setString(6, reservation.getStatus().name());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public int deleteById(Long id) {
        String sql = "delete from reservation where id = ?";
        return jdbcTemplate.update(sql, id);
    }

    @Override
    public int update(Reservation reservation) {
        String sql = "update reservation set user_id = ?, theme_id = ?, date = ?, time_id = ?, store_id = ?, status = ? where id = ?";
        return jdbcTemplate.update(sql,
                reservation.getUser().getId(),
                reservation.getTheme().getId(),
                Date.valueOf(reservation.getDate()),
                reservation.getTime().getId(),
                reservation.getStore().getId(),
                reservation.getStatus().name(),
                reservation.getId());
    }

    @Override
    public int updateStatus(Long id, ReservationStatus status) {
        String sql = "update reservation set status = ? where id = ?";
        return jdbcTemplate.update(sql, status.name(), id);
    }

    @Override
    public int updateWaitingToReserved(Reservation reservation) {
        String sql = "update reservation set status = ? where id = ? and status = ?";
        return jdbcTemplate.update(sql,
                ReservationStatus.RESERVED.name(),
                reservation.getId(),
                ReservationStatus.WAITING.name());
    }

    @Override
    public List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date) {
        String sql = """
                select time_id
                from reservation
                where theme_id = ? and date = ?
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getLong("time_id"), themeId, date);
    }

    @Override
    public boolean existsReservedByDateAndTimeAndThemeAndStore(LocalDate date, Long timeId, Long themeId,
                                                               Long storeId) {
        String sql = """
                select exists(
                    select 1
                    from reservation
                    where date = ?
                      and time_id = ?
                      and theme_id = ?
                      and store_id = ?
                      and status = ?
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class,
                date, timeId, themeId, storeId, ReservationStatus.RESERVED.name()));
    }

    @Override
    public boolean existsReservedOrPaymentPendingByDateAndTimeAndThemeAndStore(LocalDate date, Long timeId,
                                                                               Long themeId, Long storeId) {
        String sql = """
                select exists(
                    select 1
                    from reservation
                    where date = ?
                      and time_id = ?
                      and theme_id = ?
                      and store_id = ?
                      and status in (?, ?)
                )
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class,
                date, timeId, themeId, storeId, ReservationStatus.RESERVED.name(),
                ReservationStatus.PAYMENT_PENDING.name()));
    }

    @Override
    public boolean existsByReservationTimeId(Long timeId) {
        String sql = "select exists(select 1 from reservation where time_id = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existsByDateAndTimeAndThemeAndStoreAndUser(LocalDate date, Long timeId, Long themeId, Long storeId,
                                                              Long userId) {
        String sql = "select exists(select 1 from reservation where date = ? and time_id = ? and theme_id = ? and store_id = ? and user_id = ?)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, storeId, userId));
    }

}
