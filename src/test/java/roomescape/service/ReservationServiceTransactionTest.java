package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.jdbc.Sql;
import roomescape.repository.ReservationRepository;
import roomescape.service.command.ReservationChangeCommand;
import roomescape.service.result.ReservationResult;
import roomescape.support.IntegrationTest;
import roomescape.support.TestDateTimes;

@IntegrationTest
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:tx")
@Sql("/transaction-fixture.sql")
class ReservationServiceTransactionTest {

    @Autowired
    private ReservationService reservationService;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @Test
    void 예약_변경_과정에서_새로운_예약_저장_실패_시에도_정합성이_보장된다() {
        // given: 픽스처로 미리 저장된 '라텔'의 예약(엔트리 id 1) — 내일 / 공포 / 10:00
        long entryId = 1L;

        // 새로 만들어지는 target(id == null) 저장만 실패시킨다.
        // current(취소) 저장은 id가 있어 실제 수행되므로 롤백 대상이 된다.
        Mockito.doThrow(new RuntimeException())
                .when(reservationRepository).save(argThat(reservation -> reservation.getId() == null));

        // when: time2(11:00)로 변경 시도 → 2차 저장에서 실패
        assertThatThrownBy(() -> reservationService.change(
                entryId, new ReservationChangeCommand(TestDateTimes.tomorrow(), 2L)))
                .isInstanceOf(RuntimeException.class);

        // then: 트랜잭션 롤백으로 기존 예약이 그대로 활성 상태(취소 미영속, 이동 안 됨)
        ReservationResult found = reservationService.getActiveReservationEntry(entryId);
        assertThat(found.time().startAt()).isEqualTo(TestDateTimes.defaultTime());
        assertThat(found.entry().status()).isEqualTo("RESERVED");
    }
}
