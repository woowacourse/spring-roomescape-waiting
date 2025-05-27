package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@Table(name = "reservation")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @ManyToOne
    @NotNull
    private Member member;

    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    @NotNull
    private LocalDate date;

    @JoinColumn(name = "time_id")
    @ManyToOne
    @NotNull
    private ReservationTime time;

    @JoinColumn(name = "theme_id")
    @ManyToOne
    @NotNull
    private Theme theme;

    protected Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        validateMember(member);
        validateDate(date);
        validateReservationTime(time);
        validateTheme(theme);

        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        validateMember(member);
        validateDate(date);
        validateReservationTime(time);
        validateTheme(theme);

        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }


    public ReservationStatus getStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationDateTime = LocalDateTime.of(this.date, this.time.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            return ReservationStatus.EXPIRED;
        }
        return ReservationStatus.RESERVED;
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new BadRequestException(ExceptionCause.MEMBER_EMPTY_INPUT);
        }
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new BadRequestException(ExceptionCause.RESERVATION_DATE_EMPTY_INPUT);
        }
    }

    private void validateReservationTime(ReservationTime reservationTime) {
        if (reservationTime == null) {
            throw new BadRequestException(ExceptionCause.RESERVATION_TIME_EMPTY_INPUT);
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new BadRequestException(ExceptionCause.THEME_EMPTY_INPUT);
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
