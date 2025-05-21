package roomescape.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class Reservation {

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

    public Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public Reservation(Member member,
                       LocalDate date,
                       ReservationTime time,
                       Theme theme
    ) {
        this(null, member, date, time, theme);
    }


    public boolean isBefore(LocalDateTime compareDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, getStartAt());
        return reservationDateTime.isBefore(compareDateTime);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public String getName() {
        return member.getName();
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public LocalTime getStartAt() {
        return time.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public String getThemeName() {
        return theme.getName();
    }

}
