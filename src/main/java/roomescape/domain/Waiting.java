package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Embedded
    private BookingSlot bookingSlot;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime waitingStartedAt;

    protected Waiting() {
    }

    private Waiting(Long id, Member member, BookingSlot bookingSlot) {
        this.id = id;
        this.member = member;
        this.bookingSlot = bookingSlot;
    }

    public static Waiting create(Member member, BookingSlot bookingSlot) {
        return new Waiting(null, member, bookingSlot);
    }

    public Reservation confirm() {
        return Reservation.create(this.member, this.bookingSlot);
    }

    public boolean isOwnedBy(Long memberId) {
        return getMember().getId().equals(memberId);
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
