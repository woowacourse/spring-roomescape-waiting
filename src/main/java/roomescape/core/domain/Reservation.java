package roomescape.core.domain;

import jakarta.persistence.Column;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;
    @Column(nullable = false)
    private LocalDateTime createAt;

    public Reservation() {
    }

    public Reservation(final Member member, final String date, final ReservationTime time, final Theme theme,
                       final Status status, final LocalDateTime createAt) {
        this(null, member, date, time, theme, status, createAt);
    }

    public Reservation(final Long id, final Member member, final String date, final ReservationTime time,
                       final Theme theme, final Status status, final LocalDateTime createAt) {
        this.id = id;
        this.member = member;
        this.date = parseDate(date);
        this.time = time;
        this.theme = theme;
        this.status = status;
        this.createAt = createAt;
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

    public void approve() {
        this.status = Status.BOOKED;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return member.getId();
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

    public Long getTimeId() {
        return time.getId();
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public Long getThemeId() {
        return theme.getId();
    }

    public Theme getTheme() {
        return theme;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }
}
