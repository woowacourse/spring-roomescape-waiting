package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.time.ReservationTime;

public class Reservation {

    private final Long id;
    private final UserName userName;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    public Reservation(UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        this(null, userName, date, time, theme);
    }

    public Reservation(Long id, UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        validate(userName, date, time, theme);
        this.userName = userName;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validate(UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        Objects.requireNonNull(userName, "예약자 이름이 비어 있습니다.");
        Objects.requireNonNull(date, "예약 날짜가 비어 있습니다.");
        Objects.requireNonNull(time, "시간이 비어 있습니다.");
        Objects.requireNonNull(theme, "테마가 비어 있습니다.");
    }

    public void validateOwner(String name) {
        userName.validateOwner(name);
    }

    public Long getId() {
        return id;
    }

    public UserName getName() {
        return userName;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
