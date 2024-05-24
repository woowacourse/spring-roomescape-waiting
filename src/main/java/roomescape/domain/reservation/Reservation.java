package roomescape.domain.reservation;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.slot.ReservationSlot;
import roomescape.exception.RoomEscapeBusinessException;

@Entity
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE reservation SET is_deleted = true where id = ?")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Embedded
    private ReservationSlot slot;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Waiting> waitings = new ArrayList<>();

    private boolean isDeleted = false;

    public Reservation(Member member, ReservationSlot slot) {
        this(null, member, slot);
    }

    public Reservation(Long id, Member member, ReservationSlot slot) {
        this.id = id;
        this.member = member;
        this.slot = slot;
    }

    protected Reservation() {
    }

    public Waiting addWaiting(Member member) {
        validateDuplicated(member);

        Waiting waiting = new Waiting(member, this);
        waitings.add(waiting);

        return waiting;
    }

    private void validateDuplicated(Member member) {
        boolean isDuplicated = waitings.stream()
                .anyMatch(waiting -> waiting.isMember(member));

        if (this.member.equals(member) || isDuplicated) {
            throw new RoomEscapeBusinessException("중복된 예약을 할 수 없습니다.");
        }
    }

    public void approveWaiting() {
        if (hasNotWaiting()) {
            throw new RoomEscapeBusinessException("예약 대기자가 없습니다.");
        }

        Waiting waiting = waitings.remove(0);
        this.member = waiting.getMember();
    }

    public boolean hasNotWaiting() {
        return waitings.isEmpty();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationSlot getSlot() {
        return slot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reservation that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
