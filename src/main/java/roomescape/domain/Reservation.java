package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Embedded
    private BookingSlot bookingSlot;

    protected Reservation() {
    }

    private Reservation(Long id, Member member, BookingSlot bookingSlot) {
        this.id = id;
        this.member = member;
        this.bookingSlot = bookingSlot;
    }

    public static Reservation create(Member member, BookingSlot bookingSlot) {
        return new Reservation(null, member, bookingSlot);
    }

    public long calculateMinutesUntilStart(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime reservationDateTime = LocalDateTime.of(getDate(), getTime().getStartAt());
        return Duration.between(now, reservationDateTime).toMinutes();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return bookingSlot.getDate();
    }

    public ReservationTime getTime() {
        return bookingSlot.getTime();
    }

    public Theme getTheme() {
        return bookingSlot.getTheme();
    }

    public boolean isPast(Clock clock) {
        return bookingSlot.isPast(clock);
    }
}
