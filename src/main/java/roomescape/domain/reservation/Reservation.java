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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.slot.ReservationSlot;
import roomescape.exception.RoomEscapeBusinessException;

@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"date", "time_id", "theme_id"})})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Embedded
    private ReservationSlot slot;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.PERSIST)
    private List<Waiting> waitings = new ArrayList<>();

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

    public void updateMember(Member member) {
        this.member = member;
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
