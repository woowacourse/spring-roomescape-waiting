package roomescape.reservation.domain.reservation;

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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private ReservationTime time;

    @ManyToOne(fetch = FetchType.LAZY)
    private Theme theme;

    public Reservation(final Long id, final LocalDate date, final Member member, final ReservationTime time,
                       final Theme theme) {
        validate(date, member, time, theme);
        this.id = id;
        this.date = date;
        this.member = member;
        this.time = time;
        this.theme = theme;
    }

    private void validate(final LocalDate date, final Member member, final ReservationTime time, final Theme theme) {
        ValidationUtils.validateNonNull(member, "예약 멤버는 필수입니다.");
        ValidationUtils.validateNonNull(date, "예약 날짜는 필수입니다.");
        ValidationUtils.validateNonNull(time, "예약 시간은 필수입니다.");
        ValidationUtils.validateNonNull(theme, "예약 테마는 필수입니다.");
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
