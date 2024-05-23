package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.Fixture.HORROR_THEME;
import static roomescape.Fixture.LOGIN_JOJO;
import static roomescape.Fixture.MEMBER_JOJO;
import static roomescape.Fixture.MEMBER_KAKI;
import static roomescape.Fixture.RESERVATION_TIME_10_00;
import static roomescape.Fixture.TODAY;
import static roomescape.Fixture.TOMORROW;
import static roomescape.reservation.domain.Status.SUCCESS;
import static roomescape.reservation.domain.Status.WAIT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.common.config.DatabaseCleaner;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class WaitingReservationServiceTest {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingReservationService waitingReservationService;

    @AfterEach
    void init() {
        databaseCleaner.cleanUp();
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
        assertThatThrownBy(() -> waitingReservationService.saveByLoginMember(reservationSaveRequest, LOGIN_JOJO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("예약 대기 승인 시, 확정된 예약이 없으면 예약 상태로 변경된다.")
    @Test
    void updateSuccessStatus() {
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10_00);
        Theme theme = themeRepository.save(HORROR_THEME);
        Member jojo = memberRepository.save(MEMBER_JOJO);

        Reservation waiting = reservationRepository.save(new Reservation(jojo, TOMORROW, theme, reservationTime, WAIT));
        waitingReservationService.updateSuccessStatus(waiting.getId());

        Reservation afterUpdate = reservationRepository.findById(waiting.getId()).get();

        assertThat(afterUpdate.getStatus()).isEqualTo(SUCCESS);
    }

    @DisplayName("예약 대기 승인 시, 확정된 예약이 있으면 예외가 발생한다.")
    @Test
    void updateSuccessStatusWithDuplicatedReservation() {
        ReservationTime reservationTime = reservationTimeRepository.save(RESERVATION_TIME_10_00);
        Theme theme = themeRepository.save(HORROR_THEME);
        Member jojo = memberRepository.save(MEMBER_JOJO);
        Member kaki = memberRepository.save(MEMBER_KAKI);

        reservationRepository.save(new Reservation(kaki, TOMORROW, theme, reservationTime, SUCCESS));
        Reservation waiting = reservationRepository.save(new Reservation(jojo, TOMORROW, theme, reservationTime, WAIT));

        assertThatThrownBy(() -> waitingReservationService.updateSuccessStatus(waiting.getId()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
