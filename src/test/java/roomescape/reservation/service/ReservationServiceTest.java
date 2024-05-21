package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.JOJO_EMAIL;
import static roomescape.util.Fixture.JOJO_NAME;
import static roomescape.util.Fixture.LOGIN_JOJO;
import static roomescape.util.Fixture.MEMBER_JOJO;
import static roomescape.util.Fixture.RESERVATION_TIME_10_00;
import static roomescape.util.Fixture.THUMBNAIL;
import static roomescape.util.Fixture.TODAY;

import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.auth.domain.Role;
import roomescape.auth.dto.LoginMember;
import roomescape.config.DatabaseCleaner;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Description;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Status;
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

        LoginMember loginMember = new LoginMember(1L, Role.MEMBER, JOJO_NAME, JOJO_EMAIL);
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(LocalDate.now(), 1L, 1L);

        assertThatThrownBy(() -> reservationService.save(reservationSaveRequest, loginMember, Status.SUCCESS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 시, 중복된 예약이 있다면 예외가 발생한다.")
    @Test
    void duplicateReservationExceptionTest() {
        // given
        Theme theme = themeRepository.save(HORROR_THEME);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10_00);
        memberRepository.save(MEMBER_JOJO);

        // when
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(
                TODAY,
                theme.getId(),
                reservationTime.getId()
        );
        reservationService.save(reservationSaveRequest, LOGIN_JOJO, Status.SUCCESS);

        // then
        assertThatThrownBy(() -> reservationService.save(reservationSaveRequest, LOGIN_JOJO, Status.SUCCESS))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 대기 시, 저장된 예약이 없다면 예외가 발생한다.")
    @Test
    void waitingReservationWithNotSavedReservationExceptionTest() {
        // given
        Theme theme = themeRepository.save(HORROR_THEME);
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10_00);
        memberRepository.save(MEMBER_JOJO);

        // when
        ReservationSaveRequest reservationSaveRequest = new ReservationSaveRequest(
                TODAY,
                theme.getId(),
                reservationTime.getId()
        );

        // then
        assertThatThrownBy(() -> reservationService.save(reservationSaveRequest, LOGIN_JOJO, Status.WAIT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 아이디로 조회 시 존재하지 않는 아이디면 예외가 발생한다.")
    @Test
    void findByIdExceptionTest() {
        assertThatThrownBy(() -> reservationService.findById(1L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

