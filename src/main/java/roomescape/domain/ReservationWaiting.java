package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class ReservationWaiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Embedded
    @AttributeOverride(name = "date", column = @Column(nullable = false))
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private ReservationWaitingStatus status;

    public ReservationWaiting() {
    }

    public ReservationWaiting(LocalDateTime now, Member member, ReservationDate date, ReservationTime time, Theme theme) {
        this(null, now, member, date, time, theme, ReservationWaitingStatus.ALLOWED);
    }

    public ReservationWaiting(LocalDateTime now, Member member, ReservationDate date, ReservationTime time, Theme theme, ReservationWaitingStatus status) {
        this(null, now, member, date, time, theme, status);
    }

    public ReservationWaiting(Long id, LocalDateTime now, Member member, ReservationDate date, ReservationTime time, Theme theme, ReservationWaitingStatus status) {
        validateDateTime(now, date, time);
        this.member = member;
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    private static void validateDateTime(final LocalDateTime now, final ReservationDate date, final ReservationTime time) {
        LocalDateTime waitingDateTime = LocalDateTime.of(date.getDate(), time.getStartAt());
        if (waitingDateTime.isBefore(now)) {
            throw new IllegalArgumentException(String.format("지나간 시간에 대한 에약 대기는 생성할 수 없습니다. (dateTime: %s)", waitingDateTime));
        }
    }

    public boolean hasSameMemberWith(Reservation reservation) {
        Member reservationMember = reservation.getMember();
        return member.equals(reservationMember);
    }

    public boolean hasMemberId(Long memberId) {
        return memberId.equals(member.getId());
    }

    public boolean isAllowed() {
        return ReservationWaitingStatus.ALLOWED == status;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationWaitingStatus getStatus() {
        return status;
    }

    public void setStatus(final ReservationWaitingStatus status) {
        this.status = status;
    }
}
