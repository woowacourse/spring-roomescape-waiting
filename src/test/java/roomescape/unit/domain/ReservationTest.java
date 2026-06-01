package roomescape.unit.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.exception.BusinessRuleViolationException;
import roomescape.domain.exception.ForbiddenException;

class ReservationTest {

    private static final Theme ANY_THEME = new Theme(1L, "공포", "설명", "https://example.com/horror.jpg");
    private static final String RESERVATION_OWNER = "브라운";
    private static final Member RESERVER = new Member(RESERVATION_OWNER);
    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 5, 14);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 14, 14, 30);
    private static final LocalDateTime BEFORE_RESERVATION = LocalDateTime.of(2026, 5, 14, 13, 0);
    private static final ReservationTime TWO_PM = new ReservationTime(1L, LocalTime.of(14, 0));
    private static final ReservationTime THREE_PM = new ReservationTime(1L, LocalTime.of(15, 0));

    @Test
    void 지난_시각으로는_예약을_생성할_수_없다() {
        Slot slot = new Slot(RESERVATION_DATE, TWO_PM, ANY_THEME);

        assertThatThrownBy(() -> Reservation.createWith(RESERVER, slot, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("지난 시각에는 예약할 수 없습니다");
    }

    @Test
    void 미래_시각으로는_예약을_생성할_수_있다() {
        Slot slot = new Slot(RESERVATION_DATE, THREE_PM, ANY_THEME);

        assertThatCode(() -> Reservation.createWith(RESERVER, slot, NOW))
                .doesNotThrowAnyException();
    }

    @Test
    void 본인_예약이_아니면_취소할_수_없다() {
        Reservation reservation = reservation(TWO_PM);

        assertThatThrownBy(() -> reservation.cancelBy(new Member("티뉴"), BEFORE_RESERVATION))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void 지난_예약은_취소할_수_없다() {
        Reservation reservation = reservation(TWO_PM);

        assertThatThrownBy(() -> reservation.cancelBy(RESERVER, NOW))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("취소할 수 없습니다");
    }

    @Test
    void 본인의_미래_예약은_취소할_수_있다() {
        Reservation reservation = reservation(TWO_PM);

        assertThatCode(() -> reservation.cancelBy(RESERVER, BEFORE_RESERVATION))
                .doesNotThrowAnyException();
    }

    private Reservation reservation(ReservationTime time) {
        return new Reservation(RESERVER, new Slot(RESERVATION_DATE, time, ANY_THEME));
    }
}
