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
import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationRepository;
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

    @Test
    @DisplayName("예약을 생성할 수 있다")
    void createAndFindReservation() {
        // given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(10, 0)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));

        final CreateReservationServiceRequest requestDto = new CreateReservationServiceRequest(
                user.getId(),
                ReservationDate.from(LocalDate.of(2025, 8, 5)),
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
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(10, 0)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));

        final Reservation savedReservation = reservationCommandService.create(
                new CreateReservationServiceRequest(
                        user.getId(),
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime.getId(),
                        theme.getId()
                ));

        // when
        // then
        assertThatThrownBy(() -> reservationCommandService.create(
                new CreateReservationServiceRequest(
                        user.getId(),
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        reservationTime.getId(),
                        theme.getId())))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContainingAll(
                        "RESERVATION already exists.",
                        "params={ReservationDate=ReservationDate(value=",
                        "ReservationTimeId=ReservationTimeId(",
                        "ThemeId=ThemeId("
                );
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다")
    void deleteReservation() {
        // given
        final ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(10, 0)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(
                        ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));

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

        final ReservationTime validReservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        now.toLocalTime().plusNanos(1)));

        final ReservationTime pastReservationTime = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        now.toLocalTime().minusNanos(1)));

        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));

        final Theme theme = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포"),
                        ThemeDescription.from("지구별 방탈출 최고"),
                        ThemeThumbnail.from("www.making.com")));

        final CreateReservationServiceRequest pastDateReservationRequest =
                new CreateReservationServiceRequest(
                        user.getId(),
                        ReservationDate.from(now.toLocalDate().minusDays(1)),
                        validReservationTime.getId(),
                        theme.getId()
                );

        final CreateReservationServiceRequest pastTimeReservationRequest =
                new CreateReservationServiceRequest(
                        user.getId(),
                        ReservationDate.from(now.toLocalDate()),
                        pastReservationTime.getId(),
                        theme.getId()
                );

        // when
        // then
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
        final ReservationId id = ReservationId.from(-1L);

        // when
        // then
        assertThatThrownBy(() -> reservationCommandService.delete(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[RESERVATION] not found. params={ReservationId=ReservationId(-1)}");
    }
}
