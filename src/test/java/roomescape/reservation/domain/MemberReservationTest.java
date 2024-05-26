package roomescape.reservation.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;

import java.util.stream.Stream;

class MemberReservationTest {

    @ParameterizedTest
    @MethodSource("validateConstructorParameterBlankSource")
    @DisplayName("객체 생성 시, null 또는 공백이 존재하면 예외를 발생한다.")
    void validateConstructorParameterBlank(ReservationDetail reservationDetail, Member member, ReservationStatus status) {

        // when & then
        Assertions.assertThatThrownBy(() -> new MemberReservation(reservationDetail, member, status))
                .isInstanceOf(ValidateException.class)
                .hasMessage("예약 대기(MemberReservation) 생성 값(reservationDetail, member, status)에 null이 입력되었습니다.");

    }

    static Stream<Arguments> validateConstructorParameterBlankSource() {
        return Stream.of(
                Arguments.of(null,
                        new Member(),
                        ReservationStatus.RESERVED
                ),
                Arguments.of(new ReservationDetail(),
                        null,
                        ReservationStatus.RESERVED
                ),
                Arguments.of(new ReservationDetail(),
                        new Member(),
                        null
                )
        );
    }

}
