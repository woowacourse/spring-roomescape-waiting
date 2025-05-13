package roomescape.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ReservationV2 {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final ReservationTheme theme;

    private ReservationV2(final long id, final Member member, final LocalDate date, final ReservationTime time,
                          final ReservationTheme theme) {
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public ReservationV2(final long id, final String date, final ReservationTime time,
                         final ReservationTheme theme, final Member member) {
        this(id, member, LocalDate.parse(date), time, theme);
    }

    public ReservationV2(final Member member, final LocalDate date, final ReservationTime time,
                         final ReservationTheme theme) {
        validateFutureDateTime(date, time.getStartAt());
        this.id = null;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public ReservationV2 toEntity(long id) {
        return new ReservationV2(id, member, date, time, theme);
    }

    public boolean isDuplicateReservation(ReservationV2 reservation) {
        return this.date.equals(reservation.date) && this.time.isSameTime(reservation.time);
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public ReservationTheme getTheme() {
        return theme;
    }

    public String getMemberName() {
        return member.getName();
    }

    public long getMemberId() {
        return member.getId();
    }

    private void validateFutureDateTime(final LocalDate date, final LocalTime time) {
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time);
        final LocalDateTime now = LocalDateTime.now();
        if ( reservationDateTime.isBefore(now) || reservationDateTime.isEqual(now)) {
            throw new IllegalArgumentException("[ERROR] 이전 시각으로 예약할 수 없습니다.");
        }
    }
}
