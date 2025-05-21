package roomescape.entity;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
@Table(name = "RESERVATION_TIME")
public class ReservationTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "START_AT")
    private LocalTime startAt;

    @Column(name = "ALREADY_BOOKED")
    private Boolean alreadyBooked;

    public ReservationTime() {
    }

    public ReservationTime(LocalTime startAt, Boolean alreadyBooked) {
        this.startAt = startAt;
        this.alreadyBooked = alreadyBooked;
    }

    public boolean isBefore(LocalTime time) {
        return startAt.isBefore(time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public Boolean getAlreadyBooked() {
        return alreadyBooked;
    }
}
