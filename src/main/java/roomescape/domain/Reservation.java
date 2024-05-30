package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.exception.reservation.InvalidDateTimeReservationException;
import roomescape.service.dto.request.ReservationRequest;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    @ManyToOne
    private Member member;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;

    protected Reservation() {
    }

    public Reservation(Long id, LocalDate date, Member member, ReservationTime time, Theme theme) {
        validate(date.atTime(time.getStartAt()));
        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(LocalDate date, Member member, ReservationTime time, Theme theme) {
        this(null, date, member, time, theme);
    }


    private void validate(LocalDateTime localDateTime) {
        if (localDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidDateTimeReservationException();
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public Member getMember() {
        return member;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
