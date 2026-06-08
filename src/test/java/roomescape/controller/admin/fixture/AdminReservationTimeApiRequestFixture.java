package roomescape.controller.admin.fixture;

import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.service.command.ReservationTimeCommand;
import roomescape.support.TestDateTimes;

public class AdminReservationTimeApiRequestFixture {

    public static Stream<Arguments> registerFailRequestFixture() {
        return Stream.of(
                Arguments.of(
                        new ReservationTimeCommand(null),
                        "예약 시간 정보는 필수 값입니다."
                )
        );
    }

    public static ReservationTimeCommand registerSuccessRequestFixture() {
        return new ReservationTimeCommand(TestDateTimes.defaultTime());
    }
}