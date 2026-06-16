package roomescape.controller.client.fixture;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.controller.client.dto.request.ReservationRequest;
import roomescape.support.TestDateTimes;

public class ReservationApiRequestFixture {

    public static Stream<Arguments> reserveFailRequestFixture() {
        return Stream.of(
                Arguments.of(
                        new ReservationRequest(null, TestDateTimes.tomorrow(), 1L, 1L, 30000L),
                        "예약자 이름 정보는 비어있을 수 없습니다."
                ),
                Arguments.of(
                        new ReservationRequest("", TestDateTimes.tomorrow(), 1L, 1L, 30000L),
                        "예약자 이름 정보는 비어있을 수 없습니다."
                ),
                Arguments.of(
                        new ReservationRequest(" ", TestDateTimes.tomorrow(), 1L, 1L, 30000L),
                        "예약자 이름 정보는 비어있을 수 없습니다."
                ),
                Arguments.of(
                        new ReservationRequest("이프", null, 1L, 1L, 30000L),
                        "예약 날짜 정보는 필수 값입니다."
                ),
                Arguments.of(
                        new ReservationRequest("이프", TestDateTimes.yesterday(), 1L, 1L, 30000L),
                        "이미 지난 날짜는 예약할 수 없습니다."
                ),
                Arguments.of(
                        new ReservationRequest("이프", TestDateTimes.today(), null, 1L, 30000L),
                        "테마 식별자는 필수 값입니다."
                ),
                Arguments.of(
                        new ReservationRequest("이프", TestDateTimes.today(), 0L, 1L, 30000L),
                        "테마 식별자는 식별 가능한 양수입니다."
                ),
                Arguments.of(
                        new ReservationRequest("이프", TestDateTimes.today(), 1L, null, 30000L),
                        "예약 시간 식별자는 필수 값입니다."
                ),
                Arguments.of(
                        new ReservationRequest("이프", TestDateTimes.today(), 1L, 0L, 30000L),
                        "예약 시간 식별자는 식별 가능한 양수입니다."
                ),
                Arguments.of(
                        new ReservationRequest("이프", TestDateTimes.today(), 1L, 1L, null),
                        "결제 금액은 필수 값입니다."
                ),
                Arguments.of(
                        new ReservationRequest("이프", TestDateTimes.today(), 1L, 1L, 0L),
                        "결제 금액은 양수여야 합니다."
                )
        );
    }

    public static ReservationRequest reserveSuccessRequestFixture() {
        return new ReservationRequest("이프", TestDateTimes.tomorrow(), 1L, 1L, 30000L);
    }
}
