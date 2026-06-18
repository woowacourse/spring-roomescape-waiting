package roomescape.controller.admin.fixture;

import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.application.service.command.ReservationTimeCommand;

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
        return new ReservationTimeCommand(LocalTime.of(10, 0));
    }
}