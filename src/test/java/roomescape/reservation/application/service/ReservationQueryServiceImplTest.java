package roomescape.reservation.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.sign.password.Password;
import roomescape.common.domain.Email;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationRepository;
import roomescape.reservation.ui.ReservationSearchRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;
import roomescape.theme.domain.ThemeThumbnail;
import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeRepository;
import roomescape.user.application.service.UserQueryService;
import roomescape.user.domain.User;
import roomescape.user.domain.UserName;
import roomescape.user.domain.UserRepository;
import roomescape.user.domain.UserRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@Transactional
class ReservationQueryServiceImplTest {

    @Autowired
    private ReservationQueryServiceImpl reservationQueryService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserQueryService userQueryService;

    @Test
    @DisplayName("예약을 조회할 수 있다")
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
    @DisplayName("유저 아이디로 예약을 조회할 수 있다")
    void getReservationWithUserId() {
        // given
        final User me = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("password"),
                        UserRole.NORMAL
                )
        );

        final User notMe = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("password"),
                        UserRole.NORMAL
                )
        );

        final ReservationTime time = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(10, 0)));

        final Theme theme1 = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포1"),
                        ThemeDescription.from("지구별 방탈출 최고1"),
                        ThemeThumbnail.from("www.making.com")));

        final Theme theme2 = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포2"),
                        ThemeDescription.from("지구별 방탈출 최고2"),
                        ThemeThumbnail.from("www.making.com")));

        final Theme theme3 = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포3"),
                        ThemeDescription.from("지구별 방탈출 최고3"),
                        ThemeThumbnail.from("www.making.com")));

        final Reservation reservation1 = reservationRepository.save(
                Reservation.withoutId(
                        me.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(1)),
                        time,
                        theme1
                )
        );

        final Reservation reservation2 = reservationRepository.save(
                Reservation.withoutId(
                        notMe.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(1)),
                        time,
                        theme2
                )
        );

        final Reservation reservation3 = reservationRepository.save(
                Reservation.withoutId(
                        me.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(1)),
                        time,
                        theme3
                )
        );

        // when
        final List<Reservation> reservations = reservationQueryService.getAllByUserId(me.getId());

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).contains(reservation1, reservation3);
    }

    @Test
    @DisplayName("테마 아이디, 유저 아이디, 시작/끝 날짜 조건으로 검색할 수 있다")
    void getByParams() {
        // given
        final User user = userRepository.save(
                User.withoutId(
                        UserName.from("강산"),
                        Email.from("email@email.com"),
                        Password.fromEncoded("password"),
                        UserRole.NORMAL
                )
        );

        final ReservationTime time = reservationTimeRepository.save(
                ReservationTime.withoutId(
                        LocalTime.of(10, 0)));

        final Theme theme = themeRepository.save(
                Theme.withoutId(ThemeName.from("공포1"),
                        ThemeDescription.from("지구별 방탈출 최고1"),
                        ThemeThumbnail.from("www.making.com")));

        final Reservation reservation = reservationRepository.save(
                Reservation.withoutId(
                        user.getId(),
                        ReservationDate.from(LocalDate.now().plusDays(1)),
                        time,
                        theme
                )
        );

        // when
        final List<Reservation> foundByThemeIdAndUserIdAndDateToFrom = reservationQueryService.getByParams(
                new ReservationSearchRequest(
                        theme.getId(),
                        user.getId(),
                        ReservationDate.from(LocalDate.now()),
                        ReservationDate.from(LocalDate.now().plusDays(1))
                )
        );

        final List<Reservation> foundByThemeIdAndUserIdAndDateTo = reservationQueryService.getByParams(
                new ReservationSearchRequest(
                        theme.getId(),
                        user.getId(),
                        ReservationDate.from(LocalDate.now()),
                        null
                )
        );

        final List<Reservation> foundByThemeIdAndUserId = reservationQueryService.getByParams(
                new ReservationSearchRequest(
                        theme.getId(),
                        user.getId(),
                        null,
                        null
                )
        );

        final List<Reservation> foundByThemeId = reservationQueryService.getByParams(
                new ReservationSearchRequest(
                        theme.getId(),
                        null,
                        null,
                        null
                )
        );

        final List<Reservation> noFilter = reservationQueryService.getByParams(
                new ReservationSearchRequest(
                        null,
                        null,
                        null,
                        null
                )
        );

        final List<Reservation> wrongFilter = reservationQueryService.getByParams(
                new ReservationSearchRequest(
                        null,
                        null,
                        ReservationDate.from(LocalDate.now().plusMonths(1)),
                        null
                )
        );

        // then
        assertAll(() -> {
            assertThat(foundByThemeIdAndUserIdAndDateToFrom.contains(reservation)).isTrue();
            assertThat(foundByThemeIdAndUserIdAndDateTo.contains(reservation)).isTrue();
            assertThat(foundByThemeIdAndUserId.contains(reservation)).isTrue();
            assertThat(foundByThemeId.contains(reservation)).isTrue();
            assertThat(noFilter.contains(reservation)).isTrue();
            assertThat(wrongFilter.isEmpty()).isTrue();
        });
    }
}
