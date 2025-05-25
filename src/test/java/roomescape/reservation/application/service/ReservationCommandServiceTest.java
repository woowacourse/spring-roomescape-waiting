package roomescape.reservation.application.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.exception.NotFoundException;
import roomescape.common.time.TimeProvider;
import roomescape.reservation.domain.BookedStatus;
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
import roomescape.timeslot.domain.ReservationTime;
import roomescape.timeslot.domain.TimeSlot;
import roomescape.timeslot.domain.TimeSlotRepository;
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
class ReservationCommandServiceTest {

    @Autowired
    private ReservationCommandService reservationCommandService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

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

        // when
        final Reservation reservation = reservationCommandService.create(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.of(2025, 8, 5)),
                        ReservationTime.from(LocalTime.of(10, 0)),
                        theme
                ));

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
    @DisplayName("슬롯의 첫 번쨰 예약은 곧바로 승인된다.")
    void existsReservation() {
        // given
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

        final User somebody = userRepository.save(
                User.withoutId(
                        UserName.from("다른사람"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));

        final ReservationDate date = ReservationDate.from(LocalDate.of(2025, 8, 5));
        final ReservationTime time = ReservationTime.from(LocalTime.of(10, 0));

        final Reservation reservation1 = Reservation.withoutId(
                user.getId(),
                date,
                time,
                theme
        );

        final Reservation reservation2 = Reservation.withoutId(
                somebody.getId(),
                date,
                time,
                theme
        );

        reservationCommandService.create(reservation1);
        reservationCommandService.create(reservation2);

        // when
        // then
        assertThat(reservation1.getStatus() == BookedStatus.APPROVED).isTrue();
        assertThat(reservation2.getStatus() == BookedStatus.WAITING).isTrue();
    }

    @Test
    @DisplayName("예약을 삭제할 수 있다")
    void deleteReservation() {
        // given
        final TimeSlot timeSlot = timeSlotRepository.save(
                TimeSlot.withoutId(
                        ReservationTime.from(LocalTime.of(10, 0))));

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
                        timeSlot.getStartAt(),
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

        final TimeSlot validTimeSlot = timeSlotRepository.save(
                TimeSlot.withoutId(
                        ReservationTime.from(now.toLocalTime().plusNanos(1))));

        final TimeSlot pastTimeSlot = timeSlotRepository.save(
                TimeSlot.withoutId(
                        ReservationTime.from(now.toLocalTime().minusNanos(1))));

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

        final Reservation pastDateReservationRequest =
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(now.toLocalDate().minusDays(1)),
                        validTimeSlot.getStartAt(),
                        theme
                );

        final Reservation pastTimeReservationRequest =
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(now.toLocalDate()),
                        pastTimeSlot.getStartAt(),
                        theme
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
