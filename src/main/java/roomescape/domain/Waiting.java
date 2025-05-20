package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @Column(name = "waiting_order")
    private int order;

    protected Waiting() {
    }

    private Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme, int order) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.order = order;
    }

    public static Waiting createNew(Member member, LocalDate date, ReservationTime time, Theme theme, int waitingOrder) {
        return new Waiting(null, member, date, time, theme, waitingOrder);
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

    public int getOrder() {
        return order;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Waiting waiting)) return false;
        return order == waiting.order && Objects.equals(id, waiting.id) && Objects.equals(member, waiting.member) && Objects.equals(date, waiting.date) && Objects.equals(time, waiting.time) && Objects.equals(theme, waiting.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, date, time, theme, order);
    }
}
