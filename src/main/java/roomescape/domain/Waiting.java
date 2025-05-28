package roomescape.domain;

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
import java.util.Objects;

@Entity
public class Waiting extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    protected Waiting() {

    }

    public Waiting(Long id, LocalDate date, Theme theme, ReservationTime time, Member member) {
        validate(date, theme, time, member);
        this.id = id;
        this.date = date;
        this.theme = theme;
        this.time = time;
        this.member = member;
    }

    public static Waiting createWithoutId(LocalDate date, Theme theme, ReservationTime time, Member member) {
        return new Waiting(null, date, theme, time, member);
    }

    public boolean isPastWaiting() {
        LocalDateTime dateTime = LocalDateTime.of(date, time.getStartAt());
        return dateTime.isBefore(LocalDateTime.now());
    }

    public boolean isEqualMember(long memberId) {
        return getMember().isEqualMemberId(memberId);
    }

    public Long getId() {
        return id;
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

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Waiting waiting = (Waiting) o;
        if (this.id == null || waiting.id == null) {
            return false;
        }
        return Objects.equals(id, waiting.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    private void validate(LocalDate date, Theme theme, ReservationTime time, Member member) {
        validateDate(date);
        validateTheme(theme);
        validateTime(time);
        validateMember(member);
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("비어있는 날짜로 대기를 생성할 수 없습니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("비어있는 테마로 대기를 생성할 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("비어있는 시간으로 대기를 생성할 수 없습니다.");
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("비어있는 회원으로 대기를 생성할 수 없습니다.");
        }
    }
}
