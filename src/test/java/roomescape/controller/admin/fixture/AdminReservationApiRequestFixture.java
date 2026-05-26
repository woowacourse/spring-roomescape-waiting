package roomescape.controller.admin.fixture;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.controller.admin.api.dto.AdminReservationRequest;

public class AdminReservationApiRequestFixture {

    public static Stream<Arguments> reserveFailRequestFixture() {
        return Stream.of(
                Arguments.of(
                        new AdminReservationRequest(null, LocalDate.now().plusDays(1), 1L, 1L),
                        "예약자 이름 정보는 비어있을 수 없습니다."
                ),
                Arguments.of(
                        new AdminReservationRequest("", LocalDate.now().plusDays(1), 1L, 1L),
                        "예약자 이름 정보는 비어있을 수 없습니다."
                ),
                Arguments.of(
                        new AdminReservationRequest(" ", LocalDate.now().plusDays(1), 1L, 1L),
                        "예약자 이름 정보는 비어있을 수 없습니다."
                ),
                Arguments.of(
                        new AdminReservationRequest("이프", null, 1L, 1L),
                        "예약 날짜 정보는 필수 값입니다."
                ),
                Arguments.of(
                        new AdminReservationRequest("이프", LocalDate.now().minusDays(1), 1L, 1L),
                        "이미 지난 날짜는 예약할 수 없습니다."
                ),
                Arguments.of(
                        new AdminReservationRequest("이프", LocalDate.now(), null, 1L),
                        "테마 식별자는 필수 값입니다."
                ),
                Arguments.of(
                        new AdminReservationRequest("이프", LocalDate.now(), 0L, 1L),
                        "테마 식별자는 식별 가능한 양수입니다."
                ),
                Arguments.of(
                        new AdminReservationRequest("이프", LocalDate.now(), 1L, null),
                        "예약 시간 식별자는 필수 값입니다."
                ),
                Arguments.of(
                        new AdminReservationRequest("이프", LocalDate.now(), 1L, 0L),
                        "예약 시간 식별자는 식별 가능한 양수입니다."
                )
        );
    }

    public static AdminReservationRequest reserveSuccessRequestFixture() {
        return new AdminReservationRequest("이프", LocalDate.now().plusDays(1), 1L, 1L);
    }
}
