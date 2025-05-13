package roomescape.application.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.testFixture.Fixture.MEMBER1_ADMIN;
import static roomescape.testFixture.Fixture.MEMBER2_USER;
import static roomescape.testFixture.Fixture.MEMBER3_USER;
import static roomescape.testFixture.Fixture.MEMBER4_USER;
import static roomescape.testFixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.application.dto.MemberDto;
import roomescape.application.dto.ReservationDto;
import roomescape.application.dto.UserReservationCreateDto;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;

class ReservationMapperTest {

    @DisplayName("예약 요청을 Reservation 객체로 변환한다.")
    @Test
    void request_toReservation() {
        // given
        LocalDate reservationDate = LocalDate.of(2024, 4, 1);
        ReservationTime reservationTime = ReservationTime.of(1L, LocalTime.of(10, 0));
        UserReservationCreateDto request = new UserReservationCreateDto(1L, reservationDate, 1L);

        // when
        Reservation reservation = Reservation.withoutId(MEMBER1_ADMIN, THEME_1, request.date(), reservationTime);

        // then
        assertAll(
                () -> assertThat(reservation.getId()).isNull(),
                () -> assertThat(reservation.getMember()).isEqualTo(MEMBER1_ADMIN),
                () -> assertThat(reservation.getReservationDate()).isEqualTo(reservationDate),
                () -> assertThat(reservation.getReservationTime()).isEqualTo(reservationTime)
        );
    }

    @DisplayName("Reservation 객체를 응답객체로 변환한다.")
    @Test
    void reservation_toResponse() {
        // given
        Long timeId = 1L;

        LocalDate reservationDate = LocalDate.of(2025, 4, 1);
        ReservationTime reservationTime = ReservationTime.of(timeId, LocalTime.of(10, 0));
        Reservation reservation = Reservation.of(timeId, MEMBER1_ADMIN, THEME_1, reservationDate, reservationTime);

        // when
        ReservationDto dto = ReservationDto.from(reservation);

        // then
        assertAll(
                () -> assertThat(dto.id()).isEqualTo(1L),
                () -> assertThat(dto.member().id()).isEqualTo(1),
                () -> assertThat(dto.date()).isEqualTo(reservationDate),
                () -> assertThat(dto.time().id()).isEqualTo(timeId)
        );
    }

    @DisplayName("Reservation 객체들을 응답 리스트로 한꺼번에 변환한다.")
    @Test
    void multipleReservations_toResponse() {
        // given
        ReservationTime time1 = ReservationTime.of(1L, LocalTime.of(10, 0));
        ReservationTime time2 = ReservationTime.of(2L, LocalTime.of(11, 0));

        Reservation reservation1 = Reservation.of(1L, MEMBER2_USER, THEME_1, LocalDate.of(2024, 4, 1), time1);
        Reservation reservation2 = Reservation.of(2L, MEMBER3_USER, THEME_1, LocalDate.of(2024, 4, 1), time2);
        Reservation reservation3 = Reservation.of(3L, MEMBER4_USER, THEME_1, LocalDate.of(2024, 4, 2), time1);

        List<Reservation> reservations = List.of(reservation1, reservation2, reservation3);

        // when
        List<ReservationDto> dtos = ReservationDto.from(reservations);

        // then
        assertAll(
                () -> assertThat(dtos).hasSize(3),
                () -> assertThat(dtos)
                        .extracting(ReservationDto::member)
                        .containsExactly(MemberDto.from(MEMBER2_USER), MemberDto.from(MEMBER3_USER), MemberDto.from(
                                MEMBER4_USER))
        );
    }
}
