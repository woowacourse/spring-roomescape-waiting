package roomescape.business.model.entity;

import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;

@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
public class Reservation {

    private final Id id;
    private final User user;
    private final ReservationDate date;
    private final ReservationTime time;
    private final Theme theme;

    public static Reservation create(final User user, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(Id.issue(), user, new ReservationDate(date), time, theme);
    }

    public static Reservation restore(final String id, final User user, final LocalDate date, final ReservationTime time, final Theme theme) {
        return new Reservation(Id.create(id), user, new ReservationDate(date), time, theme);
    }

    public boolean isSameReserver(final String userId) {
        return user.isSameUser(userId);
    }
}
