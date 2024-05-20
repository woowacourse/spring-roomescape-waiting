package roomescape.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ReservationDto;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    private LocalDate date;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private Reservation(long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Reservation(LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(0, date, time, theme, member);
    }

    public Reservation() {
    }

    public static Reservation of(ReservationDto reservationDto, ReservationTime time, long themeId, long memberId) {
        return new Reservation(reservationDto.getDate(), time,
                new Theme(themeId, null, null, null),
                new Member(memberId, null, null, null, null));
    }

    public long getId() {
        return id;
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

    public Member getMember() {
        return member;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id && Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(theme, that.theme) && Objects.equals(member, that.member);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, date, time, theme, member);
    }
}
