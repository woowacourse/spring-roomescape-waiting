package roomescape.service;


import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static roomescape.Fixture.VALID_MEMBER;
import static roomescape.Fixture.VALID_RESERVATION;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.domain.ReservationWaiting;
import roomescape.infrastructure.ReservationWaitingRepository;
import roomescape.service.response.ReservationWaitingAppResponse;

@ExtendWith(MockitoExtension.class)
class ReservationWaitingServiceTest {

    @InjectMocks
    private ReservationWaitingService reservationWaitingService;

    @Mock
    private ReservationWaitingRepository reservationWaitingRepository;

    @DisplayName("회원 ID를 가진 예약 대기 정보를 불러온다.")
    @Test
    void findAllByMemberId() {
        ReservationWaiting data = new ReservationWaiting(VALID_MEMBER, VALID_RESERVATION, 1L);
        when(reservationWaitingRepository.findAllByMemberId(VALID_MEMBER.getId()))
                .thenReturn(List.of(data));

        List<ReservationWaitingAppResponse> actual = reservationWaitingService.findAllByMemberId(
                VALID_MEMBER.getId());

        List<ReservationWaitingAppResponse> expected = List.of(ReservationWaitingAppResponse.from(data));

        assertAll(
                () -> assertEquals(1, actual.size()),
                () -> assertEquals(expected.get(0).id(), actual.get(0).id())
        );
    }
}
