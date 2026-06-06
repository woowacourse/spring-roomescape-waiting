package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.ReserverName;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;

public class Reservation {
    private final Long id;
    private final String name;
    private final LocalDate date;
    private final Theme theme;
    private final ReservationTime time;

    private Reservation(final Long id, final String name, final LocalDate date, final Theme theme, final ReservationTime time) {
        validate(date, theme, time);
        this.id = id;
        this.name = ReserverName.from(name).value();
        this.date = date;
        this.theme = theme;
        this.time = time;
    }

    public static Reservation createNew(final String name, final LocalDate date, final Theme theme, final ReservationTime time) {
        return new Reservation(null, name, date, theme, time);
    }

    public static Reservation of(final Long id, final String name, final LocalDate date, final Theme theme, final ReservationTime time) {
        validateId(id);
        return new Reservation(id, name, date, theme, time);
    }

    public Reservation withId(final Long id) {
        validateId(id);
        return new Reservation(id, this.name, this.date, this.theme, this.time);
    }

    public Reservation withDateAndTime(final LocalDate date, final ReservationTime time) {
        return new Reservation(this.id, this.name, date, this.theme, time);
    }

    public Reservation withName(final String name) {
        return new Reservation(this.id, name, this.date, this.theme, this.time);
    }

    public boolean hasName(final String name) {
        return this.name.equals(name);
    }

    public boolean isPastAt(final LocalDateTime standardDateTime) {
        return getReservationDateTime().isBefore(standardDateTime);
    }

    private static void validateId(final Long id){
        if(id == null) {
            throw new IllegalArgumentException("Id는 비어있을 수 없습니다.");
        }
    }

    private void validate(final LocalDate date, final Theme theme, final ReservationTime time) {
        if(date == null) {
            throw new IllegalArgumentException("날짜는 비어있을 수 없습니다.");
        }

        if(theme == null) {
            throw new IllegalArgumentException("테마는 비어있으면 안됩니다.");
        }

        if(time == null) {
            throw new IllegalArgumentException("시간은 비어있으면 안됩니다.");
        }
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Reservation)) {
            return false;
        }
        Reservation r = (Reservation) o;
        return Objects.equals(id, r.getId());
    }

    @Override
    public int hashCode(){
        return Objects.hash(id);
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public Theme getTheme() {
        return this.theme;
    }

    public ReservationTime getTime() {
        return this.time;
    }

    private LocalDateTime getReservationDateTime() {
        return LocalDateTime.of(date, time.getStartAt());
    }
}
