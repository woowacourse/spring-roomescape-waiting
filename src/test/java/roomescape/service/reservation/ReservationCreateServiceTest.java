package roomescape.service.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberName;
import roomescape.domain.member.MemberPassword;
import roomescape.domain.Role;
import roomescape.service.BaseServiceTest;
import roomescape.service.dto.request.ReservationSaveRequest;

class ReservationCreateServiceTest extends BaseServiceTest {

    @Autowired
    private ReservationCreateService reservationCreateService;

    @Test
    @DisplayName("예약 가능한 시간인 경우 성공한다.")
    void checkDuplicateReservationTime_Success() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().plusDays(1L), 2L, 2L);
        Member member = new Member(
                1L,
                new MemberName("capy"),
                new MemberEmail("test@naver.com"),
                new MemberPassword("1234"), Role.USER);

        assertThatCode(() -> reservationCreateService.create(request, member))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이미 예약된 시간인 경우 예외가 발생한다.")
    void checkDuplicateReservationTime_Failure() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().plusDays(1L), 1L, 1L);
        Member member = new Member(
                new MemberName("capy"),
                new MemberEmail("test@naver.com"),
                new MemberPassword("1234"), Role.USER);

        assertThatThrownBy(() -> reservationCreateService.create(request, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 시간에 이미 예약된 테마입니다.");
    }

    @Test
    @DisplayName("지나간 날짜와 시간에 대한 예약 생성시 예외가 발생한다.")
    void checkReservationDateTimeIsFuture_Failure() {
        ReservationSaveRequest request = new ReservationSaveRequest(
                LocalDate.now().minusDays(1L), 2L, 2L);
        Member member = new Member(
                new MemberName("capy"),
                new MemberEmail("test@naver.com"),
                new MemberPassword("1234"), Role.USER);

        assertThatThrownBy(() -> reservationCreateService.create(request, member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지나간 날짜와 시간에 대한 예약 생성은 불가능합니다.");
    }
}
