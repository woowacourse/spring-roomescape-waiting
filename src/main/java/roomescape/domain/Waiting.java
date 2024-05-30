package roomescape.domain;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private TimeSlot time;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false)
    private Theme theme;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    protected Waiting() {
    }

    public Waiting(Long id, Member member, LocalDate date, TimeSlot time, Theme theme, ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
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

    public TimeSlot getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}

