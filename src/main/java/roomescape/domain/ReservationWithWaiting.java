package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ReservationWithWaiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    private ReservationStatus status;

    private Long rank;

    public ReservationWithWaiting(final Member member, final LocalDate date, final ReservationTime time,
                                  final Theme theme, final ReservationStatus status, Long rank) {
        this.id = null;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.rank = rank;
    }

    public ReservationWithWaiting() {
    }

    public static List<ReservationWithWaiting> of(final List<Reservation> reservations, final List<WaitingWithRank> waitingWithRanks) {
        List<ReservationWithWaiting> reservationWithWaitings = new ArrayList<>();

        for (Reservation reservation : reservations) {
            ReservationWithWaiting reservationWithWaiting = new ReservationWithWaiting(
                    reservation.getMember(),
                    reservation.getDate(),
                    reservation.getTime(),
                    reservation.getTheme(),
                    ReservationStatus.RESERVATION,
                    0L);
            reservationWithWaitings.add(reservationWithWaiting);
        }

        for (WaitingWithRank waitingWithRank : waitingWithRanks) {
            ReservationWithWaiting reservationWithWaiting = new ReservationWithWaiting(
                    waitingWithRank.getWaiting().getMember(),
                    waitingWithRank.getWaiting().getDate(),
                    waitingWithRank.getWaiting().getTime(),
                    waitingWithRank.getWaiting().getTheme(),
                    ReservationStatus.WAITING,
                    waitingWithRank.getRank()
                    );
            reservationWithWaitings.add(reservationWithWaiting);
        }

        return reservationWithWaitings;
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

    public Long getRank() {
        return rank;
    }
}
