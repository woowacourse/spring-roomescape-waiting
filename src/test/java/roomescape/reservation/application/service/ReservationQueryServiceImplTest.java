package roomescape.reservation.application.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceRequest;
import roomescape.reservation.application.dto.AvailableReservationTimeServiceResponse;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Slf4j
@SpringBootTest
@Transactional
class ReservationQueryServiceImplTest {

    @Autowired
    private ReservationQueryServiceImpl reservationQueryService;

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
    @DisplayName("예약을 조회할 수 있다")
    void createAndFindReservation() {
        // given
        final ReservationTime reservationTime = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme = createAndSaveTheme("공포", "지구별 방탈출 최고");
        final User user = createAndSaveUser();

        final Reservation given1 = Reservation.withoutId(
                user.getId(),
                ReservationDate.from(LocalDate.now().plusDays(1)),
                reservationTime,
                theme);

        final Reservation given2 = Reservation.withoutId(
                user.getId(),
                ReservationDate.from(LocalDate.now().plusDays(1)),
                reservationTime,
                theme);

        final Reservation saved1 = reservationRepository.save(given1);
        final Reservation saved2 = reservationRepository.save(given2);

        // when
        final List<Reservation> reservations = reservationQueryService.getAll();

        // then
        assertThat(reservations).hasSize(2);
        final Reservation found1 = reservations.getFirst();
        final Reservation found2 = reservations.get(1);

        assertAll(() -> {
            assertThat(found1).isEqualTo(saved1);
            assertThat(found2).isEqualTo(saved2);
        });
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대한 예약 가능 여부가 포함된 시간 정보를 받을 수 있다")
    void getTimesWithAvailability() {
        // given
        final ReservationTime booked = createAndSaveReservationTime(LocalTime.of(10, 0));
        final ReservationTime unbooked = createAndSaveReservationTime(LocalTime.of(11, 0));
        final Theme theme = createAndSaveTheme("공포", "지구별 방탈출 최고");
        final User user = createAndSaveUser();

        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));

        final Reservation reservation = createAndSaveReservation(user.getId(), date, booked, theme);

        // when
        final List<AvailableReservationTimeServiceResponse> timesWithAvailability = reservationQueryService.getTimesWithAvailability(
                new AvailableReservationTimeServiceRequest(date, theme.getId()));

        // then
        assertAll(
                () -> {
                    assertThat(timesWithAvailability)
                            .hasSize(2);

                    assertThat(timesWithAvailability.stream()
                            .filter(AvailableReservationTimeServiceResponse::isBooked))
                            .hasSize(1);

                    assertThat(timesWithAvailability.stream()
                            .filter(AvailableReservationTimeServiceResponse::isBooked)
                            .map(AvailableReservationTimeServiceResponse::time)
                            .findFirst()
                            .orElseThrow()
                    ).isEqualTo(booked);
                });
    }

    @Test
    @DisplayName("유저 Id가 같은 모든 예약를 조회할 수 있다")
    void getAllReservationsByUserId() {
        // given
        final User user = createAndSaveUser();
        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        final ReservationTime time = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme1 = createAndSaveTheme("공포1", "지구별 방탈출 최고1");
        final Theme theme2 = createAndSaveTheme("공포2", "지구별 방탈출 최고2");

        Long userId = user.getId();
        final Reservation reservation1 = createAndSaveReservation(userId, date, time, theme1);
        final Reservation reservation2 = createAndSaveReservation(userId, date, time, theme2);

        // when
        List<Reservation> reservations = reservationQueryService.getAllReservationsByUserId(userId);

        // then
        assertThat(reservations.size()).isEqualTo(2);
        assertThat(reservations.getFirst().getUserId()).isEqualTo(userId);
        assertThat(reservations.getLast().getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("유저 Id가 같은 모든 예약 대기를 조회할 수 있다")
    void getAllWaitingByUserId() {
        // given
        final User user1 = createAndSaveUser();
        final User user2 = userRepository.save(
                User.withoutId(
                        UserName.from("강"),
                        Email.from("emailemail@email.com"),
                        Password.fromEncoded("1234"),
                        UserRole.NORMAL));
        final ReservationDate date = ReservationDate.from(LocalDate.now().plusDays(1));
        final ReservationTime time = createAndSaveReservationTime(LocalTime.of(10, 0));
        final Theme theme1 = createAndSaveTheme("공포2", "지구별 방탈출 최고2");
        final Theme theme2 = createAndSaveTheme("공포2", "지구별 방탈출 최고2");
        Long userId = user1.getId();

        final Reservation reservation1 = createAndSaveReservation(user2.getId(), date, time, theme1);
        final Reservation reservation2 = createAndSaveReservation(user2.getId(), date, time, theme2);
        final WaitingReservation waitingReservation1 = waitingReservationRepository.save(
                WaitingReservation.withoutId(userId, 1, date, time, theme1));
        final WaitingReservation waitingReservation2 = waitingReservationRepository.save(
                WaitingReservation.withoutId(userId, 1, date, time, theme2));
        // when
        List<WaitingReservation> reservations = reservationQueryService.getWaitingByUserId(userId);

        // then
        assertThat(reservations.size()).isEqualTo(2);
        assertThat(reservations.getFirst().getUserId()).isEqualTo(waitingReservation1.getUserId());
        assertThat(reservations.getFirst().getWaitingOrder()).isEqualTo(waitingReservation1.getWaitingOrder());
        assertThat(reservations.getLast().getUserId()).isEqualTo(waitingReservation2.getUserId());
        assertThat(reservations.getLast().getWaitingOrder()).isEqualTo(waitingReservation2.getWaitingOrder());
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

    private Reservation createAndSaveReservation(Long userId, ReservationDate date, ReservationTime time, Theme theme) {
        return reservationRepository.save(
                Reservation.withoutId(userId, date, time, theme));
    }
}
