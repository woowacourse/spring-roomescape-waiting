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
import roomescape.domain.ReservationStatus;

@Entity
public class Waiting {

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

    protected Waiting() {
    }

    private Waiting(Long id, Member member, GameSchedule gameSchedule, ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.gameSchedule = gameSchedule;
        this.status = status;
    }

    public static Waiting withoutId(Member member, GameSchedule gameSchedule, ReservationStatus status) {
        return new Waiting(null, member, gameSchedule, status);
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
