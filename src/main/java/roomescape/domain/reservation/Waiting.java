package roomescape.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import roomescape.domain.member.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private ThemeSchedule themeSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    protected Waiting() {
    }

    public Waiting(Long id, LocalDateTime startedAt, ThemeSchedule themeSchedule, Member member) {
        this.id = id;
        this.startedAt = startedAt;
        this.themeSchedule = themeSchedule;
        this.member = member;
    }

    public static Waiting create(LocalDateTime startedAt, ThemeSchedule themeSchedule, Member member) {
        return new Waiting(null, startedAt, themeSchedule, member);
    }
}
