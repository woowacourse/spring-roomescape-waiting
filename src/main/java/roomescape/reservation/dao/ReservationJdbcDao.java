package roomescape.reservation.dao;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.common.exception.DuplicateEntityException;
import roomescape.common.vo.Name;
import roomescape.common.vo.Slot;
import roomescape.member.Member;
import roomescape.member.MemberRole;
import roomescape.store.Store;
import roomescape.theme.Theme;
import roomescape.time.Time;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationDao;
import roomescape.reservation.ReservationStatus;

@Repository
public class ReservationJdbcDao implements ReservationDao {
    private static final LocalDateTime SENTINEL = LocalDateTime.of(9999, 12, 31, 0, 0, 0);
    private static final String BASE_SELECT = """
            SELECT
                r.id,
                r.date,
                r.status,
                r.deleted_at,
                r.version,
                rs.id AS reservation_store_id,
                rs.name AS reservation_store_name,
                m.id AS member_id,
                m.name AS member_name,
                m.email AS member_email,
                m.password AS member_password,
                m.role AS member_role,
                ms.id AS member_store_id,
                ms.name AS member_store_name,
                t.id AS time_id,
                t.start_at AS time_start_at,
                th.id AS theme_id,
                th.name AS theme_name,
                th.thumbnail_url AS theme_thumbnail_url,
                th.description AS theme_description,
                th.price AS theme_price
            FROM reservations r
            INNER JOIN members m ON r.member_id = m.id
            INNER JOIN times t ON r.time_id = t.id
            INNER JOIN themes th ON r.theme_id = th.id
            LEFT JOIN stores rs ON r.store_id = rs.id
            LEFT JOIN stores ms ON m.store_id = ms.id
            """;

    private static final RowMapper<Theme> THEME_ROW_MAPPER = (rs, rowNum) ->
            new Theme(
                    rs.getLong("theme_id"),
                    new Name(rs.getString("theme_name")),
                    rs.getString("theme_thumbnail_url"),
                    rs.getString("theme_description"),
                    rs.getLong("theme_price")
            );
    private static final RowMapper<Time> TIME_ROW_MAPPER = (rs, rowNum) ->
            new Time(
                    rs.getLong("time_id"),
                    LocalTime.parse(rs.getString("time_start_at"))
            );
    private static final RowMapper<Member> MEMBER_ROW_MAPPER = (rs, rowNum) -> {
        Long memberStoreId = rs.getObject("member_store_id", Long.class);
        Store memberStore = memberStoreId == null ? null
                : new Store(memberStoreId, rs.getString("member_store_name"));
        return new Member(
                rs.getLong("member_id"),
                rs.getString("member_name"),
                rs.getString("member_email"),
                rs.getString("member_password"),
                MemberRole.valueOf(rs.getString("member_role")),
                memberStore
        );
    };
    private static final RowMapper<Reservation> ROW_MAPPER = (rs, rowNum) -> {
        Timestamp deletedAt = rs.getTimestamp("deleted_at");
        LocalDateTime deletedAtValue = deletedAt != null ? deletedAt.toLocalDateTime() : null;
        if (SENTINEL.equals(deletedAtValue)) {
            deletedAtValue = null;
        }
        Long reservationStoreId = rs.getObject("reservation_store_id", Long.class);
        Store reservationStore = reservationStoreId == null ? null
                : new Store(reservationStoreId, rs.getString("reservation_store_name"));
        return Reservation.reconstruct(
                rs.getLong("id"),
                MEMBER_ROW_MAPPER.mapRow(rs, rowNum),
                LocalDate.parse(rs.getString("date")),
                TIME_ROW_MAPPER.mapRow(rs, rowNum),
                THEME_ROW_MAPPER.mapRow(rs, rowNum),
                ReservationStatus.valueOf(rs.getString("status")),
                deletedAtValue,
                rs.getLong("version"),
                reservationStore
        );
    };

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationJdbcDao(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate.getJdbcTemplate())
                .withTableName("reservations")
                .usingGeneratedKeyColumns("id")
                .usingColumns("member_id", "date", "time_id", "theme_id", "store_id", "status");
    }

    @Override
    public List<Reservation> findAll() {
        return jdbcTemplate.query(BASE_SELECT, ROW_MAPPER);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        String sql = BASE_SELECT + "WHERE r.id = :id";
        return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), ROW_MAPPER)
                .stream().findFirst();
    }

    @Override
    public Reservation insert(Reservation reservation) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("member_id", reservation.getMember().getId())
                .addValue("date", reservation.getDate())
                .addValue("time_id", reservation.getTime().getId())
                .addValue("theme_id", reservation.getTheme().getId())
                .addValue("store_id", reservation.getStoreId())
                .addValue("status", reservation.getStatus().name());

        Long id = simpleJdbcInsert.executeAndReturnKey(params).longValue();
        return Reservation.reconstruct(id, reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme(), reservation.getStatus(), null, 0L,
                reservation.getStore());
    }

    @Override
    public Reservation update(Reservation reservation) {
        String sql = """
                UPDATE reservations
                SET member_id = :memberId, date = :date, time_id = :timeId, theme_id = :themeId,
                    status = :status, deleted_at = :deletedAt, version = version + 1
                WHERE id = :id AND version = :version
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("memberId", reservation.getMember().getId())
                .addValue("date", reservation.getDate())
                .addValue("timeId", reservation.getTime().getId())
                .addValue("themeId", reservation.getTheme().getId())
                .addValue("status", reservation.getStatus().name())
                .addValue("deletedAt", reservation.getDeletedAt() != null ? reservation.getDeletedAt() : SENTINEL)
                .addValue("id", reservation.getId())
                .addValue("version", reservation.getVersion());
        int updated = jdbcTemplate.update(sql, params);
        if (updated == 0) {
            throw new DuplicateEntityException("다른 사용자가 이미 수정했습니다. 다시 시도해주세요.");
        }
        return findById(reservation.getId()).orElseThrow();
    }

    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM reservations WHERE id = :id";
        return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id)) > 0;
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservations WHERE id = :id)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("id", id), Boolean.class));
    }

    @Override
    public List<Reservation> findAll(int limit, int offset) {
        String sql = BASE_SELECT + "ORDER BY r.id DESC LIMIT :limit OFFSET :offset";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        String sql = BASE_SELECT + """
                WHERE r.member_id = :memberId
                ORDER BY r.date DESC, t.start_at
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("memberId", memberId), ROW_MAPPER);
    }

    @Override
    public List<Reservation> findAllByStoreId(Long storeId) {
        String sql = BASE_SELECT + """
                WHERE r.store_id = :storeId
                ORDER BY r.date DESC, t.start_at
                """;
        return jdbcTemplate.query(sql, new MapSqlParameterSource("storeId", storeId), ROW_MAPPER);
    }

    @Override
    public long count() {
        Long result = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservations",
                new MapSqlParameterSource(),
                Long.class
        );
        return result != null ? result : 0;
    }

    @Override
    public boolean existsBySlotForUpdate(Slot slot) {
        String sql = """
                SELECT id FROM reservations
                WHERE theme_id = :themeId AND time_id = :timeId AND date = :date
                AND store_id = :storeId
                AND deleted_at = :sentinel
                FOR UPDATE
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", slot.getTheme().getId())
                .addValue("timeId", slot.getTime().getId())
                .addValue("date", slot.getDate())
                .addValue("storeId", slot.getStoreId())
                .addValue("sentinel", SENTINEL);
        return !jdbcTemplate.queryForList(sql, params, Long.class).isEmpty();
    }

    @Override
    public Optional<Reservation> findBySlotKeyForUpdate(Long themeId, Long timeId, LocalDate date, Long storeId) {
        String sql = BASE_SELECT + """
                WHERE r.theme_id = :themeId AND r.time_id = :timeId AND r.date = :date
                AND r.store_id = :storeId
                AND r.deleted_at = :sentinel
                FOR UPDATE
                """;

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("themeId", themeId)
                .addValue("timeId", timeId)
                .addValue("date", date)
                .addValue("storeId", storeId)
                .addValue("sentinel", SENTINEL);
        return jdbcTemplate.query(sql, params, ROW_MAPPER).stream().findFirst();
    }

    @Override
    public List<Long> findExpiredPendingIdsWithoutOrder(LocalDateTime threshold) {
        String sql = """
                SELECT r.id FROM reservations r
                LEFT JOIN orders o ON o.reservation_id = r.id
                WHERE r.status = 'PENDING'
                AND r.deleted_at = :sentinel
                AND r.created_at < :threshold
                AND o.id IS NULL
                """;
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("sentinel", SENTINEL)
                .addValue("threshold", threshold);
        return jdbcTemplate.queryForList(sql, params, Long.class);
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservations WHERE theme_id = :themeId)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("themeId", themeId), Boolean.class));
    }

    @Override
    public boolean existsByTimeId(Long timeId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM reservations WHERE time_id = :timeId)";
        return Boolean.TRUE.equals(
                jdbcTemplate.queryForObject(sql, new MapSqlParameterSource("timeId", timeId), Boolean.class));
    }

}
