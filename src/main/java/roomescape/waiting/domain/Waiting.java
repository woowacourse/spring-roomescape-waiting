package roomescape.waiting.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@Table(
        indexes = {
                @Index(name = "idx_reservation_date_time_theme", columnList = "reservation_date, reservation_time_id, theme_id")
        }
)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private ReservationDate reservationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    protected Waiting() {
    }

    public Waiting(Long id, ReservationDate reservationDate, ReservationTime reservationTime, Theme theme,
                   Member member) {
        this.id = id;
        this.reservationDate = reservationDate;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
    }

    public static Waiting create(
            ReservationDate reservationDate,
            ReservationTime reservationTime,
            Theme theme,
            Member member
    ) {
        return new Waiting(null, reservationDate, reservationTime, theme, member);
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return reservationDate.getDate();
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

}
