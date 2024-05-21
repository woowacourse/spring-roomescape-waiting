package roomescape.service.mapper;

import static roomescape.fixture.ReservationFixture.DEFAULT_RESERVATION;
import static roomescape.fixture.ReservationWaitingFixture.DEFAULT_WAITING;
import static roomescape.fixture.ThemeFixture.DEFAULT_THEME;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.LoginMemberReservationResponse;

class LoginMemberReservationResponseMapperTest {

    @Test
    @DisplayName("도메인을 응답으로 잘 변환하는지 확인")
    void toResponse() {
        LoginMemberReservationResponse response = LoginMemberReservationResponseMapper
                .toResponse(DEFAULT_RESERVATION);

        Assertions.assertThat(response)
                .isEqualTo(new LoginMemberReservationResponse(
                        DEFAULT_RESERVATION.getId(),
                        DEFAULT_THEME.getName(),
                        DEFAULT_RESERVATION.getDate(),
                        DEFAULT_RESERVATION.getTime(),
                        "예약"
                ));
    }

    @Test
    @DisplayName("예약 대기 응답을 내 예약 조회 응답으로 잘 변환하는지 확인")
    void from() {
        LoginMemberReservationResponse response = LoginMemberReservationResponseMapper
                .from(ReservationWaitingResponseMapper.toResponse(DEFAULT_WAITING,
                        List.of(DEFAULT_WAITING)));

        Assertions.assertThat(response)
                .isEqualTo(new LoginMemberReservationResponse(
                        DEFAULT_RESERVATION.getId(),
                        DEFAULT_THEME.getName(),
                        DEFAULT_RESERVATION.getDate(),
                        DEFAULT_RESERVATION.getTime(),
                        "1번째 예약 대기"
                ));
    }
}
