package roomescape.support;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 통합/인수 테스트의 given 단계를 빠르게 까는 헬퍼 + 검증용 조회.
 *
 * <p>given을 "실제 API 호출"이 아니라 JdbcTemplate 직접 insert로 까는 이유:
 * 테스트의 검증 대상이 아닌 사전 데이터는, 그 데이터를 만드는 API의 정상 동작까지 끌어들이지 않고 가장 싸게 깔아야 한다. (예: "대기 취소" 테스트의 given인 "예약 1건"은, 예약 생성 API가
 * 멀쩡한지와 무관하게 그냥 존재하기만 하면 된다.)
 *
 * <p>반대로 인수 테스트에서 "사용자 시나리오 그 자체"를 검증할 때는 API로 상태를 만든다.
 * given을 헬퍼로 깔지 API로 깔지는 "그 데이터 생성이 이 테스트의 검증 대상인가"로 가른다.
 *
 * <p>이 클래스는 영속 데이터를 다룬다. 메모리 도메인 객체는 Fixtures가 담당한다 — 둘을 섞지 않는다.
 */
@Component
public class ReservationTestHelper {

    private final JdbcTemplate jdbcTemplate;
    public static final String DEFAULT_NAME = "브라운";
    public static final LocalDate DEFAULT_DATE = LocalDate.of(2050, 12, 31);

    public ReservationTestHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // ---------- given: 데이터 준비 ----------

    public Long insertTime(LocalTime startAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                Time.valueOf(startAt));
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?",
                Long.class, Time.valueOf(startAt));
    }

    public Long insertTheme(String name, String description, String thumbnailUrl) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, description, thumbnailUrl);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class, name);
    }

    public Long insertTheme(String name) {
        return insertTheme(name, "설명", "https://example.com/" + name + ".jpg");
    }

    public Long insertReservation(String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES (?, ?, ?, ?)",
                name, Date.valueOf(date), timeId, themeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?",
                Long.class, name, Date.valueOf(date), timeId, themeId);
    }

    public Long insertWaiting(String name, LocalDate date, Long timeId, Long themeId, int order) {
        jdbcTemplate.update(
                "INSERT INTO waiting (name, date, time_id, theme_id, order_index) VALUES (?, ?, ?, ?, ?)",
                name, Date.valueOf(date), timeId, themeId, order);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM waiting WHERE name = ? AND date = ? AND time_id = ? AND theme_id = ?",
                Long.class, name, Date.valueOf(date), timeId, themeId);
    }

    // ---------- then: 검증용 조회 ----------

    public String findReservationOwner(LocalDate date, Long timeId, Long themeId) {
        return jdbcTemplate.queryForObject(
                "SELECT name FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                String.class, Date.valueOf(date), timeId, themeId);
    }

    public int findReservationCount(LocalDate date, Long timeId, Long themeId) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reservation WHERE date = ? AND time_id = ? AND theme_id = ?",
                Integer.class, Date.valueOf(date), timeId, themeId);
    }

    public String findReservationStatus(Long reservationId) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = ?",
                String.class, reservationId);
    }

    public boolean existsWaiting(Long waitingId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM waiting WHERE id = ?",
                Integer.class, waitingId);
        return count != null && count > 0;
    }

    public int findWaitingOrder(Long waitingId) {
        return jdbcTemplate.queryForObject(
                "SELECT order_index FROM waiting WHERE id = ?",
                Integer.class, waitingId);
    }

    // ---------- given: 빌더 (관심 있는 필드만 명시, 나머진 기본값) ----------

    public ReservationInsertBuilder reservation(Long timeId, Long themeId) {
        return new ReservationInsertBuilder(this, timeId, themeId);
    }

    public WaitingInsertBuilder waiting(Long timeId, Long themeId) {
        return new WaitingInsertBuilder(this, timeId, themeId);
    }

    public static final class ReservationInsertBuilder {
        private final ReservationTestHelper helper;
        private final Long timeId;
        private final Long themeId;
        private String name = DEFAULT_NAME;
        private LocalDate date = DEFAULT_DATE;

        private ReservationInsertBuilder(ReservationTestHelper helper, Long timeId, Long themeId) {
            this.helper = helper;
            this.timeId = timeId;
            this.themeId = themeId;
        }

        public ReservationInsertBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ReservationInsertBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Long insert() {
            return helper.insertReservation(name, date, timeId, themeId);
        }
    }

    public static final class WaitingInsertBuilder {
        private final ReservationTestHelper helper;
        private final Long timeId;
        private final Long themeId;
        private String name = DEFAULT_NAME;
        private LocalDate date = DEFAULT_DATE;
        private int order = 1;

        private WaitingInsertBuilder(ReservationTestHelper helper, Long timeId, Long themeId) {
            this.helper = helper;
            this.timeId = timeId;
            this.themeId = themeId;
        }

        public WaitingInsertBuilder name(String name) {
            this.name = name;
            return this;
        }

        public WaitingInsertBuilder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public WaitingInsertBuilder order(int order) {
            this.order = order;
            return this;
        }

        public Long insert() {
            return helper.insertWaiting(name, date, timeId, themeId, order);
        }
    }

}
