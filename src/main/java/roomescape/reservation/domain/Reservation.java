package roomescape.reservation.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.reservation.exception.ReservationExceptionCode;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    public Reservation() {
    }

    public Reservation(long id, Date date, ReservationTime time, Theme theme, Member member) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    private Reservation(LocalDate date, long timeId, long themeId, long memberId) {
        this(0, Date.saveDateFrom(date), new ReservationTime(timeId), Theme.saveThemeFrom(themeId),
                Member.saveMemberFrom(memberId));
    }

    public static Reservation of(long id, LocalDate date, ReservationTime time, Theme theme, Member member) {
        return new Reservation(id, Date.dateFrom(date), time, theme, member);
    }

    public static Reservation of(LocalDate date, long timeId, long themeId, long memberId) {
        return new Reservation(date, timeId, themeId, memberId);
    }

    public static Reservation of(LocalDate date, ReservationTime time, Theme theme, Member member) {
        validateAtSaveDateAndTime(date, time);
        return new Reservation(0, Date.dateFrom(date), time, theme, member);
    }

    public long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date.getDate();
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    public void setIdOnSave(long id) {
        this.id = id;
    }

    private static void validateAtSaveDateAndTime(LocalDate date, ReservationTime time) {
        if (date.equals(LocalDate.now())) {
            validateTime(time);
        }
    }

    private static void validateTime(ReservationTime time) {
        if (time.isBeforeTime(LocalTime.now())) {
            throw new RoomEscapeException(ReservationExceptionCode.RESERVATION_TIME_IS_PAST_EXCEPTION);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
