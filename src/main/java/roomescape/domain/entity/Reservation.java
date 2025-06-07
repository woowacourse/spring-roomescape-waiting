package roomescape.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import roomescape.domain.ReservationStatus;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_schedule_id", nullable = false)
    private GameSchedule gameSchedule;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private Reservation(
            Long id,
            Member member,
            GameSchedule gameSchedule,
            ReservationStatus status
    ) {
        validateNotWaiting(status);

        this.id = id;
        this.member = Objects.requireNonNull(member, "예약 신청자가 필요합니다.");
        this.gameSchedule = Objects.requireNonNull(gameSchedule, "예약 스케줄이 필요합니다.");
        this.status = Objects.requireNonNull(status, "예약 상태가 필요합니다.");
    }

    protected Reservation() {
    }

    public static Reservation withId(
            Long id,
            Member member,
            GameSchedule gameSchedule,
            ReservationStatus status
    ) {
        if (id == null) {
            throw new IllegalArgumentException("id를 입력해주세요.");
        }

        return new Reservation(id, member, gameSchedule, status);
    }

    public static Reservation withoutId(Member member, GameSchedule gameSchedule) {
        return new Reservation(null, member, gameSchedule, ReservationStatus.RESERVED);
    }

    private void validateNotWaiting(ReservationStatus status) {
        if (status == ReservationStatus.WAITING) {
            throw new IllegalStateException("예약은 예약대기 상태일 수 없습니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public GameSchedule getGameSchedule() {
        return gameSchedule;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
