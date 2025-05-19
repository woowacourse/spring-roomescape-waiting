package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Theme theme;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme,
                       ReservationStatus status) {
        validateMember(member);
        validateDate(date);
        validateReservationTime(time);
        validateTheme(theme);

        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
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

    public ReservationStatus getStatus() {
        return status;
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
