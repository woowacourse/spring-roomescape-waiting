package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.application.dto.MemberResponse;
import roomescape.reservation.application.dto.AvailableReservationTimeResponse;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MyReservationResponse;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.ReservationTimeResponse;
import roomescape.theme.application.dto.ThemeResponse;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Test
    void 모든_예약기록을_조회한다() {
        // given

        // when & then
        assertThat(reservationService.findReservedReservations()).hasSize(6);
    }

    @Test
    void 예약을_추가한다() {
        // given
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L);

        // when & then
        assertThat(reservationService.addMemberReservation(request, 1L)).isEqualTo(
                new ReservationResponse(
                        7L,
                        LocalDate.now().plusDays(1),
                        new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                        new ThemeResponse(1L, "인터스텔라", "시공간을 넘나들며 인류의 미래를 구해야 하는 극한의 두뇌 미션, 인터스텔라 방탈출!",
                                "https://upload.wikimedia.org/wikipedia/ko/b/b7/%EC%9D%B8%ED%84%B0%EC%8A%A4%ED%85%94%EB%9D%BC.jpg?20150905075839"),
                        new MemberResponse(1L, "엠제이")));
    }

    @Test
    void 예약을_삭제한다() {
        // given
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L);
        reservationService.addMemberReservation(request, 1L);

        // when
        final Long id = 7L;

        // then
        assertThatCode(() -> reservationService.deleteById(id)).doesNotThrowAnyException();
    }

    @Test
    void 예약가능한_시간을_조회한다() {
        // given
        final Long themeId = 2L;
        final String date = LocalDate.now().minusDays(3).toString();

        // when & then
        assertThat(reservationService.findAvailableReservationTime(themeId, date))
                .isEqualTo(List.of(
                        new AvailableReservationTimeResponse(1L, LocalTime.of(10, 0), true),
                        new AvailableReservationTimeResponse(2L, LocalTime.of(12, 0), true),
                        new AvailableReservationTimeResponse(3L, LocalTime.of(14, 0), false),
                        new AvailableReservationTimeResponse(4L, LocalTime.of(16, 0), false),
                        new AvailableReservationTimeResponse(5L, LocalTime.of(18, 0), false),
                        new AvailableReservationTimeResponse(6L, LocalTime.of(20, 0), false)
                ));
    }

    @Test
    void 해당기간에서_테마id와_멤버id로_예약을_조회한다() {
        // given
        final Long themeId = 2L;
        final Long memberId = 1L;
        final LocalDate start = LocalDate.now().minusDays(10);
        final LocalDate end = LocalDate.now().minusDays(1);

        // when

        // then
        assertThat(reservationService.findReservationByThemeIdAndMemberIdInDuration(
                themeId, memberId, start, end)).hasSize(2);
    }

    @Test
    void 멤버id로_예약기록을_조회한다() {
        // given
        final long memberId = 1L;

        // when
        final List<MyReservationResponse> responses = reservationService.findByMemberId(memberId);

        // then
        assertThat(responses).hasSize(2);
    }
}
