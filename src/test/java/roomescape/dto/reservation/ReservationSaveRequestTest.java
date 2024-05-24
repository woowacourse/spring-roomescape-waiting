package roomescape.dto.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.member.Role;
import roomescape.dto.MemberResponse;
import roomescape.dto.theme.ThemeResponse;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.*;

class ReservationSaveRequestTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "22:00:00", "abc"})
    @DisplayName("예약 날짜 입력 값이 유효하지 않으면 예외가 발생한다.")
    void throwExceptionWhenCannotConvertToLocalDate(final String invalidDate) {
        final ReservationSaveRequest request = new ReservationSaveRequest(1L, invalidDate, 1L, 1L);
        final MemberResponse memberResponse = new MemberResponse(1L, MEMBER_TENNY_NAME, MEMBER_MIA_EMAIL, Role.MEMBER);
        final ThemeResponse themeResponse = new ThemeResponse(1L, THEME_HORROR_NAME, THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
        final ReservationTimeResponse timeResponse = new ReservationTimeResponse(1L, START_AT_SIX);

        assertThatThrownBy(() -> request.toModel(memberResponse, themeResponse, timeResponse))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @ParameterizedTest
    @MethodSource("invalidLocalDate")
    @DisplayName("예약 날짜가 현재 날짜 이후가 아닌 경우 예외가 발생한다.")
    void throwExceptionWhenInvalidDate(final String invalidDate) {
        final ReservationSaveRequest request = new ReservationSaveRequest(1L, invalidDate, 1L, 1L);
        final MemberResponse memberResponse = new MemberResponse(1L, MEMBER_TENNY_NAME, MEMBER_MIA_EMAIL, Role.MEMBER);
        final ThemeResponse themeResponse = new ThemeResponse(1L, THEME_HORROR_NAME, THEME_HORROR_DESCRIPTION, THEME_HORROR_THUMBNAIL);
        final ReservationTimeResponse timeResponse = new ReservationTimeResponse(1L, START_AT_SIX);

        assertThatThrownBy(() -> request.toModel(memberResponse, themeResponse, timeResponse))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<String> invalidLocalDate() {
        return Stream.of(
                LocalDate.now().toString(),
                LocalDate.now().minusDays(1L).toString()
        );
    }
}
