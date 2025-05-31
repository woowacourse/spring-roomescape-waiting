package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class ReservationStatusTest {

    private Member member;
    private Theme theme;
    private ReservationTime time;
    private ReservationStatus reservationStatus;

    @BeforeEach
    void setUp() {
        member = Member.withDefaultRole("홍길동", "hong@example.com", "password");
        theme = Theme.of("테마명", "테마 설명", "thumbnail.jpg");
        time = ReservationTime.from(LocalTime.of(13, 0));
        reservationStatus = ReservationStatus.waiting(1L);
    }

    @Test
    void 대기_순위_감소_성공() {
        // given
        ReservationStatus reservationStatus1 = ReservationStatus.waiting(10L);

        // when
        reservationStatus1.reduceRank();

        // then
        assertThat(reservationStatus1.getStatus()).isEqualTo(Status.WAITING);
        assertThat(reservationStatus1.getRank()).isEqualTo(9L);
    }

    @Test
    void 대기_순번_0_일때_예외_발생() {
        // given
        ReservationStatus reservationStatus = ReservationStatus.waiting(0L);

        // when & then
        assertThatThrownBy(() -> reservationStatus.reduceRank())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("더이상 대기 순번을 앞당길 수 없습니다. Waiting.id: " + reservationStatus.getId());
    }

    @Test
    void 대기_순번_0_일때_자동_예약_변경() {
        // given
        ReservationStatus reservationStatus = ReservationStatus.waiting(1L);

        // when
        reservationStatus.reduceRank();

        // then
        assertThat(reservationStatus.getStatus()).isEqualTo(Status.BOOKED);
        assertThat(reservationStatus.getRank()).isZero();
    }

    @Test
    void 예약_상태_변경_불가능_예외() {
        // given
        ReservationStatus booked = ReservationStatus.booked();

        // when & then
        assertThatThrownBy(booked::reduceRank)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("더이상 대기 순번을 앞당길 수 없습니다. Waiting.id: " + booked.getId());
    }
}
