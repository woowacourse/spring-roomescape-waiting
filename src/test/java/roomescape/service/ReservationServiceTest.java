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
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.business.ReservationCreationContent;
import roomescape.dto.response.MemberProfileResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.dto.response.ReservationStatusResponse;
import roomescape.dto.response.ThemeResponse;
import roomescape.dto.response.WaitingWithRankResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

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
    private MemberRepository memberRepository;
    @Autowired
    private WaitingRepository waitingRepository;

    private ReservationService reservationService;

    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;

    @BeforeEach
    void setup() {
        reservationService = new ReservationService(
                reservationRepository,
                reservationTimeRepository,
                themeRepository,
                memberRepository,
                waitingRepository);

        reservationTime = entityManager.persist(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        theme = entityManager.persist(
                Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
        member = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123!"));
    }

    @DisplayName("모든 예약을 조회할 수 있다.")
    @Test
    void canFindAll() {
        // given
        entityManager.persist(Reservation.createWithoutId(
                TODAY, reservationTime, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, reservationTime, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, reservationTime, theme, member));

        entityManager.flush();

        // when
        List<ReservationResponse> allReservations = reservationService.findAllReservations();

        // then
        assertAll(
                () -> assertThat(allReservations).hasSize(3),
                () -> assertThat(allReservations)
                        .extracting(ReservationResponse::member)
                        .extracting(MemberProfileResponse::id)
                        .containsExactly(member.getId(), member.getId(), member.getId())
        );
    }

    @DisplayName("회원의 모든 예약을 조회할 수 있다.")
    @Test
    void testMethodNameHere() {
        // given
        entityManager.persist(Reservation.createWithoutId(
                TODAY, reservationTime, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, reservationTime, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, reservationTime, theme, member));

        Member otherMember = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원", "member2@test.com", "password123!"));

        entityManager.persist(Reservation.createWithoutId(
                TODAY, reservationTime, theme, otherMember));

        entityManager.flush();

        // when
        List<ReservationResponse> allReservations = reservationService.findAllReservationsByMember(member.getId());

        // then
        assertAll(
                () -> assertThat(allReservations).hasSize(3),
                () -> assertThat(allReservations)
                        .extracting(ReservationResponse::member)
                        .extracting(MemberProfileResponse::id)
                        .containsExactly(member.getId(), member.getId(), member.getId())
        );
    }

    @DisplayName("회원의 모든 예약 상태를 조회할 수 있다.")
    @Test
    void canFindAllReservationStatusByMember() {
        // given
        List<Reservation> reservations = List.of(
                entityManager.persist(Reservation.createWithoutId(TODAY, reservationTime, theme, member)),
                entityManager.persist(Reservation.createWithoutId(TODAY, reservationTime, theme, member)),
                entityManager.persist(Reservation.createWithoutId(TODAY, reservationTime, theme, member)));
        List<Waiting> waitings = List.of(
                entityManager.persist(Waiting.createWithoutId(TODAY, theme, reservationTime, member)),
                entityManager.persist(Waiting.createWithoutId(TODAY, theme, reservationTime, member)),
                entityManager.persist(Waiting.createWithoutId(TODAY, theme, reservationTime, member)));

        // when
        ReservationStatusResponse allReservationState =
                reservationService.findAllReservationStatusByMember(member.getId());

        // then
        List<Long> reservationIds = reservations.stream().map(Reservation::getId).toList();
        List<Long> waitingIds = waitings.stream().map(Waiting::getId).toList();
        assertAll(
                () -> assertThat(allReservationState.reservationResponses())
                        .extracting(ReservationResponse::id)
                        .containsExactlyElementsOf(reservationIds),
                () -> assertThat(allReservationState.waitingWithRankResponses())
                        .extracting(WaitingWithRankResponse::id)
                        .containsExactlyElementsOf(waitingIds)
        );
    }

    @Nested
    @DisplayName("필터를 통해 예약을 조회할 수 있다")
    class findReservationsByFilter {

        @Test
        @DisplayName("필터 조건으로 특정 유저의 예약을 조회할 수 있다")
        void canFindReservationsByMemberFilter() {
            // given
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, reservationTime, theme, member));

            Member otherMember = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "다른회원", "otherMember@test.com", "password123!"));

            entityManager.persist(Reservation.createWithoutId(
                    TODAY, reservationTime, theme, otherMember));
            entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, reservationTime, theme, otherMember));

            entityManager.flush();

            // when
            List<ReservationResponse> reservations = reservationService.findReservationsByFilter(
                    otherMember.getId(), theme.getId(), TODAY, TODAY.plusDays(7));

            // then
            assertAll(
                    () -> assertThat(reservations).hasSize(2),
                    () -> assertThat(reservations)
                            .extracting(ReservationResponse::member)
                            .extracting(MemberProfileResponse::id)
                            .containsExactly(otherMember.getId(), otherMember.getId())
            );
        }

        @Test
        @DisplayName("필터 조건으로 특정 테마의 예약을 조회할 수 있다")
        void canFindReservationsByThemeFilter() {
            // given
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, reservationTime, theme, member));

            Theme otherTheme = entityManager.persist(
                    Theme.createWithoutId("다른테마", "설명", "thumbnail.jpg"));

            entityManager.persist(Reservation.createWithoutId(
                    TODAY, reservationTime, otherTheme, member));
            entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, reservationTime, otherTheme, member));

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
                    Reservation.createWithoutId(YESTERDAY, reservationTime, theme, member));
            entityManager.persist(
                    Reservation.createWithoutId(TODAY, reservationTime, theme, member));
            entityManager.persist(
                    Reservation.createWithoutId(NEXT_DAY, reservationTime, theme, member));

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
                    () -> assertThat(reservationResponse.member().id()).isEqualTo(member.getId()),
                    () -> assertThat(reservationResponse.date()).isEqualTo(creationContent.date()),
                    () -> assertThat(reservationResponse.theme().id()).isEqualTo(creationContent.themeId())
            );
        }

        @DisplayName("유저가 존재하지 않을 경우 예약을 추가할 수 없다.")
        @Test
        void cannotAddReservationByInvalidMember() {
            // given
            long wrongMemberId = member.getId() + 100;
            ReservationCreationContent creationContent =
                    new ReservationCreationContent(theme.getId(), NEXT_DAY, reservationTime.getId());

            // when & then
            assertThatThrownBy(() -> reservationService.addReservation(wrongMemberId, creationContent))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("ID에 해당하는 회원을 찾을 수 없습니다.");
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
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("ID에 해당하는 테마을 찾을 수 없습니다.");
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
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("ID에 해당하는 예약 시간을 찾을 수 없습니다.");
        }
    }

    @DisplayName("예약이 중복일 경우 예약을 추가할 수 없다.")
    @Test
    void cannotAddReservationByDuplicationReservation() {
        // given
        Reservation alreadySavedReservation = entityManager.persist(
                Reservation.createWithoutId(NEXT_DAY, reservationTime, theme, member));

        ReservationCreationContent duplicatedCreationContent = new ReservationCreationContent(
                alreadySavedReservation.getTheme().getId(),
                alreadySavedReservation.getDate(),
                alreadySavedReservation.getReservationTime().getId());

        entityManager.flush();

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(member.getId(), duplicatedCreationContent))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 예약 입니다.");
    }

    @DisplayName("과거의 시간으로 예약을 할 수 없다.")
    @Test
    void cannotAddReservationByPastDateTime() {
        // given
        ReservationCreationContent creationContentWithPast =
                new ReservationCreationContent(theme.getId(), YESTERDAY, reservationTime.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(member.getId(), creationContentWithPast))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("과거 예약은 생성할 수 없습니다.");
    }

    @Nested
    @DisplayName("예약을 삭제할 수 있다.")
    class deleteReservationById {

        @DisplayName("예약을 성공적으로 삭제할 수 있다.")
        @Test
        void canDelete() {
            // given
            Reservation reservation = entityManager.persist(
                    Reservation.createWithoutId(NEXT_DAY, reservationTime, theme, member));

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
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("ID에 해당하는 예약을 찾을 수 없습니다.");
        }

        @DisplayName("예약 대기가 존재할 경우 첫번째 예약 대기를 예약으로 등록한다.")
        @Test
        void canAddNewReservationWithWaiting() {
            // given
            Reservation reservation = entityManager.persist(
                    Reservation.createWithoutId(NEXT_DAY, reservationTime, theme, member));

            Member firstWaitingMember = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원1", "waiting1@email.com", "qwer1234!"));
            Member secondWaitingMember = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원2", "waiting2@email.com", "qwer1234!"));

            Waiting firstWaiting = entityManager.persist(
                    Waiting.createWithoutId(
                            reservation.getDate(), reservation.getTheme(),
                            reservation.getReservationTime(), firstWaitingMember));
            Waiting secondWaiting = entityManager.persist(
                    Waiting.createWithoutId(
                            reservation.getDate(), reservation.getTheme(),
                            reservation.getReservationTime(), secondWaitingMember));

            entityManager.flush();

            // when
            reservationService.deleteReservationById(reservation.getId());

            // then
            List<Reservation> newReservation = reservationRepository.findByMember(firstWaitingMember);
            assertAll(
                    () -> assertThat(newReservation).hasSize(1),
                    () -> assertThat(newReservation.getFirst().getDate()).isEqualTo(firstWaiting.getDate()),
                    () -> assertThat(newReservation.getFirst().getTheme()).isEqualTo(firstWaiting.getTheme()),
                    () -> assertThat(newReservation.getFirst().getReservationTime()).isEqualTo(firstWaiting.getTime()),
                    () -> assertThat(newReservation.getFirst().getMember()).isEqualTo(firstWaiting.getMember())
            );
        }

        @DisplayName("예약 대기가 존재해서 첫번째 예약 대기가 예약으로 등록된 경우 첫번째 예약 대기는 삭제된다.")
        @Test
        void canDeleteFirstWaiting() {
            // given
            Reservation reservation = entityManager.persist(
                    Reservation.createWithoutId(NEXT_DAY, reservationTime, theme, member));

            Member firstWaitingMember = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원1", "waiting1@email.com", "qwer1234!"));
            Member secondWaitingMember = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원2", "waiting2@email.com", "qwer1234!"));

            Waiting firstWaiting = entityManager.persist(
                    Waiting.createWithoutId(
                            reservation.getDate(), reservation.getTheme(),
                            reservation.getReservationTime(), firstWaitingMember));
            Waiting secondWaiting = entityManager.persist(
                    Waiting.createWithoutId(
                            reservation.getDate(), reservation.getTheme(),
                            reservation.getReservationTime(), secondWaitingMember));

            entityManager.flush();
            entityManager.clear();

            // when
            reservationService.deleteReservationById(reservation.getId());

            // then
            Waiting deletedFirstWaiting = entityManager.find(Waiting.class, firstWaiting.getId());
            assertThat(deletedFirstWaiting).isNull();
        }
    }
}
