package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;
import static roomescape.test.fixture.DateFixture.TODAY;
import static roomescape.test.fixture.DateFixture.YESTERDAY;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.business.ReservationCreationContent;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.dto.response.UserProfileResponse;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.exception.local.NotFoundReservationException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.exception.local.NotFoundThemeException;
import roomescape.exception.local.NotFoundUserException;
import roomescape.exception.local.PastReservationCreationException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.UserRepository;

@DataJpaTest
class ReservationServiceTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private UserRepository userRepository;

    private ReservationService reservationService;

    private ReservationTime reservationTime;
    private Theme theme;
    private User member;

    @BeforeEach
    void setup() {
        reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                userRepository);

        reservationTime = entityManager.persist(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        theme = entityManager.persist(
                Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
        member = entityManager.persist(
                User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));
    }

    @DisplayName("모든 예약을 조회할 수 있다.")
    @Test
    void canFindAll() {
        // given
        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));

        entityManager.flush();

        // when
        List<ReservationResponse> allReservations = reservationService.findAllReservations();

        // then
        assertAll(
                () -> assertThat(allReservations).hasSize(3),
                () -> assertThat(allReservations)
                        .extracting(ReservationResponse::user)
                        .extracting(UserProfileResponse::id)
                        .containsExactly(member.getId(), member.getId(), member.getId())
        );
    }

    @DisplayName("모든 예약을 조회할 수 있다.")
    @Test
    void testMethodNameHere() {
        // given
        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));

        User otherMember = entityManager.persist(
                User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));

        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, reservationTime, theme, otherMember));

        entityManager.flush();

        // when
        List<ReservationResponse> allReservations = reservationService.findAllReservationsByMember(member.getId());

        // then
        assertAll(
                () -> assertThat(allReservations).hasSize(3),
                () -> assertThat(allReservations)
                        .extracting(ReservationResponse::user)
                        .extracting(UserProfileResponse::id)
                        .containsExactly(member.getId(), member.getId(), member.getId())
        );
    }

    @Nested
    @DisplayName("필터를 통해 예약을 조회할 수 있다")
    class findReservationsByFilter {

        @Test
        @DisplayName("필터 조건으로 특정 유저의 예약을 조회할 수 있다")
        void canFindReservationsByUserFilter() {
            // given
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));

            User otherMember = entityManager.persist(
                    User.createWithoutId(Role.ROLE_MEMBER, "다른회원", "otherMember@test.com", "password123"));

            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, theme, otherMember));
            entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, reservationTime, theme, otherMember));

            entityManager.flush();

            // when
            List<ReservationResponse> reservations = reservationService.findReservationsByFilter(
                    otherMember.getId(), theme.getId(), TODAY, TODAY.plusDays(7));

            // then
            assertAll(
                    () -> assertThat(reservations).hasSize(2),
                    () -> assertThat(reservations)
                            .extracting(ReservationResponse::user)
                            .extracting(UserProfileResponse::id)
                            .containsExactly(otherMember.getId(), otherMember.getId())
            );
        }

        @Test
        @DisplayName("필터 조건으로 특정 테마의 예약을 조회할 수 있다")
        void canFindReservationsByThemeFilter() {
            // given
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));

            Theme otherTheme = entityManager.persist(
                    Theme.createWithoutId("다른테마", "설명", "thumbnail.jpg"));

            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, otherTheme, member));
            entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, reservationTime, otherTheme, member));

            entityManager.flush();

            // when
            List<ReservationResponse> reservations = reservationService.findReservationsByFilter(
                    member.getId(), otherTheme.getId(), TODAY, TODAY.plusDays(7));

            // then
            assertAll(
                    () -> assertThat(reservations).hasSize(2),
                    () -> assertThat(reservations)
                            .extracting(ReservationResponse::theme)
                            .extracting(ThemeResponse::id)
                            .containsExactly(otherTheme.getId(), otherTheme.getId())
            );
        }

        @Test
        @DisplayName("필터 조건으로 특정 기간의 예약을 조회할 수 있다")
        void canFindReservationsByDateFilter() {
            // given
            entityManager.persist(
                    Reservation.createWithoutId(YESTERDAY, ReservationStatus.BOOKED, reservationTime, theme, member));
            entityManager.persist(
                    Reservation.createWithoutId(TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));
            entityManager.persist(
                    Reservation.createWithoutId(NEXT_DAY, ReservationStatus.BOOKED, reservationTime, theme, member));

            entityManager.flush();

            // when
            List<Reservation> reservations = reservationRepository.findReservationsByFilter(
                    member.getId(), theme.getId(), TODAY, NEXT_DAY);

            // then
            assertAll(
                    () -> assertThat(reservations).hasSize(2),
                    () -> assertThat(reservations)
                            .extracting(Reservation::getDate)
                            .containsExactlyInAnyOrder(TODAY, NEXT_DAY)
            );
        }
    }

    @Nested
    @DisplayName("예약을 추가할 수 있다.")
    class addReservation {

        @DisplayName("예약을 성공적으로 추가할 수 있다.")
        @Test
        void canAddReservation() {
            // given
            ReservationCreationContent creationContent =
                    new ReservationCreationContent(theme.getId(), NEXT_DAY, reservationTime.getId());

            // when
            ReservationResponse reservationResponse =
                    reservationService.addReservation(member.getId(), creationContent);

            // then
            Reservation expectedReservation = entityManager.find(Reservation.class, reservationResponse.id());
            assertAll(
                    () -> assertThat(reservationResponse.id()).isEqualTo(expectedReservation.getId()),
                    () -> assertThat(reservationResponse.user().id()).isEqualTo(member.getId()),
                    () -> assertThat(reservationResponse.date()).isEqualTo(creationContent.date()),
                    () -> assertThat(reservationResponse.theme().id()).isEqualTo(creationContent.themeId()),
                    () -> assertThat(reservationResponse.bookState()).isEqualTo(ReservationStatus.BOOKED.toString())
            );
        }

        @DisplayName("유저가 존재하지 않을 경우 예약을 추가할 수 없다.")
        @Test
        void cannotAddReservationByInvalidUser() {
            // given
            long wrongMemberId = member.getId() + 100;
            ReservationCreationContent creationContent =
                    new ReservationCreationContent(theme.getId(), NEXT_DAY, reservationTime.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.addReservation(wrongMemberId, creationContent))
                    .isInstanceOf(NotFoundUserException.class)
                    .hasMessage("해당 유저를 찾을 수 없습니다.");
        }

        @DisplayName("테마가 존재하지 않을 경우 예약을 추가할 수 없다.")
        @Test
        void cannotAddReservationByInvalidTheme() {
            // given
            long wrongThemeId = theme.getId() + 100;
            ReservationCreationContent creationContent =
                    new ReservationCreationContent(wrongThemeId, NEXT_DAY, reservationTime.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.addReservation(member.getId(), creationContent))
                    .isInstanceOf(NotFoundThemeException.class)
                    .hasMessage("해당 테마를 찾을 수 없습니다.");
        }

        @DisplayName("예약시간이 존재하지 않을 경우 예약을 추가할 수 없다.")
        @Test
        void cannotAddReservationByInvalidTime() {
            // given
            long wrongTimeId = reservationTime.getId() + 100;
            ReservationCreationContent creationContent =
                    new ReservationCreationContent(theme.getId(), NEXT_DAY, wrongTimeId);

            // when & then
            assertThatThrownBy(() -> reservationService.addReservation(member.getId(), creationContent))
                    .isInstanceOf(NotFoundReservationTimeException.class)
                    .hasMessage("해당 예약시간이 존재하지 않습니다.");
        }
    }

    @DisplayName("예약이 중복일 경우 예약을 추가할 수 없다.")
    @Test
    void cannotAddReservationByDuplicationReservation() {
        // given
        Reservation alreadySavedReservation = entityManager.persist(
                Reservation.createWithoutId(NEXT_DAY, ReservationStatus.BOOKED, reservationTime, theme, member));

        ReservationCreationContent duplicatedCreationContent = new ReservationCreationContent(
                alreadySavedReservation.getTheme().getId(),
                alreadySavedReservation.getDate(),
                alreadySavedReservation.getReservationTime().getId());

        entityManager.flush();

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(member.getId(), duplicatedCreationContent))
                .isInstanceOf(DuplicateReservationException.class)
                .hasMessage("이미 등록되어 있는 예약 시간입니다.");
    }

    @DisplayName("과거의 시간으로 예약을 할 수 없다.")
    @Test
    void cannotAddReservationByPastDateTime() {
        // given
        ReservationCreationContent creationContentWithPast =
                new ReservationCreationContent(theme.getId(), YESTERDAY, reservationTime.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(member.getId(), creationContentWithPast))
                .isInstanceOf(PastReservationCreationException.class)
                .hasMessage("과거의 예약을 추가할 수 없습니다.");
    }

    @Nested
    @DisplayName("예약을 삭제할 수 있다.")
    class deleteReservationById {

        @DisplayName("예약을 성공적으로 삭제할 수 있다.")
        @Test
        void canDelete() {
            // given
            Reservation reservation = entityManager.persist(
                    Reservation.createWithoutId(NEXT_DAY, ReservationStatus.BOOKED, reservationTime, theme, member));

            entityManager.flush();

            // when
            reservationService.deleteReservationById(reservation.getId());

            // then
            List<Reservation> allReservations = reservationRepository.findAll();
            assertThat(allReservations).isEmpty();
        }

        @DisplayName("존재하지 않는 예약을 삭제할 수 없다.")
        @Test
        void cannotDeleteByInvalidReservationId() {
            // given
            long invalidReservationId = 10;

            // when & then
            assertThatThrownBy(() -> reservationService.deleteReservationById(invalidReservationId))
                    .isInstanceOf(NotFoundReservationException.class)
                    .hasMessage("예약을 찾을 수 없습니다.");
        }
    }
}
