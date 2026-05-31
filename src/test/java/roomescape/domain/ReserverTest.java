package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;

class ReserverTest {

    @DisplayName("예약자 이름을 저장한다.")
    @Test
    void create() {
        Reserver reserver = new Reserver("러로");

        assertThat(reserver.getName()).isEqualTo("러로");
    }

    @DisplayName("예약자 이름은 255자까지 허용한다.")
    @Test
    void maxLength() {
        String name = "가".repeat(255);

        Reserver reserver = new Reserver(name);

        assertThat(reserver.getName()).hasSize(255);
    }

    @DisplayName("예약자 이름이 256자를 초과하면 예외를 던진다.")
    @Test
    void overMaxLength() {
        assertRoomescapeException(
                () -> new Reserver("가".repeat(256)),
                DomainErrorCode.INVALID_INPUT
        );
    }

    @DisplayName("예약자 이름은 null, 빈 문자열, 공백일 수 없다.")
    @Test
    void blankName() {
        assertRoomescapeException(() -> new Reserver(null), DomainErrorCode.INVALID_INPUT);
        assertRoomescapeException(() -> new Reserver(""), DomainErrorCode.INVALID_INPUT);
        assertRoomescapeException(() -> new Reserver("   "), DomainErrorCode.INVALID_INPUT);
    }

    @DisplayName("같은 이름의 예약자는 같은 예약자로 판단한다.")
    @Test
    void equality() {
        assertThat(new Reserver("러로")).isEqualTo(new Reserver("러로"));
        assertThat(new Reserver("러로")).hasSameHashCodeAs(new Reserver("러로"));
    }

    @DisplayName("다른 예약자 또는 null은 본인 검증에 실패한다.")
    @Test
    void validateSameReserver() {
        Reserver reserver = new Reserver("러로");

        reserver.validateSameReserver(new Reserver("러로"));

        assertRoomescapeException(
                () -> reserver.validateSameReserver(new Reserver("다른사람")),
                DomainErrorCode.UNAUTHORIZED_RESERVATION
        );
        assertRoomescapeException(
                () -> reserver.validateSameReserver(null),
                DomainErrorCode.UNAUTHORIZED_RESERVATION
        );
    }

    private void assertRoomescapeException(Runnable runnable, DomainErrorCode code) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(RoomescapeException.class)
                .extracting("code")
                .isEqualTo(code);
    }
}
