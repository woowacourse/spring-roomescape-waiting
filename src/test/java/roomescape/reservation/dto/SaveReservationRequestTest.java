package roomescape.reservation.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRole;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.ReservationTime;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class SaveReservationRequestTest {

    @DisplayName("SaveReservationRequest를 Reservation으로 변환한다.")
    @Test
    void toReservationTest() {
        // Given
        final LocalDate reservationDate = LocalDate.now().plusDays(1);
        final ReservationTime reservationTime = new ReservationTime(1L, LocalTime.of(2, 22));
        final Theme theme = Theme.of(1L, "테바의 비밀친구", "테바의 은밀한 비밀친구", "대충 테바 사진 링크");
        final Member member = Member.createMemberWithId(3L, MemberRole.USER, "password1111", "kelly", "kelly6bf@mail.com");
        final SaveReservationRequest request = new SaveReservationRequest(
                reservationDate,
                3L,
                1L,
                1L
        );

        // When
        final Reservation reservation = request.toReservation(reservationTime, theme, member);

        // Then
        assertAll(
                () -> assertThat(reservation.getDate().value()).isEqualTo(request.date()),
                () -> assertThat(reservation.getMember().getId()).isEqualTo(request.memberId()),
                () -> assertThat(reservation.getTime().getId()).isEqualTo(request.timeId()),
                () -> assertThat(reservation.getTime().getStartAt()).isEqualTo(reservationTime.getStartAt())
        );
    }
}
