package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.common.exception.ForbiddenException;
import roomescape.common.exception.UnprocessableEntityException;
import roomescape.domain.slot.theme.Theme;
import roomescape.domain.slot.time.ReservationTime;

public class Reservation {

    private final Long id;
    private final UserName userName;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;
    private ReservationStatus status;

    public Reservation(UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        this(null, userName, date, time, theme, ReservationStatus.PENDING);
    }

    public Reservation(Long id, UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        this(id, userName, date, time, theme, ReservationStatus.PENDING);
    }

    public Reservation(
            Long id,
            UserName userName,
            LocalDate date,
            ReservationTime time,
            Theme theme,
            ReservationStatus status
    ) {
        this.id = id;
        validate(userName, date, time, theme);
        this.userName = userName;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private void validate(UserName userName, LocalDate date, ReservationTime time, Theme theme) {
        Objects.requireNonNull(userName, "예약자 이름이 비어 있습니다.");
        Objects.requireNonNull(date, "예약 날짜가 비어 있습니다.");
        Objects.requireNonNull(time, "시간이 비어 있습니다.");
        Objects.requireNonNull(theme, "테마가 비어 있습니다.");
    }

    public void verifyBookable(LocalDateTime now) {
        LocalDate today = now.toLocalDate();

        if (date.isBefore(today)) {
            status = status.reject();
            throw new UnprocessableEntityException("과거 날짜로는 예약할 수 없습니다.");
        }

        if (date.isEqual(today) && time.isBefore(now.toLocalTime())) {
            status = status.reject();
            throw new UnprocessableEntityException("이미 지난 시간으로 예약할 수 없습니다.");
        }
    }

    public Reservation change(UserName userName, LocalDate newDate, ReservationTime newTime, LocalDateTime now) {
        validateOwner(userName, "다른 사람의 예약은 변경할 수 없습니다.");
        validatePast(newDate, newTime, now, "과거의 시간으로 예약을 변경할 수 없습니다.");

        return new Reservation(this.id, userName, newDate, newTime, this.theme, status.pending());
    }

    public void cancel(UserName userName, LocalDateTime now) {
        validateOwner(userName, "다른 사람의 예약은 취소할 수 없습니다.");
        validatePast(date, time, now, "이미 지난 예약은 취소할 수 없습니다.");

        status = status.cancel();
    }

    private void validateOwner(UserName userName, String message) {
        if (!userName.equals(this.userName)) {
            throw new ForbiddenException(message);
        }
    }

    private void validatePast(LocalDate date, ReservationTime time, LocalDateTime now, String message) {
        LocalDateTime requestDateTime = LocalDateTime.of(date, time.getStartAt());

        if (requestDateTime.isBefore(now)) {
            throw new UnprocessableEntityException(message);
        }
    }

    public Reservation confirm() {
        return new Reservation(id, userName, date, time, theme, status.confirm());
    }

    public Reservation reject() {
        return new Reservation(id, userName, date, time, theme, status.reject());
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
