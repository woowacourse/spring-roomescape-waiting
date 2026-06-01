package roomescape.presentation.fixture;

import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import roomescape.reservation.presentation.dto.ReservationChangeRequest;
import roomescape.reservation.presentation.dto.ReservationRequest;

public final class ReservationRequestFixture {

    private ReservationRequestFixture() {
    }

    public static Stream<Arguments> reserveFailRequestFixture() {
        return Stream.of(
                Arguments.of(new ReservationRequest(null, LocalDate.now().plusDays(1), 1L, 1L), "[name] 이름은 필수입니다."),
                Arguments.of(new ReservationRequest("", LocalDate.now().plusDays(1), 1L, 1L), "[name] 이름은 필수입니다."),
                Arguments.of(new ReservationRequest("바니", null, 1L, 1L), "[date] 날짜는 필수입니다."),
                Arguments.of(new ReservationRequest("바니", LocalDate.now().plusDays(1), null, 1L), "[timeId] 시간 ID는 필수입니다."),
                Arguments.of(new ReservationRequest("바니", LocalDate.now().plusDays(1), 1L, null), "[themeId] 테마 ID는 필수입니다.")
        );
    }

    public static ReservationRequest reserveSuccessRequestFixture() {
        return new ReservationRequest("바니", LocalDate.now().plusDays(1), 1L, 1L);
    }

    public static Stream<Arguments> modifyFailRequestFixture() {
        return Stream.of(
                Arguments.of(new ReservationChangeRequest(null, LocalDate.now().plusDays(1), 1L, 1L), "[username] 이름은 필수입니다."),
                Arguments.of(new ReservationChangeRequest("바니", null, 1L, 1L), "[date] 날짜는 필수입니다."),
                Arguments.of(new ReservationChangeRequest("바니", LocalDate.now().plusDays(1), null, 1L), "[timeId] 시간 ID는 필수입니다."),
                Arguments.of(new ReservationChangeRequest("바니", LocalDate.now().plusDays(1), 1L, null), "[themeId] 테마 ID는 필수입니다.")
        );
    }

    public static ReservationChangeRequest modifySuccessRequestFixture() {
        return new ReservationChangeRequest("바니", LocalDate.now().plusDays(1), 1L, 1L);
    }
}
