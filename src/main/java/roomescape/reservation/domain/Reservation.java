package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
@Table(name = "reservation", uniqueConstraints = @UniqueConstraint(columnNames = {"date", "timeId", "themeId"}))
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @ManyToOne(optional = false)
    @JoinColumn(name = "time_id")
    private ReservationTime time;
    @ManyToOne(optional = false)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    public Reservation(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = null;
        this.member = Objects.requireNonNull(member);
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = Objects.requireNonNull(id);
        this.member = Objects.requireNonNull(member);
        this.date = Objects.requireNonNull(date);
        this.time = Objects.requireNonNull(time);
        this.theme = Objects.requireNonNull(theme);
    }

    protected Reservation() {
    }

    public boolean isBefore(LocalDateTime currentDateTime) {
        LocalDate currentDate = currentDateTime.toLocalDate();
        if (date.isBefore(currentDate)) {
            return true;
        }
        if (date.isAfter(currentDate)) {
            return false;
        }
        return time.isBefore(currentDateTime.toLocalTime());
    }

    public Long getMemberId() {
        return member.getId();
    }

    public Long getTimeId() {
        return time.getId();
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Reservation that = (Reservation) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
