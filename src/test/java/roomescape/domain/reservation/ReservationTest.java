package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;
import roomescape.domain.theme.Theme;
import roomescape.system.exception.RoomescapeException;

class ReservationTest {

    @DisplayName("중복 예약은 허용하지 않는다.")
    @Test
    void validateDuplication() {
        // given
        List<Reservation> reservations = List.of(
            new Reservation(
                new Member(2L, "트레", "tre@email.com", "1234a", Role.ADMIN),
                "2066-05-05",
                new ReservationTime(1L, "10:00"),
                new Theme(1L, "테바와 두근두근 데이트", "정말로 후회 안하시겠습니까?", "https://tebah"),
                ReservationStatus.RESERVED
            )
        );
        Reservation reservation = new Reservation(
            new Member(1L, "테바", "tebah@email.com", "1234b", Role.USER),
            "2066-05-05",
            new ReservationTime(1L, "10:00"),
            new Theme(1L, "테바와 두근두근 데이트", "정말로 후회 안하시겠습니까?", "https://tebah"),
            ReservationStatus.RESERVED
        );
        // when & then
        assertThatCode(() -> reservation.validateDuplication(reservations))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("해당 시간에 예약이 이미 존재합니다.");
    }
}
