package roomescape.reservation.domain;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.exception.resource.ResourceLimitExceededException;
import roomescape.theme.domain.Theme;

@Entity
@Table(name = "reservation_slots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ReservationSlot {

    private static final int MAX_WAITING_COUNT = 20;

    @OneToMany(mappedBy = "reservationSlot", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private final List<Reservation> allReservations = new ArrayList<>();

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_reservation_id")
    private Reservation confirmedReservation;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;


    public ReservationSlot(final Long id, final LocalDate date, final ReservationTime time, final Theme theme) {
        validateDate(date);
        validateTime(time);
        validateFutureReservation(date, time);
        validateTheme(theme);
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public ReservationSlot(final LocalDate date, final ReservationTime time, final Theme theme) {
        this(null, date, time, theme);
    }

    private static void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null이면 안됩니다.");
        }
    }

    private static void validateFutureReservation(final LocalDate date, final ReservationTime time) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new IllegalArgumentException("예약 시간은 현재 시간보다 이후여야 합니다.");
        }
    }

    private static void validateTime(final ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("시간은 null이면 안됩니다.");
        }
    }

    private static void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마는 null이면 안됩니다.");
        }
    }

    public List<Reservation> getWaitingReservations() {
        return allReservations.stream().filter(reservation -> !Objects.equals(reservation, confirmedReservation))
                .toList();
    }

    public void addReservation(final Reservation reservation) {
        if (allReservations.size() >= MAX_WAITING_COUNT) {
            throw new ResourceLimitExceededException("최대 예약 대기 개수를 초과했습니다.");
        }
        allReservations.add(reservation);
    }

    public void assignConfirmedIfEmpty() {
        if (this.confirmedReservation == null) {
            confirmedReservation = getFirstRankWaiting();
        }
    }

    public boolean shouldBeDeleted() {
        return this.confirmedReservation == null && this.allReservations.isEmpty();
    }

    private Reservation getFirstRankWaiting() {
        return allReservations.stream().min(Comparator.comparing(Reservation::getId))
                .orElseThrow(() -> new IllegalStateException("예약 데이터가 존재하지 않습니다."));
    }

    public void removeReservation(final Reservation reservation) {
        if (!allReservations.contains(reservation)) {
            return;
        }
        if (confirmedReservation == reservation) {
            confirmedReservation = null;
        }
        allReservations.remove(reservation);
    }
}
