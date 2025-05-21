package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import roomescape.member.domain.Member;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;

public class WaitingTest {

    private Member member;
    private Theme theme;
    private ReservationTime time;
    private Waiting waiting;

    @BeforeEach
    void setUp() {
        member = Member.withDefaultRole("홍길동", "hong@example.com", "password");
        theme = Theme.of("테마명", "테마 설명", "thumbnail.jpg");
        time = ReservationTime.from(LocalTime.of(13, 0));
        waiting = Waiting.waiting(1L);
    }

    @Test
    void 대기_순위_감소_성공() {
        // given
        Waiting waiting1 = Waiting.waiting(10L);

        // when
        waiting1.reduceRank();

        // then
        assertThat(waiting1.getStatus()).isEqualTo(ReservationStatus.WAITING);
        assertThat(waiting1.getRank()).isEqualTo(9L);
    }

    @Test
    void 대기_순번_0_일때_예외_발생() {
        // given
        Waiting waiting = Waiting.waiting(0L);

        // when & then
        assertThatThrownBy(() -> waiting.reduceRank())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("더이상 대기 순번을 앞당길 수 없습니다. Waiting.id: " + waiting.getId());
    }

    @Test
    void 대기_순번_0_일때_자동_예약_변경() {
        // given
        Waiting waiting = Waiting.waiting(1L);

        // when
        waiting.reduceRank();

        // then
        assertThat(waiting.getStatus()).isEqualTo(ReservationStatus.BOOKED);
        assertThat(waiting.getRank()).isZero();
    }

    @Test
    void 예약_상태_변경_불가능_예외() {
        // given
        Waiting booked = Waiting.booked();

        // when & then
        assertThatThrownBy(booked::reduceRank)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("더이상 대기 순번을 앞당길 수 없습니다. Waiting.id: " + booked.getId());
    }
}
