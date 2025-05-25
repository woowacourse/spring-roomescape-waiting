package roomescape.reservation.application.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.application.dto.CreateReservationServiceRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.domain.WaitingReservationRepository;
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
import java.time.LocalTime;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@Transactional
class WaitingReservationCommandServiceImplTest {

    @Autowired
    private WaitingReservationCommandService service;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private WaitingReservationRepository waitingReservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("예약이 존재할 때, 예약 대기를 추가한다")
    void createWaitingReservation() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme("공포", "지구별 방탈출 최고");
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
        final WaitingReservation waitingReservation = service.create(requestDto);

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
    @DisplayName("예약이 존재하지 않을 때 예약 대기를 추가할 경우, 예외가 발생한다")
    void createWaitingReservationDuplication() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme("공포", "지구별 방탈출 최고");
        final User user = createAndSaveUser();

        final CreateReservationServiceRequest requestDto = createReservationRequest(
                user.getId(),
                LocalDate.of(2025, 8, 5),
                reservationTime.getId(),
                theme.getId());

        // when
        assertThatThrownBy(() -> service.create(requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[RESERVATION] not found. " +
                        "params={ReservationDate=ReservationDate(value=2025-08-05), " +
                        "DomainTerm=RESERVATION_TIME, " +
                        "DomainTerm=THEME}");

    }

    @Test
    @DisplayName("예약 대기를 삭제할 수 있다")
    void deleteWaitingReservation() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme("공포", "지구별 방탈출 최고");
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
        service.delete(id);

        // then
        assertThat(waitingReservationRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("예약 대기 삭제 시 뒤의 순서들이 자동으로 업데이트된다")
    void deleteWaitingReservationUpdatesOrder() {
        //given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme("공포", "지구별 방탈출 최고");
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
        service.delete(waiting2.getId());

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

    private Theme createAndSaveTheme(String name, String description) {
        return themeRepository.save(
                Theme.withoutId(
                        ThemeName.from(name),
                        ThemeDescription.from(description),
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
