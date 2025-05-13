package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate reservationDate;

    @ManyToOne
    private ReservationTime reservationTime;
    @ManyToOne
    private Member member;
    @ManyToOne
    private Theme theme;

    // TODO: 호출 순서 변경
    private Reservation(
            Long id,
            Member member,
            Theme theme,
            LocalDate reservationDate,
            ReservationTime reservationTime
    ) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
    }

    public Reservation() {
    }

    public static Reservation of(Long id, Member member, Theme theme, LocalDate date, ReservationTime time) {
        return new Reservation(id, member, theme, date, time);
    }

    public static Reservation withoutId(Member member, Theme theme, LocalDate reservationDate,
                                        ReservationTime reservationTime) {
        return new Reservation(null, member, theme, reservationDate, reservationTime);
    }

    public static Reservation assignId(Long id, Reservation reservation) {
        return new Reservation(id, reservation.getMember(), reservation.getTheme(), reservation.getReservationDate(),
                reservation.getReservationTime());
    }

    public boolean isPast() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationDateTime = LocalDateTime.of(reservationDate, reservationTime.getStartAt());
        return reservationDateTime.isBefore(now);
    }

    public boolean isDuplicated(Reservation other) {
        return this.reservationDate.equals(other.reservationDate)
               && this.reservationTime.equals(other.reservationTime)
               && this.theme.equals(other.theme);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }
}
