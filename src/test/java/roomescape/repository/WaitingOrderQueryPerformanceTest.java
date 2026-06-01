package roomescape.repository;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

/**
 * 동일한 데이터(1만/5만/10만 건)를 두 가지 물리 설계에 양쪽으로 시드한 뒤,
 * 의미가 동등한 "내 예약+대기 통합 조회" 쿼리의 응답 시간을 비교한다.
 *
 *  - 단일 테이블 모델 (현재): reservation 한 테이블에 확정+대기 누적,
 *    waitingOrder = 같은 (date, time, theme) 슬롯에서 자기보다 빠른 created_at 카운트
 *
 *  - 분리 테이블 모델 (가정): reservation_confirmed(슬롯당 1행) + waiting(나머지)
 *    waitingOrder = (확정 1) + 같은 슬롯의 waiting 중 자기보다 빠른 created_at 카운트
 *
 * 두 쿼리 모두 모든 row를 반환하는 "전체 조회" 시나리오로 측정한다.
 *
 * 실행:  ./gradlew test --tests roomescape.repository.WaitingOrderQueryPerformanceTest
 */
@JdbcTest
@Sql(scripts = "/perf-schema.sql")
class WaitingOrderQueryPerformanceTest {

    private static final int TIME_COUNT = 6;
    private static final int THEME_COUNT = 10;
    private static final int DATE_SPAN_DAYS = 60;
    private static final int SLOT_COUNT = DATE_SPAN_DAYS * TIME_COUNT * THEME_COUNT;
    private static final int PROGRESS_INTERVAL = 10_000;

    private static final String QUERY_UNIFIED = """
            SELECT
                r.id           AS id,
                r.reserver_name AS name,
                (SELECT COUNT(*)
                   FROM reservation r2
                  WHERE r2.date = r.date
                    AND r2.time_id = r.time_id
                    AND r2.theme_id = r.theme_id
                    AND r2.created_at < r.created_at) AS waiting_order
            FROM reservation r
            INNER JOIN reservation_time t ON r.time_id = t.id
            INNER JOIN theme th ON r.theme_id = th.id
            """;

    private static final String QUERY_SEPARATED = """
            SELECT
                rc.id   AS id,
                rc.reserver_name AS name,
                0       AS waiting_order
            FROM reservation_confirmed rc
            INNER JOIN reservation_time t ON rc.time_id = t.id
            INNER JOIN theme th ON rc.theme_id = th.id
            UNION ALL
            SELECT
                w.id   AS id,
                w.reserver_name AS name,
                1 + (SELECT COUNT(*)
                       FROM waiting w2
                      WHERE w2.date = w.date
                        AND w2.time_id = w.time_id
                        AND w2.theme_id = w.theme_id
                        AND w2.created_at < w.created_at) AS waiting_order
            FROM waiting w
            INNER JOIN reservation_time t ON w.time_id = t.id
            INNER JOIN theme th ON w.theme_id = th.id
            """;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ParameterizedTest(name = "{0} 건 — 단일 테이블 vs 분리 테이블")
    @ValueSource(ints = {10_000, 50_000, 100_000})
    @DisplayName("전체 조회 — 단일 테이블 모델 vs 분리 테이블 모델 응답 시간 비교")
    void compareUnifiedVsSeparatedModel(int totalRows) {
        log("=== rows=%,d : 시작 (단일 테이블 vs 분리 테이블) ===".formatted(totalRows));
        seedReferenceData();
        SeedSummary summary = seedBothModels(totalRows);
        log("seed 완료 — confirmed=%,d, waiting=%,d, 총=%,d".formatted(
                summary.confirmed, summary.waiting, summary.confirmed + summary.waiting));

        log("warmup");
        int warmupUnifiedCount = countRows(QUERY_UNIFIED);
        int warmupSeparatedCount = countRows(QUERY_SEPARATED);
        log("warmup 결과 row 수 — unified=%,d, separated=%,d".formatted(warmupUnifiedCount, warmupSeparatedCount));

        log("측정 1/2: 단일 테이블 모델 (correlated subquery on reservation)");
        int[] unifiedCount = new int[1];
        long unifiedNanos = measure(() -> unifiedCount[0] = countRows(QUERY_UNIFIED));
        log("측정 1/2 완료 — unified=%.2fms, rows=%,d".formatted(unifiedNanos / 1_000_000.0, unifiedCount[0]));

        log("측정 2/2: 분리 테이블 모델 (UNION + correlated subquery on waiting)");
        int[] separatedCount = new int[1];
        long separatedNanos = measure(() -> separatedCount[0] = countRows(QUERY_SEPARATED));
        log("측정 2/2 완료 — separated=%.2fms, rows=%,d".formatted(separatedNanos / 1_000_000.0, separatedCount[0]));

        double unifiedMs = unifiedNanos / 1_000_000.0;
        double separatedMs = separatedNanos / 1_000_000.0;
        double ratio = unifiedMs / separatedMs;

        System.out.printf(
                "%n>>> [rows=%,d  confirmed=%,d  waiting=%,d]  unified=%9.2fms   separated=%9.2fms   unified/separated=%.2fx%n%n",
                totalRows, summary.confirmed, summary.waiting, unifiedMs, separatedMs, ratio
        );
    }

    private int countRows(String sql) {
        return jdbcTemplate.query(sql, rs -> {
            int n = 0;
            long waitingOrderSink = 0;
            while (rs.next()) {
                waitingOrderSink += rs.getLong("waiting_order");
                n++;
            }
            if (waitingOrderSink == Long.MIN_VALUE) {
                System.out.println(waitingOrderSink);
            }
            return n;
        });
    }

    private long measure(java.util.function.IntSupplier task) {
        long start = System.nanoTime();
        task.getAsInt();
        return System.nanoTime() - start;
    }

    private static void log(String message) {
        System.out.printf("[%s] %s%n", java.time.LocalTime.now().withNano(0), message);
    }

    private void seedReferenceData() {
        for (int i = 1; i <= TIME_COUNT; i++) {
            jdbcTemplate.update(
                    "INSERT INTO reservation_time (start_at) VALUES (?)",
                    LocalTime.of(9 + i, 0).toString()
            );
        }
        for (int i = 1; i <= THEME_COUNT; i++) {
            jdbcTemplate.update(
                    "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                    "theme-" + i, "desc-" + i, "https://example.com/thumb" + i + ".jpg"
            );
        }
    }

    private record SlotRow(int idx, int dateOffset, int timeIdx, int themeIdx, int positionInSlot) {
    }

    private record SeedSummary(int confirmed, int waiting) {
    }

    /**
     * 같은 (date, time_id, theme_id) 슬롯을 라운드 로빈으로 채워서:
     *  - 슬롯에 처음 들어온 row(positionInSlot = 0)는 reservation_confirmed 로 + reservation 으로
     *  - 그 이후 row(positionInSlot > 0)는 waiting 으로 + reservation 으로
     * 양쪽 모델에 의미상 동일한 데이터가 들어가도록 한다.
     */
    private SeedSummary seedBothModels(int totalRows) {
        Long minTimeId = jdbcTemplate.queryForObject("SELECT MIN(id) FROM reservation_time", Long.class);
        Long minThemeId = jdbcTemplate.queryForObject("SELECT MIN(id) FROM theme", Long.class);
        LocalDate baseDate = LocalDate.of(2099, 1, 1);
        Instant baseInstant = Instant.parse("2099-01-01T00:00:00Z");

        int batchSize = 5_000;
        int remaining = totalRows;
        int globalIndex = 0;
        int confirmedTotal = 0;
        int waitingTotal = 0;
        long seedStart = System.nanoTime();
        log("seed 시작 — 목표 %,d 건 (단일 + 분리 양쪽)".formatted(totalRows));

        while (remaining > 0) {
            int chunk = Math.min(batchSize, remaining);
            List<SlotRow> rows = new ArrayList<>(chunk);
            for (int i = 0; i < chunk; i++) {
                int idx = globalIndex + i;
                int slotIdx = idx % SLOT_COUNT;
                int positionInSlot = idx / SLOT_COUNT;
                int dateOffset = slotIdx % DATE_SPAN_DAYS;
                int timeIdx = (slotIdx / DATE_SPAN_DAYS) % TIME_COUNT;
                int themeIdx = slotIdx / (DATE_SPAN_DAYS * TIME_COUNT);
                rows.add(new SlotRow(idx, dateOffset, timeIdx, themeIdx, positionInSlot));
            }

            insertBatch("reservation", rows, minTimeId, minThemeId, baseDate, baseInstant);

            List<SlotRow> confirmedRows = rows.stream().filter(r -> r.positionInSlot == 0).toList();
            List<SlotRow> waitingRows = rows.stream().filter(r -> r.positionInSlot > 0).toList();
            if (!confirmedRows.isEmpty()) {
                insertBatch("reservation_confirmed", confirmedRows, minTimeId, minThemeId, baseDate, baseInstant);
            }
            if (!waitingRows.isEmpty()) {
                insertBatch("waiting", waitingRows, minTimeId, minThemeId, baseDate, baseInstant);
            }

            confirmedTotal += confirmedRows.size();
            waitingTotal += waitingRows.size();
            globalIndex += chunk;
            remaining -= chunk;
            if (globalIndex % PROGRESS_INTERVAL == 0 || remaining == 0) {
                long elapsedMs = (System.nanoTime() - seedStart) / 1_000_000;
                log("  seed 진행: %,d / %,d  (누적 %,dms)".formatted(globalIndex, totalRows, elapsedMs));
            }
        }
        return new SeedSummary(confirmedTotal, waitingTotal);
    }

    private void insertBatch(String table, List<SlotRow> rows, Long minTimeId, Long minThemeId,
                             LocalDate baseDate, Instant baseInstant) {
        String sql = "INSERT INTO " + table + " (reserver_name, date, time_id, theme_id, created_at) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                SlotRow row = rows.get(i);
                String name = "user-" + row.idx + "-" + ThreadLocalRandom.current().nextInt(1_000_000);
                ps.setString(1, name);
                ps.setDate(2, Date.valueOf(baseDate.plusDays(row.dateOffset)));
                ps.setLong(3, minTimeId + row.timeIdx);
                ps.setLong(4, minThemeId + row.themeIdx);
                ps.setTimestamp(5, Timestamp.from(baseInstant.plusMillis(row.idx)));
            }

            @Override
            public int getBatchSize() {
                return rows.size();
            }
        });
    }
}
