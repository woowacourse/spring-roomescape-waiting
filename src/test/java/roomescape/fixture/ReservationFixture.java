package roomescape.fixture;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

import java.time.LocalDate;

public class ReservationFixture {

    private Long id = 1L;
    private String name = "티뉴";
    private LocalDate date = LocalDate.of(2026, 8, 5);
    private ReservationTime time = ReservationTimeFixture.create();
    private Theme theme = ThemeFixture.create();
    private ReservationStatus status = ReservationStatus.CONFIRM;

    private ReservationFixture() {
    }

    public static ReservationFixture builder() {
        return new ReservationFixture();
    }

    public static Reservation create() {
        return builder().build();
    }

    public ReservationFixture id(Long id) {
        this.id = id;
        return this;
    }

    public ReservationFixture name(String name) {
        this.name = name;
        return this;
    }

    public ReservationFixture date(LocalDate date) {
        this.date = date;
        return this;
    }

    public Reservation build() {
        return new Reservation(
                id,
                name,
                date,
                time,
                theme,
                status
        );
    }
}
