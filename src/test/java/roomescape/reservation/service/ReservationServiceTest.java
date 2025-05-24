package roomescape.reservation.service;

import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import java.time.LocalDate;
import java.time.LocalTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.exception.ConflictException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.dto.ReservationInfo;
import roomescape.reservation.domain.dto.ReservationRequestDto;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.fixture.ReservationTimeFixture;
import roomescape.reservationtime.repository.ReservationTimeRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;
import roomescape.user.domain.Role;
import roomescape.user.domain.User;
import roomescape.user.fixture.UserFixture;
import roomescape.user.repository.UserRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.fixture.WaitingFixture;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class ReservationServiceTest {

    @Autowired
    private ReservationService service;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private UserRepository userRepository;

    private ReservationTime savedReservationTime;
    private Theme savedTheme;
    private User savedUser;
    @Autowired
    private ReservationService reservationService;

    @BeforeEach
    void beforeEach() {
        savedReservationTime = reservationTimeRepository.save(ReservationTimeFixture.create(LocalTime.of(14, 14)));
        savedTheme = themeRepository.save(new Theme("name1", "dd", "tt"));
        savedUser = userRepository.save(UserFixture.create(Role.ROLE_MEMBER, "n1", "e1", "p1"));
    }

    private Reservation createDefaultReservationByBookedStatus(LocalDate date) {
        return ReservationFixture.createByBookedStatus(date, savedReservationTime, savedTheme, savedUser);
    }

    private ReservationTime createAndSaveReservationTime(LocalTime time) {
        ReservationTime reservationTime = ReservationTimeFixture.create(time);
        return reservationTimeRepository.save(reservationTime);
    }

    private Reservation createReservation(int plusDays, ReservationTime time) {
        LocalDate date = LocalDate.now().plusDays(plusDays);
        return ReservationFixture.createByBookedStatus(date, time, savedTheme, savedUser);
    }

    private ReservationRequestDto createRequestDto(int plusDays, Long timeId, Long themeId) {
        LocalDate date = LocalDate.now().plusDays(plusDays);

        return ReservationFixture.createRequestDto(date, timeId, themeId);
    }

    @Nested
    @DisplayName("예약 추가하기 기능")
    class add {

        @DisplayName("이미 같은 시간에 예약이 존재한다면 예외 처리한다.")
        @Test
        void add_failure_byDuplicateDateTime() {
            // given
            ReservationTime reservationTime1 = createAndSaveReservationTime(LocalTime.of(17, 33));
            Reservation reservation1 = createReservation(1, reservationTime1);

            ReservationTime reservationTime2 = createAndSaveReservationTime(LocalTime.of(21, 37));
            Reservation reservation2 = createReservation(2, reservationTime2);

            reservationRepository.save(reservation1);
            reservationRepository.save(reservation2);

            // when & then
            LocalDate duplicateDate = reservation1.getDate();
            Long duplicateReservationTimeId = reservationTime1.getId();
            ReservationRequestDto requestDto = ReservationFixture.createRequestDto(duplicateDate,
                    duplicateReservationTimeId, savedTheme.getId());

            Assertions.assertThatThrownBy(
                    () -> service.add(requestDto, savedUser)
            ).isInstanceOf(ConflictException.class);
        }

        @DisplayName("시간이 같아도 날짜가 다르다면 예약이 가능하다.")
        @Test
        void add_success_withDifferenceDateAndSameTime() {
            // given
            ReservationTime reservationTime1 = createAndSaveReservationTime(LocalTime.of(11, 33));
            Reservation reservation1 = createReservation(1, reservationTime1);

            ReservationTime reservationTime2 = createAndSaveReservationTime(LocalTime.of(22, 44));
            Reservation reservation2 = createReservation(2, reservationTime2);

            reservationRepository.save(reservation1);
            reservationRepository.save(reservation2);

            // when & then
            Long duplicateReservationTimeId = reservationTime1.getId();
            ReservationRequestDto requestDto = createRequestDto(3, duplicateReservationTimeId,
                    savedTheme.getId());

            Assertions.assertThatCode(
                    () -> service.add(requestDto, savedUser)
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("예약 삭제 기능")
    class cancelReservationAndReturnInfo {

        @Nested
        @DisplayName("예약 삭제 성공 기능")
        class cancelReservationAndReturnInfo_success {

            @DisplayName("존재하는 예약의 id로 요청을 하면 예약은 삭제된다")
            @Test
            void cancelReservationAndReturnInfo_success_byExistingReservationId() {
                // given
                Reservation reservation = createDefaultReservationByBookedStatus(
                        LocalDate.now().plusDays(1));
                Reservation savedReservation = reservationRepository.save(reservation);

                // before then
                Assertions.assertThat(reservationRepository.findAll()).hasSize(1);

                // when
                reservationService.cancelReservationAndReturnInfo(savedReservation.getId());

                // then
                Assertions.assertThat(reservationRepository.findAll()).hasSize(0);
            }
        }
    }

    @Nested
    @DisplayName("대기를 예약으로 승인 기능")
    class approveWaiting {

        private Waiting createAndSaveWaitingByReservation(Reservation reservation) {
            Waiting waiting = createWaitingByReservation(reservation);
            return waitingRepository.save(waiting);
        }

        private Waiting createWaitingByReservation(Reservation reservation) {
            return WaitingFixture.create(reservation.getDate(),
                    reservation.getReservationTime(),
                    reservation.getTheme(),
                    reservation.getUser());
        }

        @DisplayName("예약의 정보로 요청하면 대기가 예약으로 승인된다")
        @Test
        void cancelReservationAndReturnInfoAndApproveWaiting_success_byReservationInfo() {
            // given
            Reservation reservation = createDefaultReservationByBookedStatus(
                    LocalDate.now().plusDays(1));
            Reservation savedReservation = reservationRepository.save(reservation);

            Waiting savedWaiting1 = createAndSaveWaitingByReservation(reservation);
            Waiting savedWaiting2 = createAndSaveWaitingByReservation(reservation);

            // before then
            Assertions.assertThat(reservationRepository.findAll()).hasSize(1);
            Assertions.assertThat(waitingRepository.findAll()).hasSize(2);

            // when
            ReservationInfo reservationInfo = reservationService.cancelReservationAndReturnInfo(reservation.getId());
            reservationService.approveWaiting(reservationInfo);

            // then
            Assertions.assertThat(reservationRepository.findAll()).hasSize(1);
            Assertions.assertThat(waitingRepository.findAll()).hasSize(1);
            Assertions.assertThat(savedWaiting1.getDate()).isEqualTo(reservation.getDate());
        }
    }
}
