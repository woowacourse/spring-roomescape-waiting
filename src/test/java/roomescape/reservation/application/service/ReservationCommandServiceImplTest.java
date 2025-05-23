package roomescape.reservation.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.time.TimeProvider;
import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.WaitingReservationRepository;
import roomescape.reservation.exception.PastDateReservationException;
import roomescape.reservation.exception.PastTimeReservationException;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class ReservationCommandServiceImplTest {

    @Autowired
    private ReservationCommandServiceImpl reservationCommandService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeProvider timeProvider;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Test
    @DisplayName("예약을 생성할 수 있다")
    void createAndFindReservation() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme();
        final User user = createAndSaveUser();

        final CreateReservationServiceRequest requestDto = createReservationRequest(
                user.getId(),
                LocalDate.of(2025, 8, 5),
                reservationTime.getId(),
                theme.getId());

        // when
        final Reservation reservation = reservationCommandService.create(requestDto);

        // then
        final Reservation found = reservationRepository.findById(reservation.getId())
                .orElseThrow(NoSuchElementException::new);

        assertThat(reservation).isEqualTo(found);
        assertThat(reservation.getId()).isEqualTo(found.getId());
        assertThat(reservation.getUserId()).isEqualTo(found.getUserId());
        assertThat(reservation.getDate()).isEqualTo(found.getDate());
        assertThat(reservation.getTime()).isEqualTo(found.getTime());
        assertThat(reservation.getTheme()).isEqualTo(found.getTheme());
    }

    @Test
    @DisplayName("중복된 예약을 생성할 수 없다.")
    void existsReservation() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme();
        final User user = createAndSaveUser();

        final CreateReservationServiceRequest requestDto = createReservationRequest(
                user.getId(),
                LocalDate.of(2025, 8, 5),
                reservationTime.getId(),
                theme.getId());

        final Reservation savedReservation = reservationCommandService.create(requestDto);

        // when & then
        assertThatThrownBy(() -> reservationCommandService.create(requestDto))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContainingAll(
                        "RESERVATION already exists.",
                        "params={ReservationDate=ReservationDate(value=",
                        "Long=",
                        "Long="
                );
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다")
    void deleteReservation() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme();
        final User user = createAndSaveUser();

        final Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime,
                        theme));

        // when
        reservationCommandService.delete(reservation.getId());

        // then
        assertThat(reservationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("지나간 날짜/시간에 대한 예약을 생성할 수 없다")
    void cannotCreatePastDateTimeReservation() {
        // given
        final LocalDateTime now = timeProvider.now();

        final ReservationTime validReservationTime = createAndSaveReservationTime(
                now.toLocalTime().plusNanos(1));
        final ReservationTime pastReservationTime = createAndSaveReservationTime(
                now.toLocalTime().minusNanos(1));

        final User user = createAndSaveUser();
        final Theme theme = createAndSaveTheme();

        final CreateReservationServiceRequest pastDateReservationRequest = createReservationRequest(
                user.getId(),
                now.toLocalDate().minusDays(1),
                validReservationTime.getId(),
                theme.getId());

        final CreateReservationServiceRequest pastTimeReservationRequest = createReservationRequest(
                user.getId(),
                now.toLocalDate(),
                pastReservationTime.getId(),
                theme.getId());

        // when & then
        assertAll(() -> {
            assertThatThrownBy(() -> reservationCommandService.create(pastDateReservationRequest))
                    .isInstanceOf(PastDateReservationException.class)
                    .hasMessageContaining("Attempted to reserve with past date.");

            assertThatThrownBy(() -> reservationCommandService.create(pastTimeReservationRequest))
                    .isInstanceOf(PastTimeReservationException.class)
                    .hasMessageContaining("Attempted to reserve with past time.");
        });
    }

    @Test
    @DisplayName("존재하지 않는 예약을 삭제하려 하면 예외가 발생한다")
    void deleteNonExistentReservation() {
        // given
        final Long id = -1L;

        // when & then
        assertThatThrownBy(() -> reservationCommandService.delete(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[RESERVATION] not found. params={Long=-1}");
    }

    @Test
    @DisplayName("예약이 존재할 때, 예약 대기를 추가한다")
    void createWaitingReservation() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme();
        final User user = createAndSaveUser();

        final CreateReservationServiceRequest requestDto = createReservationRequest(
                user.getId(),
                LocalDate.of(2025, 8, 5),
                reservationTime.getId(),
                theme.getId());

        final Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime,
                        theme));

        // when
        final WaitingReservation waitingReservation = reservationCommandService.createWaitingReservation(requestDto);

        // then
        final WaitingReservation found = waitingReservationRepository.findById(waitingReservation.getId())
                .orElseThrow(NoSuchElementException::new);

        assertThat(waitingReservation).isEqualTo(found);
        assertThat(waitingReservation.getId()).isEqualTo(found.getId());
        assertThat(waitingReservation.getUserId()).isEqualTo(found.getUserId());
        assertThat(waitingReservation.getDate()).isEqualTo(found.getDate());
        assertThat(waitingReservation.getTime()).isEqualTo(found.getTime());
        assertThat(waitingReservation.getTheme()).isEqualTo(found.getTheme());
    }

    @Test
    @DisplayName("중복된 예약 대기가 존재할 때, 예외가 발생한다")
    void createWaitingReservationDuplication() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme();
        final User user = createAndSaveUser();

        final CreateReservationServiceRequest requestDto = createReservationRequest(
                user.getId(),
                LocalDate.of(2025, 8, 5),
                reservationTime.getId(),
                theme.getId());

        final Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime,
                        theme));

        // when
        final WaitingReservation waitingReservation = reservationCommandService.createWaitingReservation(requestDto);
        assertThatThrownBy(() -> reservationCommandService.createWaitingReservation(requestDto))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContainingAll(
                        "RESERVATION_WAITING already exists.",
                        "params={ReservationDate=ReservationDate(value=",
                        "DomainTerm=THEME_ID, DomainTerm=RESERVATION_TIME_ID}"
                );

    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다")
    void deleteWaitingReservation() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme();
        final User user = createAndSaveUser();

        final WaitingReservation waitingReservation = waitingReservationRepository.save(
                WaitingReservation.withoutId(
                        user.getId(),
                        1,
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime,
                        theme
                ));

        // when
        Long id = waitingReservation.getId();
        reservationCommandService.deleteWaiting(id);

        // then
        assertThat(waitingReservationRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("예약 대기 삭제 시 뒤의 순서들이 자동으로 업데이트된다")
    void deleteWaitingReservationUpdatesOrder() {
        //given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme();
        final User user = createAndSaveUser();

        final WaitingReservation waiting1 = waitingReservationRepository.save(
                WaitingReservation.withoutId(
                        user.getId(),
                        1,
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime,
                        theme));

        final WaitingReservation waiting2 = waitingReservationRepository.save(
                WaitingReservation.withoutId(
                        user.getId(),
                        2,
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime,
                        theme));

        final WaitingReservation waiting3 = waitingReservationRepository.save(
                WaitingReservation.withoutId(
                        user.getId(),
                        3,
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime,
                        theme));

        final WaitingReservation waiting4 = waitingReservationRepository.save(
                WaitingReservation.withoutId(
                        user.getId(),
                        4,
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime,
                        theme));

        //when
        reservationCommandService.deleteWaiting(waiting2.getId());

        //then
        assertThat(waitingReservationRepository.findById(waiting2.getId())).isEmpty();

        final WaitingReservation updatedWaiting1 = waitingReservationRepository.findById(waiting1.getId())
                .orElseThrow(NoSuchElementException::new);
        assertThat(updatedWaiting1.getWaitingOrder()).isEqualTo(1);

        final WaitingReservation updatedWaiting3 = waitingReservationRepository.findById(waiting3.getId())
                .orElseThrow(NoSuchElementException::new);
        assertThat(updatedWaiting3.getWaitingOrder()).isEqualTo(2);

        final WaitingReservation updatedWaiting4 = waitingReservationRepository.findById(waiting4.getId())
                .orElseThrow(NoSuchElementException::new);
        assertThat(updatedWaiting4.getWaitingOrder()).isEqualTo(3);
    }

    // Helper 메서드들
    private ReservationTime createAndSaveReservationTime(LocalTime time) {
        return reservationTimeRepository.save(
                ReservationTime.withoutId(time));
    }

    private Theme createAndSaveTheme() {
        return themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));
    }

    private User createAndSaveUser() {
        return userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));
    }

    private CreateReservationServiceRequest createReservationRequest(Long userId, LocalDate date, Long timeId, Long themeId) {
        return new CreateReservationServiceRequest(
                userId,
                ReservationDate.from(date),
                timeId,
                themeId);
    }
}
