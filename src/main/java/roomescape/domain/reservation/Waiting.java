package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.BaseEntity;
import roomescape.domain.member.Member;
import roomescape.infrastructure.error.exception.WaitingException;

@Entity
public class Waiting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    public Waiting(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }

    public Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    protected Waiting() {
    }

    public void validateWaitable(LocalDateTime currentDateTime) {
        LocalDateTime waitingDateTime = LocalDateTime.of(date, time.getStartAt());
        if (waitingDateTime.isBefore(currentDateTime)) {
            throw new WaitingException("예약일이 지나 대기 신청을 할 수 없습니다.");
        }
    }

    public boolean canBeApprovedBy(Member member) {
        return member.isAdmin();
    }

    public boolean canBeCanceledBy(Member member) {
        return isOwner(member) || member.isAdmin();
    }

    private boolean isOwner(Member member) {
        return this.member.equals(member);
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
}
