package roomescape.reservation.repository.fixture;

import static roomescape.reservation.repository.fixture.MemberFixture.MEMBER1;
import static roomescape.reservation.repository.fixture.MemberFixture.MEMBER2;
import static roomescape.reservation.repository.fixture.ReservationTimeFixture.TIME1;
import static roomescape.reservation.repository.fixture.ReservationTimeFixture.TIME2;
import static roomescape.reservation.repository.fixture.ReservationTimeFixture.TIME3;
import static roomescape.reservation.repository.fixture.ThemeFixture.THEME1;
import static roomescape.reservation.repository.fixture.ThemeFixture.THEME2;
import static roomescape.reservation.repository.fixture.ThemeFixture.THEME3;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

public enum ReservationFixture {

    RESERVATION1(1L, MEMBER1, "2024-12-12", TIME1, THEME1, Status.RESERVATION, "2024-12-01 00:00:00.000"),
    RESERVATION2(2L, MEMBER1, "2024-12-23", TIME2, THEME3, Status.RESERVATION, "2024-12-02 00:00:00.000"),
    RESERVATION3(3L, MEMBER2, "2024-12-25", TIME3, THEME2, Status.RESERVATION, "2024-12-03 00:00:00.000"),
    RESERVATION4(4L, MEMBER1, "2024-06-30", TIME1, THEME2, Status.RESERVATION, "2024-12-04 00:00:00.000"),
    RESERVATION5(5L, MEMBER2, "2024-06-30", TIME1, THEME2, Status.WAITING, "2024-12-05 00:00:00.000"),
    ;

    private final long id;
    private final MemberFixture memberFixture;
    private final String date;
    private final ReservationTimeFixture timeFixture;
    private final ThemeFixture themeFixture;
    private final Status status;
    private final String createdAt;

    ReservationFixture(long id,
                       MemberFixture memberFixture,
                       String date,
                       ReservationTimeFixture timeFixture,
                       ThemeFixture themeFixture,
                       Status status,
                       String createdAt
    ) {
        this.id = id;
        this.memberFixture = memberFixture;
        this.date = date;
        this.timeFixture = timeFixture;
        this.themeFixture = themeFixture;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static int count() {
        return values().length;
    }

    public Reservation create() {
        return new Reservation(
                id,
                memberFixture.create(),
                LocalDate.parse(date),
                timeFixture.create(),
                themeFixture.create(),
                status,
                LocalDateTime.parse(createdAt, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
        );
    }
}
