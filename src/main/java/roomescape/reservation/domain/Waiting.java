package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@Entity
@Table(name = "waitings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"id"})
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Waiting(
            final Long id,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member,
            final LocalDateTime createdAt
    ) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateMember(member);
        validateCreatedAt(createdAt);

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.createdAt = createdAt;
    }

    public Waiting(
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member,
            final LocalDateTime createdAt
    ) {
        this(null, date, time, theme, member, createdAt);
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null이면 안됩니다.");
        }
    }

    private void validateTime(final ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("시간은 null이면 안됩니다.");
        }
    }

    private void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마는 null이면 안됩니다.");
        }
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("멤버는 null이면 안됩니다.");
        }
    }

    private void validateCreatedAt(final LocalDateTime createdAt) {
        if (createdAt == null) {
            throw new IllegalArgumentException("생성 시간이 null이면 안됩니다.");
        }
    }
}
