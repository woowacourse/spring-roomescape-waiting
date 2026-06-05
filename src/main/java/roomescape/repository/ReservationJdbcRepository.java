package roomescape.repository;

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
import roomescape.domain.Slot;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.User;

@Repository
public class ReservationJdbcRepository implements ReservationRepository {

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

        Slot slot = new Slot(
                resultSet.getLong("slot_id"),
                resultSet.getDate("date").toLocalDate(),
                theme,
                time,
                store
        );

        return new Reservation(
                resultSet.getLong("id"),
                user,
                slot,
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
        String sql = """
                select r.id, rs.id as slot_id, rs.date, r.status,
                       u.id as u_id, u.username as u_username, u.password as u_password,
                       u.name as u_name, u.role as u_role,
                       t.id as time_id, t.start_at,
                       th.id as theme_id, th.name as theme_name, th.description, th.thumbnail_image_url,
                       s.id as store_id, s.name as store_name
                from reservation r
                join reservation_slot rs on r.slot_id = rs.id
                join users u on r.user_id = u.id
                join reservation_time t on rs.time_id = t.id
                join theme th on rs.theme_id = th.id
                join store s on rs.store_id = s.id
                where s.id in (%s)
                order by r.id limit ? offset ?
                """.formatted(placeholders(storeIds.size()));
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
        String sql = """
                select r.id, rs.id as slot_id, rs.date, r.status,
                       u.id as u_id, u.username as u_username, u.password as u_password,
                       u.name as u_name, u.role as u_role,
                       t.id as time_id, t.start_at,
                       th.id as theme_id, th.name as theme_name, th.description, th.thumbnail_image_url,
                       s.id as store_id, s.name as store_name
                from reservation r
                join reservation_slot rs on r.slot_id = rs.id
                join users u on r.user_id = u.id
                join reservation_time t on rs.time_id = t.id
                join theme th on rs.theme_id = th.id
                join store s on rs.store_id = s.id
                where s.id in (%s) and u.name = ?
                order by r.id limit ? offset ?
                """.formatted(placeholders(storeIds.size()));
        List<Object> args = new ArrayList<>(storeIds);
        args.add(name);
        args.add(limit);
        args.add(offset);
        return jdbcTemplate.query(sql, rowMapper, args.toArray());
    }

    @Override
    public Map<Reservation, Integer> findAllByUserIdWithWaitingOrder(Long userId, int limit, int offset) {
        String sql = """
                select *
                from (
                    select r.id, rs.id as slot_id, rs.date, r.status,
                           u.id as u_id, u.username as u_username, u.password as u_password,
                           u.name as u_name, u.role as u_role,
                           t.id as time_id, t.start_at,
                           th.id as theme_id, th.name as theme_name, th.description, th.thumbnail_image_url,
                           s.id as store_id, s.name as store_name,
                           row_number() over (
                               partition by rs.date, rs.time_id, rs.theme_id, rs.store_id, r.status
                               order by r.id
                           ) as waiting_order
                    from reservation r
                    join reservation_slot rs on r.slot_id = rs.id
                    join users u on r.user_id = u.id
                    join reservation_time t on rs.time_id = t.id
                    join theme th on rs.theme_id = th.id
                    join store s on rs.store_id = s.id
                ) ranked_reservation
                where u_id = ?
                order by case status
                             when 'RESERVED' then 0
                             when 'WAITING' then 1
                             else 2
                         end,
                         date, start_at, waiting_order, id
                limit ? offset ?
                """;

        Map<Reservation, Integer> reservations = new LinkedHashMap<>();
        jdbcTemplate.query(sql, resultSet -> {
            Reservation reservation = rowMapper.mapRow(resultSet, resultSet.getRow());
            reservations.put(reservation, resultSet.getInt("waiting_order"));
        }, userId, limit, offset);
        return reservations;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = """
                select r.id, rs.id as slot_id, rs.date, r.status,
                       u.id as u_id, u.username as u_username, u.password as u_password,
                       u.name as u_name, u.role as u_role,
                       t.id as time_id, t.start_at,
                       th.id as theme_id, th.name as theme_name, th.description, th.thumbnail_image_url,
                       s.id as store_id, s.name as store_name
                from reservation r
                join reservation_slot rs on r.slot_id = rs.id
                join users u on r.user_id = u.id
                join reservation_time t on rs.time_id = t.id
                join theme th on rs.theme_id = th.id
                join store s on rs.store_id = s.id
                where r.id = ?
                """;
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
        String sql = "insert into reservation(user_id, slot_id, status) values(?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, reservation.getUser().getId());
            ps.setLong(2, reservation.getSlot().getId());
            ps.setString(3, reservation.getStatus().name());
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
        String sql = "update reservation set user_id = ?, slot_id = ? where id = ?";
        return jdbcTemplate.update(sql,
                reservation.getUser().getId(),
                reservation.getSlot().getId(),
                reservation.getId());
    }

    @Override
    public int updateStatus(Long id, ReservationStatus status) {
        String sql = "update reservation set status = ? where id = ?";
        return jdbcTemplate.update(sql, status.name(), id);
    }

    @Override
    public Optional<Reservation> findFirstWaitingBySlotId(Long slotId) {
        String sql = """
                select r.id, rs.id as slot_id, rs.date, r.status,
                       u.id as u_id, u.username as u_username, u.password as u_password,
                       u.name as u_name, u.role as u_role,
                       t.id as time_id, t.start_at,
                       th.id as theme_id, th.name as theme_name, th.description, th.thumbnail_image_url,
                       s.id as store_id, s.name as store_name
                from reservation r
                join reservation_slot rs on r.slot_id = rs.id
                join users u on r.user_id = u.id
                join reservation_time t on rs.time_id = t.id
                join theme th on rs.theme_id = th.id
                join store s on rs.store_id = s.id
                where r.slot_id = ? and r.status = ?
                order by r.id
                limit 1
                """;
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    sql, rowMapper, slotId, ReservationStatus.WAITING.name()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsBySlotIdAndStatus(Long slotId, ReservationStatus status) {
        String sql = "select exists(select 1 from reservation where slot_id = ? and status = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, slotId, status.name()));
    }

    @Override
    public List<Long> findTimeIdsByThemeIdAndDate(Long themeId, LocalDate date) {
        String sql = """
                select rs.time_id
                from reservation r
                join reservation_slot rs on r.slot_id = rs.id
                where rs.theme_id = ? and rs.date = ?
                """;
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> resultSet.getLong("time_id"), themeId, date);
    }

    @Override
    public boolean existsByDateAndTimeAndThemeAndStore(LocalDate date, Long timeId, Long themeId, Long storeId) {
        String sql = """
                select exists(
                    select 1 from reservation r join reservation_slot rs on r.slot_id = rs.id
                    where rs.date = ? and rs.time_id = ? and rs.theme_id = ? and rs.store_id = ?)
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, storeId));
    }

    @Override
    public boolean existsByReservationTimeId(Long timeId) {
        String sql = """
                select exists(
                    select 1 from reservation r join reservation_slot rs on r.slot_id = rs.id
                    where rs.time_id = ?)
                """;
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, timeId));
    }

    @Override
    public boolean existsByDateAndTimeAndThemeAndStoreAndUser(LocalDate date, Long timeId, Long themeId, Long store_id,
                                                              Long userId) {
        String sql = """
                select exists(
                    select 1 from reservation r join reservation_slot rs on r.slot_id = rs.id
                    where rs.date = ? and rs.time_id = ? and rs.theme_id = ? and rs.store_id = ? and r.user_id = ?)
                """;
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, store_id, userId));
    }

    @Override
    public boolean existsByDateAndTimeAndThemeAndStoreAndStatus(LocalDate date, Long timeId, Long themeId,
                                                                Long storeId,
                                                                ReservationStatus reservationStatus) {
        String sql = """
                select exists(
                    select 1 from reservation r join reservation_slot rs on r.slot_id = rs.id
                    where rs.date = ? and rs.time_id = ? and rs.theme_id = ? and rs.store_id = ? and r.status = ?)
                """;
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, Boolean.class, date, timeId, themeId, storeId, reservationStatus.name()));
    }

    @Override
    public int countWaitingByDateAndTimeAndThemeAndStore(LocalDate date, Long timeId, Long themeId, Long storeId) {
        String sql = """
                select count(*) from reservation r join reservation_slot rs on r.slot_id = rs.id
                where rs.date = ? and rs.time_id = ? and rs.theme_id = ? and rs.store_id = ? and r.status = ?
                """;
        Integer count = jdbcTemplate.queryForObject(
                sql, Integer.class, date, timeId, themeId, storeId, ReservationStatus.WAITING.name());
        return count == null ? 0 : count;
    }
}
