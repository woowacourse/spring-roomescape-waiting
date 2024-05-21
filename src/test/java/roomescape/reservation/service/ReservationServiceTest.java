package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.HOUR_10;
import static roomescape.util.Fixture.JOJO_EMAIL;
import static roomescape.util.Fixture.JOJO_NAME;
import static roomescape.util.Fixture.KAKI_EMAIL;
import static roomescape.util.Fixture.KAKI_NAME;
import static roomescape.util.Fixture.KAKI_PASSWORD;
import static roomescape.util.Fixture.THUMBNAIL;
import static roomescape.util.Fixture.TODAY;

import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.auth.domain.Role;
import roomescape.auth.dto.LoginMember;
import roomescape.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
import roomescape.reservation.dto.ReservationSaveRequest;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ReservationServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationService reservationService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
    }

    @DisplayName("존재하지 않는 예약 시간에 예약을 하면 예외가 발생한다.")
    @Test
    void notExistReservationTimeIdExceptionTest() {
        themeRepository.save(
                new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        LoginMember loginMember = new LoginMember(1L, Role.USER, JOJO_NAME, JOJO_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, 1L, 1L);

        assertThatThrownBy(() -> reservationService.saveReservationSuccess(reservationSaveRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 성공 상태의 중복된 예약이 있다면 예외가 발생한다.")
    @Test
    void validateDuplicatedReservationSuccess() {
        Theme theme = themeRepository.save(new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        LoginMember loginMember = new LoginMember(1L, Role.USER, JOJO_NAME, JOJO_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, 1L, 1L);
        reservationService.saveReservationSuccess(reservationSaveRequest, loginMember);

        ReservationSaveRequest duplicateRequest = new ReservationSaveRequest(TODAY, theme.getId(), reservationTime.getId());
        assertThatThrownBy(() -> reservationService.saveReservationSuccess(duplicateRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("동일한 회원이 예약 대기 상태의 중복된 예약을 할 경우 예외가 발생한다.")
    @Test
    void validateDuplicatedReservationWaiting() {
        themeRepository.save(new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        LoginMember loginMember = new LoginMember(1L, Role.USER, KAKI_NAME, KAKI_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, 1L, 1L);
        reservationService.saveReservationWaiting(reservationSaveRequest, loginMember);

        assertThatThrownBy(() -> reservationService.saveReservationWaiting(reservationSaveRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("동일한 회원이 예약 후 해당 예약에 연달아 대기를 걸 경우 예외가 발생한다.")
    @Test
    void validateReservationWaitingAfterReservation() {
        themeRepository.save(new Theme(new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL));

        reservationTimeRepository.save(new ReservationTime(LocalTime.parse(HOUR_10)));

        memberRepository.save(Member.createMemberByUserRole(new MemberName(KAKI_NAME), KAKI_EMAIL, KAKI_PASSWORD));

        LoginMember loginMember = new LoginMember(1L, Role.USER, KAKI_NAME, KAKI_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(TODAY, 1L, 1L);
        reservationService.saveReservationSuccess(reservationSaveRequest, loginMember);

        assertThatThrownBy(() -> reservationService.saveReservationWaiting(reservationSaveRequest, loginMember))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    @Test
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> reservationService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

