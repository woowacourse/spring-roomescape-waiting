package roomescape.booking.waiting;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.Member;
import roomescape.schedule.Schedule;

import java.time.LocalDateTime;

@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Schedule schedule;

    private LocalDateTime createdAt;

    public Waiting(final Schedule schedule, final Member member, final LocalDateTime createdAt) {
        this.schedule = schedule;
        this.member = member;
        this.createdAt = createdAt;
    }
}
