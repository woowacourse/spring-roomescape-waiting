package roomescape.reservation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.application.dto.MemberResponse;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.ReservationResponse;
import roomescape.reservation.application.dto.ReservationTimeResponse;
import roomescape.reservation.domain.BookingStatus;
import roomescape.theme.application.dto.ThemeResponse;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
class ReservationCommandServiceTest {

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Test
    void 예약을_추가한다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.RESERVED);

        assertThat(reservationCommandService.addMemberReservation(request, 1L)).isEqualTo(
                new ReservationResponse(
                        12L,
                        LocalDate.now().plusDays(1),
                        new ReservationTimeResponse(1L, LocalTime.of(10, 0)),
                        new ThemeResponse(1L, "인터스텔라", "시공간을 넘나들며 인류의 미래를 구해야 하는 극한의 두뇌 미션, 인터스텔라 방탈출!",
                                "https://upload.wikimedia.org/wikipedia/ko/b/b7/%EC%9D%B8%ED%84%B0%EC%8A%A4%ED%85%94%EB%9D%BC.jpg?20150905075839"),
                        new MemberResponse(1L, "엠제이")));
    }

    @Test
    void 예약을_삭제한다() {
        final MemberReservationRequest request = new MemberReservationRequest(
                LocalDate.now().plusDays(1), 1L, 1L, BookingStatus.RESERVED);
        reservationCommandService.addMemberReservation(request, 1L);

        final Long id = 7L;
        assertThatCode(() -> reservationCommandService.deleteById(id)).doesNotThrowAnyException();
    }
}

