package roomescape.domain.reservation;

import jakarta.persistence.*;
import roomescape.domain.user.Member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private Member member;

    public Reservation() {
    }

    public Reservation(final Long id, final ReservationDate date, final ReservationTime time, final Theme theme, final Member member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public static Reservation from(final Long id, final String date, final ReservationTime time, final Theme theme, final Member member) {
        return new Reservation(id, ReservationDate.from(date), time, theme, member);
    }

    public Long getId() {
        return id;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    public String getLocalDateTimeFormat() {
        return parseLocalDateTime().toString();
    }

    public boolean isBefore(final LocalDate localDate, final LocalTime localTime) {
        return parseLocalDateTime().isBefore(LocalDateTime.of(localDate, localTime));
    }

    public boolean isEqualMember(final long memberId) {
        return this.member.isEqualId(memberId);
    }

    public boolean isEqualTheme(final long themeId) {
        return this.theme.isEqualId(themeId);
    }

    public LocalDateTime parseLocalDateTime() {
        return LocalDateTime.of(date.date(), this.time.getStartAt());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Reservation that = (Reservation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
