package roomescape.reservation.waiting.domain;

import jakarta.persistence.*;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;

    public Waiting(Long id, LocalDateTime createdAt, Member member, LocalDate date, Theme theme, ReservationTime time) {
        validateNotNull(member, date, theme, time);
        this.id = id;
        this.createdAt = createdAt;
        this.member = member;
        this.date = date;
        this.theme = theme;
        this.time = time;
    }

    protected Waiting() {
    }

    public static Waiting register(final Member member, final LocalDate date, final Theme theme, final ReservationTime time) {
        return new Waiting(null, LocalDateTime.now(), member, date, theme, time);
    }

    private void validateNotNull(final Member member, final LocalDate date,
                                 final Theme theme, final ReservationTime time) {
        if (member == null) {
            throw new IllegalArgumentException("사용자를 입력해야 합니다.");
        }
        if (date == null) {
            throw new IllegalArgumentException("날짜를 입력해야 합니다.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("테마를 입력해야 합니다.");
        }
        if (time == null) {
            throw new IllegalArgumentException("시간을 입력해야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationTime getTime() {
        return time;
    }
}
