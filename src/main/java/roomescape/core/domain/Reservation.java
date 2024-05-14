package roomescape.core.domain;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Reservation {
    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

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
}
