package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import roomescape.exception.BadRequestException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"theme_id", "time_id", "date"})
})
public class Reservation {
    private static final String status = "예약";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Theme theme;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Time time;
    @Column(nullable = false)
    private LocalDate date;

    public Reservation() {
    }

    public Reservation(Member member, Theme theme, Time time, LocalDate date) {
        this(null, member, theme, time, date);
    }

    public Reservation(Long id, Member member, Theme theme, Time time, LocalDate date) {
        validate(member, date, time, theme);
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.time = time;
        this.date = date;
    }

    private void validate(Member member, LocalDate date, Time time, Theme theme) {
        if (member == null || date == null || time == null || theme == null) {
            throw new BadRequestException("예약 정보가 부족합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getMemberName() {
        return member.getName();
    }

    public Long getMemberId() {
        return member.getId();
    }

    public LocalDate getDate() {
        return date;
    }

    public Time getTime() {
        return time;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public String getStatus() {
        return status;
    }

    public boolean isReservedAtPeriod(LocalDate start, LocalDate end) {
        return date.isAfter(start) && date.isBefore(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation that)) return false;

        if (id == null || that.id == null) {
            return Objects.equals(date, that.date) && Objects.equals(time, that.time)
                   && Objects.equals(theme, that.theme);
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        if (id == null) return Objects.hash(date, time, theme);
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Reservation{" +
               "id=" + id +
               ", member=" + member +
               ", theme=" + theme +
               ", time=" + time +
               ", date=" + date +
               '}';
    }
}
