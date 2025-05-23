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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.InvalidReservationException;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Column(name = "waiting_id", nullable = false)
    private List<Waiting> waitings = new ArrayList<>();

    public Reservation(final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme) {
        this.date = date;
        this.time = time;
        this.theme = theme;
        waitings.add(new Waiting(WaitingStatus.CURRENT, member, this));
    }

    protected Reservation() {
    }

    public static Reservation createUpcomingReservation(final Member member,
                                                        final LocalDate date,
                                                        final ReservationTime time,
                                                        final Theme theme, final LocalDateTime now) {
        validateDateTime(date, time.getStartAt(), now);
        return new Reservation(member, date, time, theme);
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
        if (!(object instanceof final Reservation that)) {
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
