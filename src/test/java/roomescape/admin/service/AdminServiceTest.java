package roomescape.admin.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.theme.domain.Theme;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.User;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.dto.WaitingResponseDto;
import roomescape.waiting.fixture.WaitingFixture;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
        {MemberTestDataConfig.class, ThemeTestDataConfig.class, ReservationTimeTestDataConfig.class,})
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class AdminServiceTest {

    @Autowired
    private AdminService adminService;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberTestDataConfig memberTestDataConfig;
    @Autowired
    private ThemeTestDataConfig themeTestDataConfig;
    @Autowired
    private ReservationTimeTestDataConfig timeTestDataConfig;

    @Nested
    @DisplayName("모든 예약 대기 목록 조회 기능")
    class findAllWaitings {

        private final User savedMember = memberTestDataConfig.getSavedUser();
        private final Theme savedTheme = themeTestDataConfig.getSavedTheme();
        private final ReservationTime savedTime = timeTestDataConfig.getSavedReservationTime();

        private Waiting createAndSaveWaiting(LocalDate date, ReservationTime time, Theme theme) {
            Waiting waiting = createWaiting(date, time, theme, savedMember);
            return waitingRepository.save(waiting);
        }

        private Waiting createWaiting(LocalDate date, ReservationTime time, Theme theme, User member) {
            return WaitingFixture.create(date, time, theme, member);
        }

        @DisplayName("예약 대기 목록이 여러 개 일 때 List 형태로 반환된다")
        @Test
        void findAll_success_whenWaitingsExists() {
            // given
            Reservation reservation1 = ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(1),
                    savedTime,
                    savedTheme, savedMember);
            Reservation reservation2 = ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(2),
                    savedTime,
                    savedTheme, savedMember);
            Reservation savedReservation1 = reservationRepository.save(reservation1);
            Reservation savedReservation2 = reservationRepository.save(reservation2);

            createAndSaveWaiting(savedReservation1.getDate(), savedReservation1.getReservationTime(), savedReservation1.getTheme());
            createAndSaveWaiting(savedReservation2.getDate(), savedReservation2.getReservationTime(),
                    savedReservation2.getTheme());

            // when
            List<WaitingResponseDto> responseDtos = adminService.findAllWaitings();

            // then
            assertSoftly(s -> {
                        s.assertThat(responseDtos).hasSize(2);
                        responseDtos.forEach(resDto ->
                                s.assertThat(resDto.id()).isNotNull());
                    }
            );
        }

        @DisplayName("예약 대기 목록이 없더라도 예외 없이 빈 리스트를 반환한다")
        @Test
        void findAll_success_whenNoWaitings() {
            // given
            waitingRepository.deleteAll();

            // when
            List<WaitingResponseDto> responseDtos = adminService.findAllWaitings();

            // then
            Assertions.assertThat(responseDtos).hasSize(0);
        }
    }
}
