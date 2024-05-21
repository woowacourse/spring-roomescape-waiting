package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    private TimeSlot time;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;
    @Enumerated(value = EnumType.STRING)
    private ReservationStatus status;

    protected Reservation() {
    }

    public Reservation(Long id, Member member, LocalDate date, TimeSlot time, Theme theme, ReservationStatus status) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.status = status;
    }

    public void validatePast(LocalDateTime now) {
        LocalDate today = LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth());
        if (date.isBefore(today) || date.isEqual(today) && time.isBefore(now)) {
            throw new IllegalArgumentException("지나간 날짜와 시간으로 예약할 수 없습니다");
        }
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
