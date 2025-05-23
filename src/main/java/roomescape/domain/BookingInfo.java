package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;

@Embeddable
@Getter
public class BookingInfo {
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(nullable = false)
    private LocalDate date;

    protected BookingInfo() {
    }

    public BookingInfo(Member member, LocalDate date, ReservationTime time, Theme theme) {
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public boolean isPast(Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime bookingDateTime = LocalDateTime.of(date, time.getStartAt());
        return bookingDateTime.isBefore(now);
    }
} 
