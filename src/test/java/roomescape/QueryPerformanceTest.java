package roomescape;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(webEnvironment = NONE)
@Disabled("윈도우 함수를 이용한 쿼리 vs 별도 쿼리 의 성능을 비교하기 위한 테스트")
public class QueryPerformanceTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long timeId1;
    private Long timeId2;
    private Long themeId1;
    private Long themeId2;

    @BeforeEach
    void setUp() {
        // 1. Clean data
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");

        // 2. Insert Times
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", Time.valueOf(LocalTime.of(10, 0)));
        timeId1 = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class,
                Time.valueOf(LocalTime.of(10, 0)));

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", Time.valueOf(LocalTime.of(12, 0)));
        timeId2 = jdbcTemplate.queryForObject("SELECT id FROM reservation_time WHERE start_at = ?", Long.class,
                Time.valueOf(LocalTime.of(12, 0)));

        // 3. Insert Themes
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "테마1", "설명1",
                "url1");
        themeId1 = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, "테마1");

        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "테마2", "설명2",
                "url2");
        themeId2 = jdbcTemplate.queryForObject("SELECT id FROM theme WHERE name = ?", Long.class, "테마2");

        // 4. Create Indexes to simulate optimized database environment
        jdbcTemplate.update(
                "CREATE INDEX IF NOT EXISTS idx_waiting_date_time_theme ON reservation_waiting (reservation_date, time_id, theme_id)");
        jdbcTemplate.update("CREATE INDEX IF NOT EXISTS idx_waiting_name ON reservation_waiting (name)");

        // 5. Insert dummy Reservations
        jdbcTemplate.update("INSERT INTO reservation (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", LocalDate.of(2026, 5, 1), timeId1, themeId1);
        jdbcTemplate.update("INSERT INTO reservation (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "네오", LocalDate.of(2026, 5, 1), timeId2, themeId2);

        // 6. Insert target user ("브라운") waitings
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", LocalDate.of(2026, 5, 1), timeId1, themeId1);
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                "브라운", LocalDate.of(2026, 5, 1), timeId2, themeId2);

        // 7. [Batch Insert] Insert 100,000 dummy records across 20,000 different slots (5 waitings per slot)
        System.out.println("대기 테이블 10만 건 데이터 적재 시작...");
        int totalRows = 100000;
        int batchSize = 10000;

        for (int i = 0; i < totalRows; i += batchSize) {
            final int offset = i;
            jdbcTemplate.batchUpdate(
                    "INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int j) throws SQLException {
                            int index = offset + j;
                            // Distribute across slots dynamically (vary date offset to create 20,000 slots of 5 waitings)
                            int slotIndex = index / 5;
                            LocalDate date = LocalDate.of(2026, 5, 1).plusDays(slotIndex);
                            Long timeId = (slotIndex % 2 == 0) ? timeId1 : timeId2;
                            Long themeId = (slotIndex % 2 == 0) ? themeId1 : themeId2;

                            ps.setString(1, "유저" + index);
                            ps.setDate(2, java.sql.Date.valueOf(date));
                            ps.setLong(3, timeId);
                            ps.setLong(4, themeId);
                        }

                        @Override
                        public int getBatchSize() {
                            return batchSize;
                        }
                    }
            );
        }
        System.out.println("대기 테이블 10만 건 데이터 적재 완료!");
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DROP INDEX IF EXISTS idx_waiting_date_time_theme");
        jdbcTemplate.update("DROP INDEX IF EXISTS idx_waiting_name");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
    }

    private void simulateDbNetworkDelay() {
        try {
            Thread.sleep(1); // 1ms simulated DB network roundtrip
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void runExperiment() {
        int iterations = 100;
        String name = "브라운";

        // Warm up
        for (int i = 0; i < 5; i++) {
            runOneQueryApproach(name);
            runThreeQueryApproach(name);
        }

        // Test 1-Query Window Function
        long start1 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            runOneQueryApproach(name);
        }
        long end1 = System.currentTimeMillis();
        long totalOneQueryTime = end1 - start1;

        // Test 3-Query In-Memory
        long start2 = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            runThreeQueryApproach(name);
        }
        long end2 = System.currentTimeMillis();
        long totalThreeQueryTime = end2 - start2;

        System.out.println("==================================================");
        System.out.println("10만 건 적재 후 대기 순번 조회 성능 비교 결과 (반복: " + iterations + ")");
        System.out.println("--------------------------------------------------");
        System.out.println("1-Query 방식 (SQL Window Function): " + totalOneQueryTime + " ms");
        System.out.println("  (평균 1회 수행 시간: " + ((double) totalOneQueryTime / iterations) + " ms)");
        System.out.println("3-Query 방식 (In-Memory 정렬):     " + totalThreeQueryTime + " ms");
        System.out.println("  (평균 1회 수행 시간: " + ((double) totalThreeQueryTime / iterations) + " ms)");
        System.out.println("==================================================");
    }

    private void runOneQueryApproach(String targetName) {
        String sql = """
                SELECT id, name, reservation_date, time_id, time_start_at,
                       theme_id, theme_name, theme_description, theme_thumbnail_url,
                       status, waiting_order
                FROM (
                    SELECT r.id AS id,
                           r.name AS name,
                           r.reservation_date,
                           r.time_id,
                           t.start_at AS time_start_at,
                           h.id AS theme_id,
                           h.name AS theme_name,
                           h.description AS theme_description,
                           h.thumbnail_url AS theme_thumbnail_url,
                           'reserved' AS status,
                           0 AS waiting_order
                    FROM reservation r
                    INNER JOIN reservation_time t ON r.time_id = t.id
                    INNER JOIN theme h ON r.theme_id = h.id
                    WHERE r.name = ?
                    UNION ALL
                    SELECT ranked.id,
                           ranked.name,
                           ranked.reservation_date,
                           ranked.time_id,
                           ranked.time_start_at,
                           ranked.theme_id,
                           ranked.theme_name,
                           ranked.theme_description,
                           ranked.theme_thumbnail_url,
                           ranked.status,
                           ranked.waiting_order
                    FROM (
                        SELECT rw.id AS id,
                               rw.name AS name,
                               rw.reservation_date AS reservation_date,
                               rw.time_id,
                               t.start_at AS time_start_at,
                               h.id AS theme_id,
                               h.name AS theme_name,
                               h.description AS theme_description,
                               h.thumbnail_url AS theme_thumbnail_url,
                               'waiting' AS status,
                               ROW_NUMBER() OVER (PARTITION BY rw.reservation_date, rw.time_id, rw.theme_id ORDER BY rw.id) AS waiting_order
                        FROM reservation_waiting rw
                        INNER JOIN reservation_time t ON rw.time_id = t.id
                        INNER JOIN theme h ON rw.theme_id = h.id
                    ) ranked
                    WHERE ranked.name = ?
                ) combined
                ORDER BY reservation_date, time_id, theme_id, status
                """;

        jdbcTemplate.queryForList(sql, targetName, targetName);
        simulateDbNetworkDelay(); // 1 query execution delay
    }

    private void runThreeQueryApproach(String targetName) {
        // Query 1: Reservations
        String sqlReservations = "SELECT r.id, r.name, r.reservation_date, r.time_id, r.theme_id FROM reservation r WHERE r.name = ?";
        List<Map<String, Object>> reservations = jdbcTemplate.queryForList(sqlReservations, targetName);
        simulateDbNetworkDelay();

        // Query 2: Waitings
        String sqlWaitings = "SELECT w.id, w.name, w.reservation_date, w.time_id, w.theme_id FROM reservation_waiting w WHERE w.name = ?";
        List<Map<String, Object>> waitings = jdbcTemplate.queryForList(sqlWaitings, targetName);
        simulateDbNetworkDelay();

        if (waitings.isEmpty()) {
            return;
        }

        // Query 3: Batch query for slots
        StringBuilder sqlAllQueue = new StringBuilder("""
                SELECT r.id, r.name, r.reservation_date, r.time_id, r.theme_id
                FROM reservation_waiting r
                WHERE 
                """);
        List<Object> params = new ArrayList<>();
        for (int i = 0; i < waitings.size(); i++) {
            if (i > 0) {
                sqlAllQueue.append(" OR ");
            }
            sqlAllQueue.append("(r.reservation_date = ? AND r.time_id = ? AND r.theme_id = ?)");
            Map<String, Object> w = waitings.get(i);
            params.add(w.get("reservation_date"));
            params.add(w.get("time_id"));
            params.add(w.get("theme_id"));
        }

        List<Map<String, Object>> allWaitingsForSlots = jdbcTemplate.queryForList(sqlAllQueue.toString(),
                params.toArray());
        simulateDbNetworkDelay();

        // In-memory grouping and sorting emulation
        Map<String, List<Map<String, Object>>> grouped = allWaitingsForSlots.stream()
                .collect(Collectors.groupingBy(w ->
                        w.get("reservation_date").toString() + "-" + w.get("time_id").toString() + "-" + w.get(
                                "theme_id").toString()
                ));

        for (Map<String, Object> w : waitings) {
            String key =
                    w.get("reservation_date").toString() + "-" + w.get("time_id").toString() + "-" + w.get("theme_id")
                            .toString();
            List<Map<String, Object>> slotQueue = grouped.getOrDefault(key, new ArrayList<>());

            // Sort by id
            slotQueue.sort((o1, o2) -> ((Long) o1.get("id")).compareTo((Long) o2.get("id")));

            int rank = -1;
            for (int index = 0; index < slotQueue.size(); index++) {
                if (slotQueue.get(index).get("id").equals(w.get("id"))) {
                    rank = index + 1;
                    break;
                }
            }
        }
    }
}
