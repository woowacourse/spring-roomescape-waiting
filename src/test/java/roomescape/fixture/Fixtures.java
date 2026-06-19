package roomescape.fixture;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Password;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Store;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.command.CancelReservationCommand;
import roomescape.dto.command.CreateReservationCommand;
import roomescape.dto.command.UpdateReservationCommand;

public final class Fixtures {

    public static final long DEFAULT_STORE_ID = 1L;
    public static final long DEFAULT_AMOUNT = 10_000L;

    private Fixtures() {
    }

    public static Store store(String name) {
        return new Store(null, name);
    }

    public static Store storeWithId(long id, String name) {
        return new Store(id, name);
    }

    public static User member(String name) {
        return new User(name + "@test.com", Password.ofHashed("hash"), name, Role.MEMBER);
    }

    public static User manager(String name) {
        return new User(name + "@test.com", Password.ofHashed("hash"), name, Role.MANAGER);
    }

    public static User memberWithId(long id, String name) {
        return member(name).withId(id);
    }

    public static Theme theme(String name) {
        return new Theme(null, name, "설명", "https://thumbnail.url");
    }

    public static ReservationTime time(int hour) {
        return new ReservationTime(null, LocalTime.of(hour, 0));
    }

    public static Reservation reservation(User user, Theme theme, LocalDate date, ReservationTime time) {
        return new Reservation(null, user, theme, date, time, storeWithId(DEFAULT_STORE_ID, "매장"),
                ReservationStatus.RESERVED);
    }

    public static Reservation reservation(User user, Theme theme, LocalDate date, ReservationTime time, Store store,
                                          ReservationStatus status) {
        return new Reservation(null, user, theme, date, time, store, status);
    }

    public static Reservation reservation(User user, Theme theme, LocalDate date, ReservationTime time,
                                          ReservationStatus status) {
        return new Reservation(null, user, theme, date, time, storeWithId(DEFAULT_STORE_ID, "매장"), status);
    }

    public static Reservation sampleReservation(long id) {
        User user = memberWithId(1L, "브라운");
        Theme theme = new Theme(1L, "공포", "무서움", "https://thumbnail.url");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Store store = storeWithId(DEFAULT_STORE_ID, "매장");
        return new Reservation(id, user, theme, LocalDate.of(2026, 5, 6), time, store, ReservationStatus.RESERVED);
    }

    public static Reservation sampleWaitingReservation(long id) {
        User user = memberWithId(1L, "브라운");
        Theme theme = new Theme(1L, "공포", "무서움", "https://thumbnail.url");
        ReservationTime time = new ReservationTime(1L, LocalTime.of(10, 0));
        Store store = storeWithId(DEFAULT_STORE_ID, "매장");
        return new Reservation(id, user, theme, LocalDate.of(2026, 5, 1), time, store, ReservationStatus.WAITING);
    }

    public static Reservation reservationOf(long userId, long themeId, long timeId, long storeId, LocalDate date) {
        User user = memberWithId(userId, "브라운");
        Theme theme = new Theme(themeId, "테마", "설명", "https://thumbnail.url");
        ReservationTime time = new ReservationTime(timeId, LocalTime.of(10, 0));
        Store store = storeWithId(storeId, "매장");
        return new Reservation(null, user, theme, date, time, store, ReservationStatus.RESERVED);
    }

    public static CreateReservationCommand createCommand(long userId, long themeId, LocalDate date, long timeId) {
        return createCommand(userId, themeId, date, timeId, DEFAULT_AMOUNT);
    }

    public static CreateReservationCommand createCommand(long userId, long themeId, LocalDate date, long timeId,
                                                         long amount) {
        return new CreateReservationCommand(userId, themeId, date, timeId, DEFAULT_STORE_ID, amount);
    }

    public static UpdateReservationCommand updateCommand(
            long reservationId, long userId, long themeId, LocalDate date, long timeId) {
        return new UpdateReservationCommand(reservationId, userId, themeId, date, timeId);
    }

    public static CancelReservationCommand cancelCommand(long reservationId, long userId) {
        return new CancelReservationCommand(reservationId, userId);
    }
}
