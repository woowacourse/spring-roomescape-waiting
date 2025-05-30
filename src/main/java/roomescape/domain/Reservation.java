package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Embedded
    private Schedule schedule;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    public Reservation() {

    }

    private Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme, ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.schedule = new Schedule(date, time, theme);
        this.status = status;
    }

    public static Reservation createNew(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(null, member, date, time, theme, ReservationStatus.RESERVED);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public ReservationTime getTime() {
        return schedule.getTime();
    }

    public Theme getTheme() {
        return schedule.getTheme();
    }

    public ReservationStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Reservation that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(member, that.member) && Objects.equals(schedule, that.schedule) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, schedule, status);
    }
}
