package roomescape.reservation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import roomescape.member.entity.Member;
import roomescape.theme.entity.Theme;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Waiting(LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(null, date, time, theme, member, null);
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    public LocalDateTime getDateTime() {
        return LocalDateTime.of(date, time.getStartAt());
    }
}
