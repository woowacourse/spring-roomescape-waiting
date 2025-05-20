package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.request.MemberRegisterRequest;
import roomescape.dto.request.ReservationThemeRequest;
import roomescape.dto.request.ReservationTimeRequest;
import roomescape.dto.response.MemberRegisterResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationThemeResponse;
import roomescape.dto.response.ReservationTimeResponse;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReservationThemeServiceTest {

    @Autowired
    private ReservationThemeService reservationThemeService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("모든 테마를 다 가져온다.")

    void findReservationThemesTest() {
        //given
        final ReservationThemeRequest reservationThemeRequest = new ReservationThemeRequest("test", "test", "test");
        reservationThemeService.addReservationTheme(reservationThemeRequest);

        //when
        final List<ReservationThemeResponse> expected = reservationThemeService.findReservationThemes();

        //then
        assertThat(expected).hasSize(1);

    }

    @Test
    void findPopularThemes() {
    }

    @Test
    @DisplayName("예약 테마를 저장한다.")
    void saveTest() {
        //given
        final ReservationThemeRequest reservationThemeRequest = new ReservationThemeRequest("test", "test", "test");

        //when
        final ReservationThemeResponse expected = reservationThemeService.addReservationTheme(
                reservationThemeRequest);

        //then
        assertAll(
                () -> assertThat(expected.id()).isEqualTo(1L),
                () -> assertThat(expected.name()).isEqualTo("test"),
                () -> assertThat(expected.description()).isEqualTo("test"),
                () -> assertThat(expected.thumbnail()).isEqualTo("test")
        );

    }

    @Test
    @DisplayName("존재하지 않는 예약 테마를 삭제하여 예외가 발생한다.")
    void deleteTest1() {
        //given
        final long id = 1L;

        //when & then
        assertThatThrownBy(() -> reservationThemeService.removeReservationTheme(id))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    @DisplayName("존재하는 예약 테마를 삭제하여 예외가 발생 하지 않는다.")
    void deleteTest2() {
        //given
        final ReservationThemeRequest reservationThemeRequest = new ReservationThemeRequest("test", "test", "test");
        final ReservationThemeResponse saved = reservationThemeService.addReservationTheme(
                reservationThemeRequest);

        //when & then
        assertThatCode(() -> reservationThemeService.removeReservationTheme(saved.id())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("참조가 있는 테마를 삭제하려고 할 시 예외가 발생한다.")
    void deleteTest3() {
        // given
        final MemberRegisterResponse member = memberService.addMember(
                new MemberRegisterRequest("test", "test", "test"));
        final ReservationThemeResponse theme = reservationThemeService.addReservationTheme(
                new ReservationThemeRequest("test", "test", "test"));
        final ReservationTimeResponse time = reservationTimeService.addReservationTime(
                new ReservationTimeRequest(LocalTime.now()));
        final ReservationResponse reservation = reservationService.addReservationForAdmin(
                new AdminReservationRequest(member.id(), LocalDate.now().plusDays(1), theme.id(), time.id())
        );

        // when, then
        assertThatThrownBy(() -> reservationThemeService.removeReservationTheme(theme.id()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
