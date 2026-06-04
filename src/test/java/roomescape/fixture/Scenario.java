package roomescape.fixture;

import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.acceptance.AuthTestSupport;
import roomescape.domain.Role;

/**
 * 의존 그래프(user·theme·time·reservation)를 소유하는 의미 기반 픽스처. 테스트는 "예약 하나가 존재한다", "사용 중인 시간" 같은 의도만 표현하고, 도메인 구조가 바뀌면 이 클래스만
 * 수정한다.
 */
public final class Scenario {

    private Scenario() {
    }

    public static ReservationBuilder reservation(JdbcTemplate jdbc) {
        return new ReservationBuilder(jdbc);
    }

    public static ReservationBuilder waitingReservation(JdbcTemplate jdbc) {
        return new ReservationBuilder(jdbc).status("WAITING");
    }

    public static BookableSlot bookableSlot(JdbcTemplate jdbc) {
        return bookableSlot(jdbc, "브라운");
    }

    public static BookableSlot bookableSlot(JdbcTemplate jdbc, String member) {
        long themeId = DbFixtures.insertTheme(jdbc, "테마");
        long timeId = DbFixtures.insertTime(jdbc, "10:00");
        long storeId = DbFixtures.insertStore(jdbc, "매장");
        long userId = DbFixtures.insertMember(jdbc, member);
        return new BookableSlot(themeId, timeId, storeId, member, bearer(userId, member));
    }

    public static long timeInUse(JdbcTemplate jdbc) {
        return reservation(jdbc).save().timeId();
    }

    public static long timeNotInUse(JdbcTemplate jdbc) {
        return DbFixtures.insertTime(jdbc, "10:00");
    }

    public static long themeWithReservations(JdbcTemplate jdbc, int count) {
        long themeId = DbFixtures.insertTheme(jdbc, "인기테마");
        long timeId = DbFixtures.insertTime(jdbc, "10:00");
        for (int i = 0; i < count; i++) {
            DbFixtures.insertReservation(jdbc, "예약자" + i, themeId, String.format("2026-05-%02d", i + 1), timeId);
        }
        return themeId;
    }

    private static String bearer(long userId, String member) {
        return AuthTestSupport.bearer(userId, member + "@test.com", Role.MEMBER);
    }

    public static long member(JdbcTemplate jdbc) {
        return DbFixtures.insertMember(jdbc, "샤를");
    }

    public record BookableSlot(long themeId, long timeId, long storeId, String member, String bearer) {
    }

    public record ExistingReservation(long reservationId, String date, long themeId, long timeId, long userId,
                                      long storeId,
                                      String status,
                                      String member, String bearer) {
    }

    public static final class ReservationBuilder {

        private final JdbcTemplate jdbc;
        private String member = "브라운";
        private String themeName = "테마";
        private String startAt = "10:00";
        private String date = "2026-05-08";
        private String storeName = "매장";
        private String status = "RESERVED";
        private Long existingStoreId;
        private Long existingThemeId;
        private Long existingTimeId;

        private ReservationBuilder(JdbcTemplate jdbc) {
            this.jdbc = jdbc;
        }

        public ReservationBuilder member(String member) {
            this.member = member;
            return this;
        }

        public ReservationBuilder date(String date) {
            this.date = date;
            return this;
        }

        public ReservationBuilder theme(String themeName) {
            this.themeName = themeName;
            return this;
        }

        public ReservationBuilder time(String startAt) {
            this.startAt = startAt;
            return this;
        }

        public ReservationBuilder onTheme(long themeId) {
            this.existingThemeId = themeId;
            return this;
        }

        public ReservationBuilder onTime(long timeId) {
            this.existingTimeId = timeId;
            return this;
        }

        public ReservationBuilder store(String storeName) {
            this.storeName = storeName;
            return this;
        }

        public ReservationBuilder onStore(long storeId) {
            this.existingStoreId = storeId;
            return this;
        }

        public ReservationBuilder status(String status) {
            this.status = status;
            return this;
        }

        public ExistingReservation save() {
            long themeId = (existingThemeId != null) ? existingThemeId : DbFixtures.insertTheme(jdbc, themeName);
            long timeId = (existingTimeId != null) ? existingTimeId : DbFixtures.insertTime(jdbc, startAt);
            long storeId = (existingStoreId != null) ? existingStoreId : DbFixtures.insertStore(jdbc, storeName);
            long userId = DbFixtures.insertMember(jdbc, member);
            long reservationId = DbFixtures.insertReservation(jdbc, userId, themeId, date, timeId, storeId, status);
            return new ExistingReservation(reservationId, date, themeId, timeId, userId, storeId, status, member,
                    bearer(userId, member));
        }
    }
}
