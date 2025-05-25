package roomescape.reservationslot.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.reservationslot.exception.InvalidReservationException;
import roomescape.reservationslot.exception.ReservationAlreadyExistsException;
import roomescape.reservationslot.exception.ReservationNotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.reservation.exception.SlotReservationNotFoundException;

@Entity
@Table(name = "reservation_slots")
public class ReservationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_slot_id")
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @OneToMany(mappedBy = "reservationSlot", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "waiting_id", nullable = false)
    private List<Reservation> reservations = new ArrayList<>();

    public ReservationSlot(final Member member, final LocalDate date, final ReservationTime time,
                           final Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        reservations.add(new Reservation(ReservationStatus.CURRENT, member, this));
    }

    protected ReservationSlot() {
    }

    public static ReservationSlot createUpcomingReservation(final Member member,
                                                            final LocalDate date,
                                                            final ReservationTime time,
                                                            final Theme theme, final LocalDateTime now) {
        validateDateTime(date, time.getStartAt(), now);
        return new ReservationSlot(member, date, time, theme);
    }

    private static void validateDateTime(LocalDate date, LocalTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new InvalidReservationException("예약 시간이 현재 시간보다 이전일 수 없습니다.");
        }
    }

    public Reservation addMemberToWaiting(final Member member) {
        boolean alreadyWaiting = this.reservations.stream()
                .anyMatch(waiting -> waiting.getMember().getId().equals(member.getId()));
        if (alreadyWaiting) {
            throw new ReservationAlreadyExistsException("이미 예약 대기중입니다.");
        }
        Reservation reservation = new Reservation(ReservationStatus.WAITING, member, this);
        this.reservations.add(reservation);
        return reservation;
    }

    public Member findReservedMember() {
        return reservations.stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .map(Reservation::getMember)
                .findFirst()
                .orElseThrow(() -> new ReservationNotFoundException("현재 예약한 멤버가 없습니다."));
    }

    public long findRank(final Reservation givenReservation) {
        if (!reservations.contains(givenReservation)) {
            throw new SlotReservationNotFoundException("해당 예약 대기를 찾을 수 없습니다.");
        }
        List<Reservation> sortedReservations = new ArrayList<>(reservations)
                .stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .toList();
        return sortedReservations.indexOf(givenReservation);
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final ReservationSlot that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId()) && Objects.equals(getDate(), that.getDate())
                && Objects.equals(getTime(), that.getTime()) && Objects.equals(getTheme(),
                that.getTheme()) && Objects.equals(reservations, that.reservations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDate(), getTime(), getTheme(), reservations);
    }

    public Long getId() {
        return id;
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
