package roomescape.reservation.domain.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.theme.Theme;
import roomescape.reservation.domain.time.ReservationTime;
import roomescape.reservation.domain.util.ValidationUtils;

@Entity
@Getter
@NoArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode(of = "id")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Member member;

    private LocalDate date;

    @ManyToOne
    private ReservationTime time;

    @ManyToOne
    private Theme theme;

    @Enumerated(EnumType.STRING)
    private final ReservationStatus status = ReservationStatus.RESERVED;

    public Reservation(final Long id, final Member member, final LocalDate date, final ReservationTime time,
                       final Theme theme) {
        validate(member, date, time, theme);
        this.id = id;
        this.member = member;
        this.date = date;
        this.time = time;
        this.theme = theme;
    }

    private void validate(final Member member, final LocalDate date, final ReservationTime time, final Theme theme) {
        ValidationUtils.validateNonNull(member, "예약 멤버를 입력해야 합니다.");
        ValidationUtils.validateNonNull(date, "예약 날짜를 입력해야 합니다.");
        ValidationUtils.validateNonNull(time, "예약 시간을 입력해야 합니다.");
        ValidationUtils.validateNonNull(theme, "예약 테마를 입력해야 합니다.");
    }

    public boolean isSameTime(final ReservationTime time) {
        return this.time.equals(time);
    }

    public boolean isMemberSameId(final long id) {
        return member.isSameId(id);
    }

    public boolean isThemeSameId(final long id) {
        return theme.isSameId(id);
    }

    public boolean isDateBetween(final LocalDate from, final LocalDate to) {
        return (date.isEqual(from) || date.isAfter(from)) &&
                (date.isEqual(to) || date.isBefore(to));
    }
}
