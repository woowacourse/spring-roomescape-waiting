package roomescape.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    private int order;

    protected Waiting() {
    }

    private Waiting(Long id, Member member, ReservationTime time, Theme theme, int order) {
        this.id = id;
        this.member = member;
        this.time = time;
        this.theme = theme;
        this.order = order;
    }

    public static Waiting createNew(Member member, ReservationTime time, Theme theme, int waitingOrder) {
        return new Waiting(null, member, time, theme, waitingOrder);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
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
        return order == waiting.order && Objects.equals(id, waiting.id) && Objects.equals(member, waiting.member) && Objects.equals(time, waiting.time) && Objects.equals(theme, waiting.theme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, time, theme, order);
    }
}
