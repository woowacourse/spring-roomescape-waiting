package roomescape.domain.reservation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.waiting.ReservationWaitingTicket;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.Theme;
import roomescape.exception.InvalidRequestException;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.REMOVE)
    private ReservationWaitingTicket reservationWaitingTicket;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

      public Reservation(
            final Long id,
            final Member member,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final ReservationStatus status
    ) {
        this.id = id;
        this.member = Objects.requireNonNull(member);
        this.date = Objects.requireNonNull(date, "예약 날짜는 반드시 입력해야 합니다. 예시) YYYY-MM-DD");
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
        this.status = Objects.requireNonNull(status);
    }

    public Reservation (Member member, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status) {
        this(null, member, date, time, theme, status);
    }

    protected Reservation() {
    }

    public void changeStatusToReserved() {
        this.status = ReservationStatus.RESERVED;
    }

    public boolean isReservationWaiting() {
        return status == ReservationStatus.WAITING;
    }

    public void validateReservableTime(final LocalDateTime now){
       LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        if (dateTime.isBefore(now)) {
            throw new InvalidRequestException("현 시점 이후의 날짜와 시간을 선택해주세요.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member) && Objects.equals(date, that.date)
                && Objects.equals(time, that.time) && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme);
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

    public ReservationWaitingTicket getReservationWaitingTicket() {
        return reservationWaitingTicket;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
