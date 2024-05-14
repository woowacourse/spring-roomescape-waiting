package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Reservation {

    @Id
    private Long id;
    @ManyToOne
    private Member member;
    private LocalDate date;
    @ManyToOne
    private TimeSlot time;
    @ManyToOne
    private Theme theme;

    public Reservation() {
    }

    public Reservation(final Long id, final Member member, final LocalDate date, final TimeSlot time,
                       final Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(final Member member, final LocalDate date, final TimeSlot time, final Theme theme) {
        this(null, member, date, time, theme);
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return date;
    }

    public TimeSlot getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Long getId() {
        return id;
    }

    public Reservation with(final Long id) {
        return new Reservation(id, member, date, time, theme);
    }
}
