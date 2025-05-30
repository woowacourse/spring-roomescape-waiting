package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    @Embedded
    private Schedule schedule;

    private LocalDateTime createdAt;

    @PrePersist
    private void initializeCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }

    protected Waiting() {
    }

    public Waiting(Long id, Member member, Schedule schedule) {
        this.id = id;
        this.member = member;
        this.schedule = schedule;
    }

    public static Waiting createNew(Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Waiting(null, member, new Schedule(date, time, theme));
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public LocalDate getDate() {
        return schedule.getDate();
    }

    public ReservationTime getTime() {
        return schedule.getTime();
    }

    public Theme getTheme() {
        return schedule.getTheme();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Waiting waiting)) return false;
        return Objects.equals(id, waiting.id) && Objects.equals(member, waiting.member) && Objects.equals(schedule, waiting.schedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, member, schedule);
    }
}