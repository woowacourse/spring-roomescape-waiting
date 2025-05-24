package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import roomescape.member.domain.Member;
import roomescape.theme.domain.Theme;

@Entity
@Table(name = "reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = {"id"})
@ToString
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id")
    private ReservationTime time;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private Theme theme;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    public Reservation(final Long id, final LocalDate date, final ReservationTime time,
                       final Theme theme, final Member member, final BookingStatus status) {
        validateDate(date);
        validateTime(time);
        validateTheme(theme);
        validateMember(member);
        validateStatus(status);

        this.id = id;
        this.date = date;
        this.time = time;
        this.theme = theme;
        this.member = member;
        this.status = status;
    }

    public static Reservation createForRegister(final LocalDate date, final ReservationTime time, final Theme theme,
                                                final Member member,
                                                final BookingStatus state) {
        validateFutureReservation(date, time);
        return new Reservation(null, date, time, theme, member, state);
    }

    private static void validateFutureReservation(final LocalDate date, final ReservationTime time) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new IllegalArgumentException("예약 시간은 현재 시간보다 이후여야 합니다.");
        }
    }

    private void validateMember(final Member member) {
        if (member == null) {
            throw new IllegalArgumentException("멤버는 null이면 안됩니다.");
        }
    }

    private void validateDate(final LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null이면 안됩니다.");
        }
    }

    private void validateTime(final ReservationTime time) {
        if (time == null) {
            throw new IllegalArgumentException("시간은 null이면 안됩니다.");
        }
    }

    private void validateTheme(final Theme theme) {
        if (theme == null) {
            throw new IllegalArgumentException("테마는 null이면 안됩니다.");
        }
    }

    private void validateStatus(final BookingStatus state) {
        if (state == null) {
            throw new IllegalArgumentException("예약 상태는 null이면 안됩니다.");
        }
    }

    public void confirmReservation() {
        this.status = BookingStatus.CONFIRMED;
    }
}
