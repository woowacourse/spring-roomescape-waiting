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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationDuplicatedException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationslot.exception.InvalidReservationSlotException;
import roomescape.reservationslot.exception.ReservationSlotNotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

@Entity
@Table(name = "reservation_slots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"time_id", "theme_id", "date"})
)
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
    private List<Reservation> reservations = new ArrayList<>();

    public ReservationSlot(final LocalDate date, final ReservationTime time, final Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    protected ReservationSlot() {
    }

    public Reservation addReservation(final Member member, final LocalDateTime now) {
        validateDateTime(date, time.getStartAt(), now);
        validateMemberReserved(member);
        Reservation reservation = new Reservation(member, this);
        reservations.add(reservation);
        return reservation;
    }

    public Member findConfirmedMember() {
        return reservations.stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .map(Reservation::getMember)
                .findFirst()
                .orElseThrow(() -> new ReservationSlotNotFoundException("현재 예약한 멤버가 없습니다."));
    }

    public long findRank(final Reservation reservation) {
        validateReservationExists(reservation);
        List<Reservation> sortedReservations = reservations
                .stream()
                .sorted(Comparator.comparing(Reservation::getCreatedAt))
                .toList();
        return sortedReservations.indexOf(reservation);
    }

    public Reservation findConfirmedReservation() {
        return reservations.stream()
                .min(Comparator.comparing(Reservation::getCreatedAt))
                .orElseThrow(() -> new ReservationSlotNotFoundException("현재 예약한 멤버가 없습니다."));
    }

    private void validateDateTime(LocalDate date, LocalTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new InvalidReservationSlotException("예약 시간이 현재 시간보다 이전일 수 없습니다.");
        }
    }

    private void validateMemberReserved(final Member member) {
        boolean memberExists = reservations.stream()
                .anyMatch(reservation -> reservation.getMember().getId().equals(member.getId()));
        if (memberExists) {
            throw new ReservationDuplicatedException("해당 멤버는 이미 예약 중입니다.");
        }
    }

    private void validateReservationExists(final Reservation reservation) {
        if (!reservations.contains(reservation)) {
            throw new ReservationNotFoundException("해당 예약을 찾을 수 없습니다.");
        }
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final ReservationSlot that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId());
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

    public List<Reservation> getReservations() {
        return reservations;
    }
}
