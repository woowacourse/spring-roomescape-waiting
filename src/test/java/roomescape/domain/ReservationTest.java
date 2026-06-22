package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.domain.ReservationException;

class ReservationTest {

    @Test
    void 현재보다_이전_날짜면_예외를_반환한다() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, Month.MAY, 14, 11, 0);
        LocalDate pastDate = now.toLocalDate().minusDays(1);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(11, 0));
        Theme theme = new Theme(1L, "테마", "설명설명설명설명설명설명", "https://sdfsd.com");
        Slot slot = new Slot(pastDate, reservationTime, theme);

        // when & then
        assertThatThrownBy(() -> Reservation.createFutureReservation(
                "예약자",
                slot,
                now
        ))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorCode.PAST_DATE_NOT_ALLOWED.getMessage());
    }

    @Test
    void 현재보다_이전_시간이면_예외를_반환한다() {
        // given
        LocalDateTime now = LocalDateTime.of(2026, Month.MAY, 14, 11, 0);
        LocalDate today = now.toLocalDate();
        ReservationTime pastTime = new ReservationTime(1L, LocalTime.of(10, 0));
        Theme theme = new Theme(1L, "테마", "설명설명설명설명설명설명", "https://sdfsd.com");
        Slot slot = new Slot(today, pastTime, theme);

        // when & then
        assertThatThrownBy(() -> Reservation.createFutureReservation(
                "예약자",
                slot,
                now
        ))
                .isInstanceOf(ReservationException.class)
                .hasMessage(ReservationErrorCode.PAST_DATE_NOT_ALLOWED.getMessage());
    }

    @ParameterizedTest(name = "현재 시간 {0}이면 취소 마감 지남 여부는 {1}")
    @CsvSource({
            "2026-05-09T14:59:00, false",
            "2026-05-09T15:00:00, false",
            "2026-05-09T15:01:00, true"
    })
    void 예약_삭제_하려는_시간이_마감_기한을_지났는지_확인한다(String nowText, boolean expected) {
        // given
        Reservation reservation = new Reservation(
                1L,
                new Slot(
                        LocalDate.of(2026, 5, 10),
                        new ReservationTime(1L, LocalTime.of(15, 0)),
                        new Theme(1L, "공포의 저택", "무서운 방탈출", "https://image.com")
                ),
                "브라운",
                roomescape.domain.ReservationStatus.CONFIRMED
        );

        LocalDateTime now = LocalDateTime.parse(nowText);

        // when
        boolean result = reservation.isNotModifiableAt(now);

        // then
        assertThat(result).isEqualTo(expected);
    }
}
