package roomescape.time.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.exception.ConstraintConflictException;
import roomescape.common.exception.DuplicateException;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.application.dto.CreateReservationTimeServiceRequest;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReservationTimeCommandServiceImplTest {

    @Autowired
    private ReservationTimeCommandServiceImpl reservationTimeCommandService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("예약 시간을 생성할 수 있다")
    void createReservationTime() {
        // given
        final CreateReservationTimeServiceRequest request = new CreateReservationTimeServiceRequest(LocalTime.of(12, 30));

        // when
        final ReservationTime reservationTime = reservationTimeCommandService.create(request);

        // then
        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(12, 30));
        assertThat(reservationTimeRepository.findById(reservationTime.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("예약 시간을 삭제할 수 있다")
    void deleteReservationTime() {
        // given
        final ReservationTime saved =
                reservationTimeRepository.save(
                        ReservationTime.withoutId(LocalTime.of(14, 0)));
        final ReservationTimeId id = saved.getId();

        // when
        reservationTimeCommandService.delete(id);

        // then
        assertThat(reservationTimeRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하려 하면 예외가 발생한다")
    void deleteNonExistentReservationTime() {
        // given
        final ReservationTimeId id = ReservationTimeId.from(-1L);

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandService.delete(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[RESERVATION_TIME] not found. params={ReservationTimeId=ReservationTimeId(-1)}");
    }

    @Test
    @DisplayName("참조 중인 예약 시간을 삭제하려 하면 예외가 발생한다")
    void deleteRefReservationTime() {
        // given
        final ReservationTime savedTime =
                reservationTimeRepository.save(ReservationTime.withoutId(
                        LocalTime.of(14, 0)));

        final Theme theme = themeRepository.save(Theme.withoutId(
                ThemeName.from("공포"),
                ThemeDescription.from("지구별 방탈출 최고"),
                ThemeThumbnail.from("www.making.com")));

        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));

        final Reservation reservation = reservationRepository.save(Reservation.withoutId(
                user.getId(),
                ReservationDate.from(LocalDate.now().plusDays(1L)),
                savedTime,
                theme
        ));

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandService.delete(savedTime.getId()))
                .isInstanceOf(ConstraintConflictException.class)
                .hasMessage("[RESERVATION_TIME] is referenced by another entity. " +
                        "params={ReservationTimeId=ReservationTimeId(%s)}".formatted(reservation.getTime().getId().getValue()));
    }

    @Test
    @DisplayName("추가하려는 시간이 이미 존재한다면, 예외가 발생한다")
    void existsTime() {
        // given

        final LocalTime time = LocalTime.of(14, 0);
        reservationTimeRepository.save(ReservationTime.withoutId(time));

        final CreateReservationTimeServiceRequest sameTimeRequest = new CreateReservationTimeServiceRequest(time);

        // when
        // then
        assertThatThrownBy(() -> reservationTimeCommandService.create(sameTimeRequest))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("RESERVATION_TIME already exists. params={LocalTime=14:00}");
    }

}
