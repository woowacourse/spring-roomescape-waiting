package roomescape.user.service;

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
import roomescape.member.domain.dto.ReservationWithStateDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.fixture.ReservationFixture;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservationtime.ReservationTimeTestDataConfig;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.ThemeTestDataConfig;
import roomescape.theme.domain.Theme;
import roomescape.user.MemberTestDataConfig;
import roomescape.user.domain.Role;
import roomescape.user.domain.User;
import roomescape.user.exception.NotFoundUserException;
import roomescape.user.exception.UserForbiddenException;
import roomescape.user.fixture.UserFixture;
import roomescape.user.repository.UserRepository;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.exception.NotFoundWaitingException;
import roomescape.waiting.fixture.WaitingFixture;
import roomescape.waiting.repository.WaitingRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes =
        {MemberTestDataConfig.class, ThemeTestDataConfig.class, ReservationTimeTestDataConfig.class,})
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
class UserServiceTest {

    @Autowired
    private UserService service;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private MemberTestDataConfig memberTestDataConfig;
    @Autowired
    private ThemeTestDataConfig themeTestDataConfig;
    @Autowired
    private ReservationTimeTestDataConfig timeTestDataConfig;
    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("유저에 예약 리스트를 조회하는 기능")
    class findAllReservationByMember {

        private final User savedMember = memberTestDataConfig.getSavedUser();
        private final Theme savedTheme = themeTestDataConfig.getSavedTheme();
        private final ReservationTime savedTime = timeTestDataConfig.getSavedReservationTime();

        @DisplayName("유저의 예약 리스트가 여러개 존재할 때 예약 리스트를 모두 가져올 수 있다.")
        @Test
        void findAllReservationByMember_success_bySomeReservations() {
            // given
            List<Reservation> reservations = List.of(ReservationFixture.createByBookedStatus(
                            LocalDate.now().plusDays(1), savedTime, savedTheme, savedMember),
                    ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(2), savedTime, savedTheme, savedMember));
            List<Reservation> savedReservations = reservationRepository.saveAll(reservations);

            // when
            List<ReservationWithStateDto> allReservationByMember = service.findAllReservationByMember(savedMember);

            // then
            Assertions.assertThat(allReservationByMember).hasSize(2);
        }

        @DisplayName("유저의 예약 리스트가 빈 리스트여도 예외가 터지지 않는다.")
        @Test
        void findAllReservationByMember_success_byNonExistedReservation() {
            // given
            // when
            List<ReservationWithStateDto> allReservationByMember = service.findAllReservationByMember(savedMember);

            // then
            Assertions.assertThat(allReservationByMember).hasSize(0);
        }

        @DisplayName("유저를 찾지 못했을 때 예외가 발생한다. : NotFoundUserException")
        @Test
        void findAllReservationByMember_throwException_byInvalidMember() {
            // given
            User user = new User(Long.MAX_VALUE, Role.ROLE_MEMBER, "ustfarbmn1", "ustfarbme1", "ustfarbmp1");

            // when
            // then
            Assertions.assertThatThrownBy(
                    () -> service.findAllReservationByMember(user)
            ).isInstanceOf(NotFoundUserException.class);
        }
    }

    @Nested
    @DisplayName("예약 대기 삭제 기능")
    class deleteWaitingByMember {

        private final User savedMember = memberTestDataConfig.getSavedUser();

        private Waiting createAndSaveWaiting(LocalDate date, ReservationTime time, Theme theme) {
            Waiting waiting = createWaiting(date, time, theme);
            return waitingRepository.save(waiting);
        }

        private Waiting createWaiting(LocalDate date, ReservationTime time, Theme theme) {
            return WaitingFixture.create(date, time, theme, savedMember);
        }

        private Reservation createReservation(int plusDays, ReservationTime time, Theme theme) {
            return ReservationFixture.createByBookedStatus(LocalDate.now().plusDays(plusDays), time, theme,
                    savedMember);
        }

        @DisplayName("존재하는 예약 대기 id로 삭제를 요청하면 삭제가 된다.")
        @Test
        void deleteWaitingByMember_success_byExistingWaitingId() {
            // given
            Reservation reservation = createReservation(1,
                    timeTestDataConfig.getSavedReservationTime(),
                    themeTestDataConfig.getSavedTheme());
            Reservation savedReservation = reservationRepository.save(reservation);
            Waiting savedWaiting = createAndSaveWaiting(savedReservation.getDate(),
                    savedReservation.getReservationTime(),
                    savedReservation.getTheme());

            // beforeThen
            Assertions.assertThat(waitingRepository.findAll().size()).isEqualTo(1);

            // when

            // after then
            Assertions.assertThatCode(
                    () -> service.deleteWaitingByMember(savedWaiting.getId(), savedMember)
            ).doesNotThrowAnyException();
            Assertions.assertThat(waitingRepository.findAll().size()).isEqualTo(0);
        }

        @DisplayName("존재하지 않는 예약 대기 id로 삭제를 요청하면 예외가 발생한다. : NotFoundWaitingException")
        @Test
        void deleteWaitingByMember_throwException_byNonExistingWaitingId() {
            // given
            // when
            // then
            Assertions.assertThatThrownBy(
                    () -> service.deleteWaitingByMember(Long.MAX_VALUE, savedMember)
            ).isInstanceOf(NotFoundWaitingException.class);
        }

        @DisplayName("자신의 예약이 아닌 사람이 삭제를 요청하면 예외가 발생한다. : UserForbiddenException")
        @Test
        void deleteWaitingByMember_throwException_byNotWaitingMember() {
            // given
            Reservation reservation = createReservation(2,
                    timeTestDataConfig.getSavedReservationTime(),
                    themeTestDataConfig.getSavedTheme());
            Reservation savedReservation = reservationRepository.save(reservation);
            Waiting savedWaiting = createAndSaveWaiting(savedReservation.getDate(),
                    savedReservation.getReservationTime(),
                    savedReservation.getTheme());

            User anotherUser = UserFixture.create(Role.ROLE_MEMBER, "dummyn1", "dummye1", "dummyp1");
            userRepository.save(anotherUser);

            // when
            // then
            Assertions.assertThatThrownBy(
                    () -> service.deleteWaitingByMember(savedWaiting.getId(), anotherUser)
            ).isInstanceOf(UserForbiddenException.class);
        }
    }
}
