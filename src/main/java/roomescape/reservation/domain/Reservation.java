package roomescape.reservation.domain;

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
import java.time.LocalTime;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id", nullable = false)
    private ReservationTime reservationTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(value = EnumType.STRING)
    private ReservationStatus reservationStatus;

    public Reservation() {
    }

    public Reservation(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member
    ) {
        this(null, date, reservationTime, theme, member, ReservationStatus.WAITING);
    }

    public Reservation(
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member,
            final ReservationStatus status
    ) {
        this(null, date, reservationTime, theme, member, status);
    }

    public Reservation(
            final Long id,
            final LocalDate date,
            final ReservationTime reservationTime,
            final Theme theme,
            final Member member,
            final ReservationStatus status
    ) {
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;
        this.reservationStatus = status;
        validateBlank();
    }

    private void validateBlank() {
        if (date == null || reservationTime == null || theme == null || member == null) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("예약(Reservation) 생성에 유효하지 않은 값(null OR 공백)이 입력되었습니다. [values: %s]", this));
        }
    }

    public boolean isReserved() {
        return reservationStatus == ReservationStatus.RESERVED;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public String getThemeName() {
        return theme.getName();
    }

    public LocalTime getStartAt() {
        return reservationTime.getStartAt();
    }

    public Theme getTheme() {
        return theme;
    }

    public Member getMember() {
        return member;
    }
}
