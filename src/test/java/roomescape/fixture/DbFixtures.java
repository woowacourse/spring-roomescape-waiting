package roomescape.fixture;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import roomescape.acceptance.AuthTestSupport;
import roomescape.domain.Role;

public final class DbFixtures {

    private DbFixtures() {
    }

    public static long insertUser(JdbcTemplate jdbc, String name, Role role) {
        String username = name + "@test.com";
        try {
            jdbc.update(
                    "INSERT INTO users(name, username, password, role) VALUES (?, ?, 'hash', ?)",
                    name, username, role.name());
        } catch (DuplicateKeyException ignored) {
        }
        return jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
    }

    public static long insertMember(JdbcTemplate jdbc, String name) {
        return insertUser(jdbc, name, Role.MEMBER);
    }

    public static long insertManager(JdbcTemplate jdbc, String name) {
        return insertUser(jdbc, name, Role.MANAGER);
    }

    public static void insertTheme(JdbcTemplate jdbc, long id, String name) {
        jdbc.update(
                "INSERT INTO theme(id, name, description, thumbnail_image_url) VALUES (?, ?, '설명', 'https://thumbnail.url')",
                id, name);
    }

    public static long insertTheme(JdbcTemplate jdbc, String name) {
        return insertAndReturnKey(jdbc, "theme", Map.of(
                "name", name, "description", "설명", "thumbnail_image_url", "https://thumbnail.url"));
    }

    public static void insertTime(JdbcTemplate jdbc, long id, String startAt) {
        jdbc.update("INSERT INTO reservation_time(id, start_at) VALUES (?, ?)", id, startAt);
    }

    public static long insertTime(JdbcTemplate jdbc, String startAt) {
        return insertAndReturnKey(jdbc, "reservation_time", Map.of("start_at", startAt));
    }

    public static long insertStore(JdbcTemplate jdbc, String name) {
        return insertAndReturnKey(jdbc, "store", Map.of("name", name));
    }

    public static long defaultStoreId(JdbcTemplate jdbc) {
        List<Long> ids = jdbc.queryForList("SELECT id FROM store ORDER BY id LIMIT 1", Long.class);
        return ids.isEmpty() ? insertStore(jdbc, "기본매장") : ids.get(0);
    }

    public static long insertReservation(JdbcTemplate jdbc, String name, long themeId, String date, long timeId) {
        return insertReservation(jdbc, insertMember(jdbc, name), themeId, date, timeId);
    }

    public static long insertReservation(JdbcTemplate jdbc, long userId, long themeId, String date, long timeId) {
        return insertReservationInStore(jdbc, userId, themeId, date, timeId, defaultStoreId(jdbc));
    }

    public static long insertReservation(JdbcTemplate jdbc, long userId, long themeId, String date, long timeId,
                                         long storeId) {
        return insertReservationInStore(jdbc, userId, themeId, date, timeId, storeId);
    }

    public static long insertReservation(JdbcTemplate jdbc, long userId, long themeId, String date, long timeId,
                                         long storeId, String status) {
        return insertAndReturnKey(jdbc, "reservation", Map.of(
                "user_id", userId, "theme_id", themeId, "date", LocalDate.parse(date), "time_id", timeId,
                "store_id", storeId, "status", status));
    }

    public static long insertReservation(JdbcTemplate jdbc, long userId, long themeId, String date, long timeId,
                                         String status) {
        return insertAndReturnKey(jdbc, "reservation", Map.of(
                "user_id", userId, "theme_id", themeId, "date", LocalDate.parse(date), "time_id", timeId,
                "store_id", defaultStoreId(jdbc), "status", status));
    }

    public static long insertReservationInStore(
            JdbcTemplate jdbc, long userId, long themeId, String date, long timeId, long storeId) {
        return insertAndReturnKey(jdbc, "reservation", Map.of(
                "user_id", userId, "theme_id", themeId, "date", LocalDate.parse(date), "time_id", timeId,
                "store_id", storeId, "status", "RESERVED"));
    }

    public static void assignManager(JdbcTemplate jdbc, long storeId, long userId) {
        jdbc.update("INSERT INTO store_managers(store_id, user_id) VALUES (?, ?)", storeId, userId);
    }

    public static String memberBearer(JdbcTemplate jdbc, String name) {
        long userId = insertMember(jdbc, name);
        return AuthTestSupport.bearer(userId, name + "@test.com", Role.MEMBER);
    }

    public static String managerBearer(JdbcTemplate jdbc, String name) {
        long userId = insertManager(jdbc, name);
        return AuthTestSupport.bearer(userId, name + "@test.com", Role.MANAGER);
    }

    private static long insertAndReturnKey(JdbcTemplate jdbc, String table, Map<String, Object> values) {
        return new SimpleJdbcInsert(jdbc)
                .withTableName(table)
                .usingGeneratedKeyColumns("id")
                .usingColumns(values.keySet().toArray(new String[0]))
                .executeAndReturnKey(values)
                .longValue();
    }
}
