package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import roomescape.exception.business.BusinessException;
import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class Reservation {

    private final Long id;
    private final Member member;
    private final LocalDate date;
    private final ReservationTime time;
    private final Theme theme;

    private Reservation(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        if (member == null) {
            throw new IllegalArgumentException("예약자는 필수입니다.");
        }
        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수입니다.");
        }
        if (time == null) {
            throw new IllegalArgumentException("예약 시간은 필수입니다.");
        }
        if (theme == null) {
            throw new IllegalArgumentException("테마는 필수입니다.");
        }
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation of(Member member, LocalDate date, ReservationTime time, Theme theme) {
        Reservation reservation = new Reservation(null, member, date, time, theme);
        if (reservation.isPast()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 지난 시간에는 예약할 수 없습니다.");
        }
        return reservation;
    }

    public static Reservation restore(Long id, Member member, LocalDate date, ReservationTime time, Theme theme) {
        return new Reservation(id, member, date, time, theme);
    }

    public Reservation reschedule(LocalDate date, ReservationTime time) {
        Reservation changed = new Reservation(id, this.member, date, time, this.theme);
        if (changed.isPast()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미 지난 시간으로 변경할 수 없습니다.");
        }
        return changed;
    }

    public boolean isPast() {
        return LocalDateTime.of(date, time.getStartAt()).isBefore(LocalDateTime.now());
    }

    public boolean isOwnedBy(Long memberId) {
        return this.member.getId().equals(memberId);
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

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }
}
