package roomescape.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import roomescape.model.member.Member;
import roomescape.model.theme.Theme;
import roomescape.service.dto.ReservationDto;

import java.time.LocalDate;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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

    private Waiting(Long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Waiting(LocalDate date, ReservationTime time, Theme theme, Member member) {
        this(0L, date, time, theme, member);
    }

    public static Waiting of(ReservationDto reservationDto, ReservationTime time, Theme theme, Member member) {
        return new Waiting(0L, reservationDto.getDate(), time, theme, member);
    }

    protected Waiting() {
    }

    public Long getId() {
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
}
