package roomescape.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "waiting_id")
    private Waiting waiting;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;

    private Reservation(
            Long id,
            Member member,
            Theme theme,
            LocalDate date,
            ReservationTime time,
            Waiting waiting
    ) {
        this.id = id;
        this.member = member;
        this.theme = theme;
        this.date = date;
        this.time = time;
        this.waiting = waiting;
        addReservationInTime();
    }

    private void addReservationInTime() {
        this.time.addReservation(this);
    }

    protected Reservation() {
    }

    public static Reservation of(
            Long id,
            Member member,
            Theme theme,
            LocalDate date,
            ReservationTime time,
            Waiting waiting
    ) {
        return new Reservation(id, member, theme, date, time, waiting);
    }

    public static Reservation withoutId(
            Member member,
            Theme theme,
            LocalDate reservationDate,
            ReservationTime reservationTime,
            Waiting waiting
    ) {
        return new Reservation(null, member, theme, reservationDate, reservationTime, waiting);
    }

    public boolean isPast(LocalDateTime comparedDateTime) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        return reservationDateTime.isBefore(comparedDateTime);
    }

    public void deleteSelf() {
        if (this.time != null) {
            this.time.removeReservation(this);
        }
    }

    public boolean isDuplicated(Reservation other) {
        return this.date.equals(other.date)
                && this.time.compareEqualId(other.time)
                && this.theme.compareEqualId(other.theme);
    }

    public boolean isAlreadyBookedTime(LocalDate date, Long themeId, Long timeId) {
        return this.date.equals(date)
                && this.theme.getId().equals(themeId)
                && this.time.getId().equals(timeId);
    }

    public boolean isWaiting() {
        return this.waiting.getStatus() == ReservationStatus.WAITING;
    }

    public void reserve() {
        this.waiting.setStatus(ReservationStatus.RESERVED);
    }

    public void cancel() {
        this.waiting.setStatus(ReservationStatus.CANCELED);
    }

    public void setTime(ReservationTime time) {
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Theme getTheme() {
        return theme;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Waiting getWaiting() {
        return waiting;
    }
}
