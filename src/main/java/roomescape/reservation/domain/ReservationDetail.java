package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class ReservationDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_time_id", nullable = false)
    private ReservationTime reservationTime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    public ReservationDetail() {
    }

    public ReservationDetail(final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        this(null, date, reservationTime, theme);
    }

    public ReservationDetail(final Long id, final LocalDate date, final ReservationTime reservationTime, final Theme theme) {
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;

        validateNotNull();
    }

    private void validateNotNull() {
        if (date == null || reservationTime == null || theme == null) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA, "예약(Reservation) 생성 정보(date, reservationTime, theme) 에 null이 입력되었습니다.");
        }
    }

    public boolean isPastThen(final LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        if (this.date.isBefore(today)) {
            return true;
        }
        if (this.date.isEqual(today) && reservationTime.isBefore(nowTime)) {
            return true;
        }
        return false;
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


    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date=" + date +
                ", reservationTime=" + reservationTime +
                ", theme=" + theme +
                '}';
    }
}
