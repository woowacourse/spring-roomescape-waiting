package roomescape.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.global.ReservationStatus;

@Entity
public class Waiting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne
    @JoinColumn(nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Theme theme;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    public Waiting(Long id, Member member, LocalDate date, ReservationTime time, Theme theme,
                   ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }
    public Waiting(Member member, LocalDate date, ReservationTime time, Theme theme,
                   ReservationStatus status) {
        this(null, member, date, time, theme, status);
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

    public ReservationStatus getStatus() {
        return status;
    }

    public String getName(){
        return member.getName();
    }

    public LocalTime getStartAt(){
        return time.getStartAt();
    }

    public String getThemeName(){
        return theme.getName();
    }
}
