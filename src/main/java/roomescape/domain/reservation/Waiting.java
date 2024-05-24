package roomescape.domain.reservation;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import roomescape.domain.user.Member;

@Entity
public class Waiting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ReservationDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @ManyToOne
    private Member member;

    private LocalTime createdAt;

    public Waiting() {
    }

    public Waiting(Long id, ReservationDate date, ReservationTime time, Theme theme, Member member,
                   LocalTime createdAt) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.createdAt = createdAt;
    }

    public boolean isBefore(LocalDate localDate, LocalTime localTime) {
        return LocalDateTime.of(date.date(), this.time.getStartAt())
                .isBefore(LocalDateTime.of(localDate, localTime));
    }

    public String getLocalDateTimeFormat() {
        return LocalDateTime.of(date.date(), this.time.getStartAt())
                .toString();
    }

    public long getId() {
        return id;
    }

    public ReservationDate getDate() {
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

    public LocalTime getCreatedAt() {
        return createdAt;
    }
}
