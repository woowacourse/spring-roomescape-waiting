package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Embedded
    @Column(nullable = false)
    private ReservationDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Theme theme;

    public Reservation(Long id,
                       Member member,
                       ReservationDate date,
                       ReservationTime time,
                       Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this(id, member, new ReservationDate(date), time, theme);
    }

    public Reservation(Long id, Reservation reservation) {
        this(
                id,
                reservation.getMember(),
                new ReservationDate(reservation.getDate()),
                reservation.getTime(),
                reservation.getTheme()
        );
    }

    protected Reservation() {
    }

    public boolean isPast() {
        return date.isPast();
    }

    public boolean isReservedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    public boolean isWaitingRankLowerThan(Reservation other) {
        return this.id > other.id;
    }

    public boolean isWaitingOrderHigherThan(Reservation other) {
        return this.id < other.id;
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

    public LocalDate getDate() {
        return date.getValue();
    }

    public ReservationTime getTime() {
        return time;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public String getThemeName() {
        return theme.getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member)
                && Objects.equals(date, that.date) && Objects.equals(time, that.time)
                && Objects.equals(theme, that.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme);
    }
}
