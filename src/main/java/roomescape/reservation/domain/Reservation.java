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
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

//TODO: ReservationDetail 로 변경
@Entity
public class Reservation {
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
    //TODO: 제거
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public Reservation() {
    }

    public Reservation(final LocalDate date, final ReservationTime reservationTime, final Theme theme, final Member member) {
        this(null, date, reservationTime, theme, member);
    }

    public Reservation(final Long id, final LocalDate date, final ReservationTime reservationTime, final Theme theme, final Member member) {
        this.id = id;
        this.date = date;
        this.reservationTime = reservationTime;
        this.theme = theme;
        this.member = member;

        validateNotNull();
    }

    private void validateNotNull() {
        if (date == null || reservationTime == null || theme == null || member == null) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA, "예약(Reservation) 생성에 null이 입력되었습니다.");
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

    public Member getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", date=" + date +
                ", reservationTime=" + reservationTime +
                ", theme=" + theme +
                ", member=" + member +
                '}';
    }
}
