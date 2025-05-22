package roomescape.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "WAITING")
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATE")
    private LocalDate date;

    @ManyToOne
    @JoinColumn(name = "MEMBER_ID")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "TIME_ID")
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "THEME_ID")
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private Status status;

    public Waiting() {
    }

    public Waiting(
            Member member,
            LocalDate date,
            ReservationTime time,
            Theme theme
    ) {
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = Status.WAITING;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
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

    public Status getStatus() {
        return status;
    }

    public String getThemeName() {
        return theme.getName();
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }

    public String getStatusText() {
        return status.getText();
    }

}
