package roomescape.business.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "waiting")
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime reservationTime;

    @Column(nullable = false)
    private LocalDate reservationDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Waiting() {

    }

    public Waiting(final Member member, final Theme theme, final ReservationTime reservationTime,
                   final LocalDate reservationDate) {
        this.member = member;
        this.theme = theme;
        this.reservationTime = reservationTime;
        this.reservationDate = reservationDate;
        this.createdAt = LocalDateTime.now();
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

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
