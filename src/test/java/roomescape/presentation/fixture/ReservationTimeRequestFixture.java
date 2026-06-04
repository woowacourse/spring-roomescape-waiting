package roomescape.presentation.fixture;

import java.time.LocalTime;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.time.presentation.dto.ReservationTimeRequest;

public final class ReservationTimeRequestFixture {

    private ReservationTimeRequestFixture() {
    }

    public static Stream<Arguments> registerFailRequestFixture() {
        return Stream.of(Arguments.of(new ReservationTimeRequest(null), "[startAt] 시간은 필수입니다."));
    }

    public static ReservationTimeRequest registerSuccessRequestFixture() {
        return new ReservationTimeRequest(LocalTime.of(13, 0));
    }
}
