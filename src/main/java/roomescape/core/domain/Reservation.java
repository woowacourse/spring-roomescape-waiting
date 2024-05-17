package roomescape.core.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public Reservation() {
    }

    public Reservation(final Member member, final String date, final ReservationTime time, final Theme theme) {
        this(null, member, date, time, theme);
    }

    public Reservation(final Long id, final Member member, final String date, final ReservationTime time,
                       final Theme theme) {
        this.id = id;
        this.member = member;
        this.date = parseDate(date);
        this.time = time;
        this.theme = theme;
        this.status = Status.BOOKED;
    }

    private LocalDate parseDate(final String date) {
        try {
            return LocalDate.parse(date);
        } catch (final DateTimeParseException e) {
            throw new IllegalArgumentException("날짜 형식이 잘못되었습니다.");
        }
    }

    public void validateDateAndTime() {
        if (isDatePast()) {
            throw new IllegalArgumentException("지난 날짜에는 예약할 수 없습니다.");
        }
        if (isDateToday() && time.isPast()) {
            throw new IllegalArgumentException("지난 시간에는 예약할 수 없습니다.");
        }
    }

    private boolean isDatePast() {
        final ZoneId kst = ZoneId.of("Asia/Seoul");
        return date.isBefore(LocalDate.now(kst));
    }

    private boolean isDateToday() {
        final ZoneId kst = ZoneId.of("Asia/Seoul");
        return date.isEqual(LocalDate.now(kst));
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

    public String getDateString() {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }
}
