package roomescape.reservation.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import roomescape.member.model.Member;

import java.time.LocalDate;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
    @Embedded
    private ReservationDate date;
    @ManyToOne
    private ReservationTime time;
    @ManyToOne
    private Theme theme;
    @ManyToOne
    private Member member;

    public static Reservation of(
            final ReservationStatus status,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        checkRequiredData(status, time, theme, member);

        final ReservationDate reservationDate = new ReservationDate(date);
        return new Reservation(
                null,
                status,
                reservationDate,
                time,
                theme,
                member
        );
    }

    private static void checkRequiredData(
            final ReservationStatus status,
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member
    ) {
        if (status == null || reservationTime == null || theme == null || member == null) {
            throw new IllegalArgumentException("예약 상태, 시간, 테마, 회원 정보는 Null을 입력할 수 없습니다.");
        }
    }

    public static Reservation of(
            final Long id,
            final ReservationStatus status,
            final LocalDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        checkRequiredData(status, time, theme, member);

        return new Reservation(
                id,
                status,
                new ReservationDate(date),
                time,
                theme,
                member
        );
    }

    protected Reservation() {
    }

    private Reservation(
            final Long id,
            final ReservationStatus status,
            final ReservationDate date,
            final ReservationTime time,
            final Theme theme,
            final Member member
    ) {
        this.id = id;
        this.status = status;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
    }

    public Long getId() {
        return id;
    }

    public ReservationDate getDate() {
        return date;
    }

    public ReservationTime getTime() {
        return time;
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }

    public ReservationStatus getStatus() {
        return status;
    }
}
