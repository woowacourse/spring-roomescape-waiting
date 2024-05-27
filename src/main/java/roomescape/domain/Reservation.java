package roomescape.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import roomescape.domain.exception.PastReservationException;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "time_id", "theme_id"})
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Member member;
    @Embedded
    private ReservationDate date;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ReservationTime time;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Theme theme;

    protected Reservation() {
    }

    public Reservation(Member member, ReservationDate date, ReservationTime time, Theme theme) {
        this(null, member, date, time, theme);
    }

    public Reservation(Long id, Member member, ReservationDate date, ReservationTime time, Theme theme) {
        validateMember(member);
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        this.member = member;
        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    public static Reservation create(Member member, ReservationDate date, ReservationTime time, Theme theme) {
        Reservation newInstance = new Reservation(member, date, time, theme);
        validatePastReservation(newInstance);
        return newInstance;
    }

    private static void validatePastReservation(Reservation reservation) {
        if (reservation.isPast()) {
            throw new PastReservationException();
        }
    }

    private void validateMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("예약자는 비어있을 수 없습니다.");
        }
    }

    private void validateDate(ReservationDate date) {
        if (date == null) {
            throw new IllegalArgumentException("예약 날짜는 비어있을 수 없습니다.");
        }
    }

    private void validateTime(ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약 시간은 비어있을 수 없습니다.");
        }
    }

    private void validateTheme(Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("예약 테마는 비어있을 수 없습니다.");
        }
    }

    public boolean isPast() {
        return date.isBeforeNow() || date.isToday() && time.isBeforeNow();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public ReservationDate getReservationDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public void changeMember(Member member) {
        this.member = member;
    }
}
