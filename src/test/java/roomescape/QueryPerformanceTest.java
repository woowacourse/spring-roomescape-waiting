//package roomescape;
//
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.jdbc.core.BatchPreparedStatementSetter;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.context.ActiveProfiles;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
//@ActiveProfiles("test")
//public class QueryPerformanceTest {
//
//    private static final int NETWORK_DELAY_MS = 2; // 실제 DB 인프라 네트워크 I/O 평균 지연시간 (2ms 가정)
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Test
//    @DisplayName("데이터 개수별 예약 목록 및 대기 순번 조회 성능 비교 테스트")
//    public void comparePerformance() {
//        int[] dataSizes = {10_000, 100_000, 300_000};
//        String targetName = "브라운";
//
//        for (int size : dataSizes) {
//            System.out.println("==================================================");
//            System.out.println("데이터 세팅 시작... (목표 데이터 수: " + size + ")");
//
//            // 매 반복마다 메타데이터와 더미 데이터를 새롭게 세팅
//            setupMetaData();
//            setupDummyData(size, targetName);
//            System.out.println("데이터 세팅 완료! 성능 측정을 시작합니다.");
//
//            // 1. 단일 쿼리로 한 번에 조회 (Window Function)
//            long timeAllInOne = measureAllInOne(targetName);
//            System.out.println("[방법 1] 단일 쿼리(JOIN + Window 함수) 소요 시간: " + timeAllInOne + " ms");
//
//            // 2. 예약/대기 조회 후 개별 쿼리로 대기 순번 구하기 (N+1 방식)
//            long timeSeparately = measureSeparately(targetName);
//            System.out.println("[방법 2] 분리 조회(N+1 개별 카운트) 소요 시간: " + timeSeparately + " ms");
//
//            System.out.println("==================================================\n");
//        }
//    }
//
//    private void setupMetaData() {
//        clearData();
//        jdbcTemplate.execute("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
//        jdbcTemplate.execute("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
//        jdbcTemplate.execute("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
//        jdbcTemplate.execute("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
//
//        for (int i = 1; i <= 100; i++) {
//            jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
//                    "테마" + i, "설명" + i, "url" + i);
//        }
//        for (int i = 1; i <= 100; i++) {
//            jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)",
//                    LocalTime.of(0, 0).plusMinutes(i));
//        }
//    }
//
//    private void setupDummyData(int size, String targetName) {
//        Random random = new Random();
//        List<Object[]> reservationArgs = new ArrayList<>();
//        List<Object[]> waitingArgs = new ArrayList<>();
//
//        // targetName 의 데이터가 무조건 일부 섞이도록 조치
//        for (int i = 0; i < size; i++) {
//            boolean isTarget = random.nextInt(100) < 5; // 5% 확률로 타겟 유저
//            String name = isTarget ? targetName : "User" + i;
//
//            // i 값을 기반으로 유니크한 조합 생성 (theme: 1~100, time: 1~100, date: 순차적 증가)
//            long themeId = (i % 100) + 1;
//            long timeId = ((i / 100) % 100) + 1;
//            LocalDate date = LocalDate.now().plusDays(i / 10000);
//
//            if (random.nextBoolean()) {
//                reservationArgs.add(new Object[]{name, date, timeId, themeId});
//            } else {
//                waitingArgs.add(new Object[]{name, date, timeId, themeId});
//            }
//
//            // 배치 인서트 (메모리 초과 방지)
//            if (reservationArgs.size() >= 10000) {
//                batchInsert("INSERT INTO reservation (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
//                        reservationArgs);
//                reservationArgs.clear();
//            }
//            if (waitingArgs.size() >= 10000) {
//                batchInsert(
//                        "INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
//                        waitingArgs);
//                waitingArgs.clear();
//            }
//        }
//
//        if (!reservationArgs.isEmpty()) {
//            batchInsert("INSERT INTO reservation (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
//                    reservationArgs);
//        }
//        if (!waitingArgs.isEmpty()) {
//            batchInsert(
//                    "INSERT INTO reservation_waiting (name, reservation_date, time_id, theme_id) VALUES (?, ?, ?, ?)",
//                    waitingArgs);
//        }
//    }
//
//    private void batchInsert(String sql, List<Object[]> args) {
//        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
//            @Override
//            public void setValues(PreparedStatement ps, int i) throws SQLException {
//                Object[] row = args.get(i);
//                ps.setString(1, (String) row[0]);
//                ps.setObject(2, row[1]);
//                ps.setLong(3, (Long) row[2]);
//                ps.setLong(4, (Long) row[3]);
//            }
//
//            @Override
//            public int getBatchSize() {
//                return args.size();
//            }
//        });
//    }
//
//    private void clearData() {
//        jdbcTemplate.execute("DELETE FROM reservation");
//        jdbcTemplate.execute("DELETE FROM reservation_waiting");
//        jdbcTemplate.execute("DELETE FROM theme");
//        jdbcTemplate.execute("DELETE FROM reservation_time");
//    }
//
//    private long measureAllInOne(String name) {
//        String sql = """
//                SELECT id, name, reservation_date, time_id, time_start_at,
//                       theme_id, theme_name, theme_description, theme_thumbnail_url,
//                       status, waiting_order
//                FROM (
//                    SELECT r.id AS id,
//                           r.name AS name,
//                           r.reservation_date,
//                           r.time_id,
//                           t.start_at AS time_start_at,
//                           h.id AS theme_id,
//                           h.name AS theme_name,
//                           h.description AS theme_description,
//                           h.thumbnail_url AS theme_thumbnail_url,
//                           'reserved' AS status,
//                           0 AS waiting_order
//                    FROM reservation r
//                    INNER JOIN reservation_time t ON r.time_id = t.id
//                    INNER JOIN theme h ON r.theme_id = h.id
//                    WHERE r.name = ?
//                    UNION ALL
//                    SELECT ranked.id,
//                           ranked.name,
//                           ranked.reservation_date,
//                           ranked.time_id,
//                           ranked.time_start_at,
//                           ranked.theme_id,
//                           ranked.theme_name,
//                           ranked.theme_description,
//                           ranked.theme_thumbnail_url,
//                           ranked.status,
//                           ranked.waiting_order
//                    FROM (
//                        SELECT rw.id AS id,
//                               rw.name AS name,
//                               rw.reservation_date AS reservation_date,
//                               rw.time_id,
//                               t.start_at AS time_start_at,
//                               h.id AS theme_id,
//                               h.name AS theme_name,
//                               h.description AS theme_description,
//                               h.thumbnail_url AS theme_thumbnail_url,
//                               'waiting' AS status,
//                               ROW_NUMBER() OVER (PARTITION BY rw.reservation_date, rw.time_id, rw.theme_id ORDER BY rw.id) AS waiting_order
//                        FROM reservation_waiting rw
//                        INNER JOIN reservation_time t ON rw.time_id = t.id
//                        INNER JOIN theme h ON rw.theme_id = h.id
//                    ) ranked
//                    WHERE ranked.name = ?
//                ) combined
//                ORDER BY reservation_date, time_id, theme_id, status
//                """;
//
//        long start = System.currentTimeMillis();
//        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, name, name);
//        simulateNetworkDelay(1); // 단일 쿼리 1회 실행에 대한 I/O 지연
//        long end = System.currentTimeMillis();
//        return end - start;
//    }
//
//    private long measureSeparately(String name) {
//        long start = System.currentTimeMillis();
//
//        // 1. 예약과 대기 목록을 UNION ALL로 한 번에 조회 (단, 순번은 계산하지 않음)
//        String baseSql = """
//                SELECT r.id, r.name, r.reservation_date, r.time_id, t.start_at,
//                       h.id AS theme_id, h.name AS theme_name, 'reserved' AS status
//                FROM reservation r
//                INNER JOIN reservation_time t ON r.time_id = t.id
//                INNER JOIN theme h ON r.theme_id = h.id
//                WHERE r.name = ?
//                UNION ALL
//                SELECT rw.id, rw.name, rw.reservation_date, rw.time_id, t.start_at,
//                       h.id AS theme_id, h.name AS theme_name, 'waiting' AS status
//                FROM reservation_waiting rw
//                INNER JOIN reservation_time t ON rw.time_id = t.id
//                INNER JOIN theme h ON rw.theme_id = h.id
//                WHERE rw.name = ?
//                """;
//        List<Map<String, Object>> records = jdbcTemplate.queryForList(baseSql, name, name);
//        simulateNetworkDelay(1); // UNION ALL 기본 쿼리 1회 실행 지연
//
//        // 2. 조회된 전체 목록 중 'waiting' 상태인 건에 대해서만 별도 쿼리로 대기 순번 계산 (N+1 발생 지점)
//        for (Map<String, Object> record : records) {
//            if ("waiting".equals(record.get("status"))) {
//                String countSql = """
//                        SELECT COUNT(*) + 1
//                        FROM reservation_waiting
//                        WHERE reservation_date = ? AND time_id = ? AND theme_id = ? AND id < ?
//                        """;
//                Long order = jdbcTemplate.queryForObject(countSql, Long.class,
//                        record.get("reservation_date"),
//                        record.get("time_id"),
//                        record.get("theme_id"),
//                        record.get("id"));
//                record.put("waiting_order", order);
//                simulateNetworkDelay(1); // N+1 쿼리마다 각각 지연시간 발생
//            }
//        }
//
//        // 4. 리스트 합치기 및 정렬은 생략 (DB I/O 성능 비교가 주 목적이므로)
//
//        long end = System.currentTimeMillis();
//        return end - start;
//    }
//
//    private void simulateNetworkDelay(int queryCount) {
//        try {
//            Thread.sleep((long) NETWORK_DELAY_MS * queryCount);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//}
