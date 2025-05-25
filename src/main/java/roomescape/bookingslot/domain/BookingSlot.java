package roomescape.bookingslot.domain;

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
import roomescape.bookingslot.exception.InvalidReservationException;
import roomescape.bookingslot.exception.ReservationAlreadyExistsException;
import roomescape.bookingslot.exception.ReservationNotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;
import roomescape.waiting.exception.WaitingNotFoundException;

@Entity
@Table(name = "booking_slots")
public class BookingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_slot_id")
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @OneToMany(mappedBy = "bookingSlot", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Column(name = "waiting_id", nullable = false)
    private List<Waiting> waitings = new ArrayList<>();

    public BookingSlot(final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        // TODO : 연관관계 저장 안됨 문제
        waitings.add(new Waiting(WaitingStatus.CURRENT, member, this));
    }

    protected BookingSlot() {
    }

    public static BookingSlot createUpcomingReservation(final Member member,
                                                        final LocalDate date,
                                                        final ReservationTime time,
                                                        final Theme theme, final LocalDateTime now) {
        validateDateTime(date, time.getStartAt(), now);
        return new BookingSlot(member, date, time, theme);
    }

    private static void validateDateTime(LocalDate date, LocalTime time, LocalDateTime now) {
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new InvalidReservationException("예약 시간이 현재 시간보다 이전일 수 없습니다.");
        }
    }

    public Waiting addMemberToWaiting(final Member member) {
        boolean alreadyWaiting = waitings.stream()
                .anyMatch(waiting -> waiting.getMember().getId().equals(member.getId()));
        if (alreadyWaiting) {
            throw new ReservationAlreadyExistsException("이미 예약 대기중입니다.");
        }
        Waiting waiting = new Waiting(WaitingStatus.WAITING, member, this);
        waitings.add(waiting);
        return waiting;
    }

    public Member findReservedMember() {
        return waitings.stream()
                .sorted(Comparator.comparing(Waiting::getCreatedAt))
                .map(Waiting::getMember)
                .findFirst()
                .orElseThrow(() -> new ReservationNotFoundException("현재 예약한 멤버가 없습니다."));
    }

    public long findRank(final Waiting givenWaiting) {
        if (!waitings.contains(givenWaiting)) {
            throw new WaitingNotFoundException("해당 예약 대기를 찾을 수 없습니다.");
        }
        List<Waiting> sortedWaitings = new ArrayList<>(waitings)
                .stream()
                .sorted(Comparator.comparing(Waiting::getCreatedAt))
                .toList();
        return sortedWaitings.indexOf(givenWaiting);
    }

    public void cancelWaiting(final Member member) {
        Waiting waitingToRemove = waitings.stream()
                .filter(waiting -> waiting.getMember().equals(member))
                .filter(waiting -> waiting.getWaitingStatus() == WaitingStatus.WAITING)
                .findFirst()
                .orElseThrow(() -> new ReservationNotFoundException("대기 정보를 찾을 수 없습니다."));

        waitings.remove(waitingToRemove);
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof final BookingSlot that)) {
            return false;
        }
        return Objects.equals(getId(), that.getId()) && Objects.equals(getDate(), that.getDate())
                && Objects.equals(getTime(), that.getTime()) && Objects.equals(getTheme(),
                that.getTheme()) && Objects.equals(waitings, that.waitings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getDate(), getTime(), getTheme(), waitings);
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
