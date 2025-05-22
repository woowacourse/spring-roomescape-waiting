package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.test.fixture.DateFixture.NEXT_DAY;
import static roomescape.test.fixture.DateFixture.YESTERDAY;

import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.dto.business.WaitingCreationContent;
import roomescape.dto.response.WaitingResponse;
import roomescape.exception.local.DuplicatedWaitingException;
import roomescape.exception.local.NotCreateWaitingInEmptyReservationException;
import roomescape.exception.local.NotFoundMemberException;
import roomescape.exception.local.NotFoundReservationTimeException;
import roomescape.exception.local.NotFoundThemeException;
import roomescape.exception.local.PastWaitingCreationException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;

@DataJpaTest
class WaitingServiceTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    private WaitingService waitingService;

    @BeforeEach
    void setup() {
        waitingService = new WaitingService(
                waitingRepository, themeRepository, reservationTimeRepository, memberRepository, reservationRepository);
    }

    @Nested
    @DisplayName("대기 데이터를 추가할 수 있다.")
    public class addWaiting {

        @DisplayName("대기 데이터를 추가할 수 있다.")
        @Test
        void testMethodNameHere() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));
            Reservation reservation = entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, time, theme, member));

            entityManager.flush();

            WaitingCreationContent creationContent =
                    new WaitingCreationContent(NEXT_DAY, theme.getId(), time.getId(), member.getId());

            // when
            WaitingResponse waitingResponse = waitingService.addWaiting(creationContent);

            // then
            Waiting expectedWaiting = entityManager.find(Waiting.class, waitingResponse.id());
            assertAll(
                    () -> assertThat(waitingResponse.id()).isEqualTo(expectedWaiting.getId()),
                    () -> assertThat(waitingResponse.date()).isEqualTo(creationContent.date()),
                    () -> assertThat(waitingResponse.themeResponse().id()).isEqualTo(creationContent.themeId()),
                    () -> assertThat(waitingResponse.timeResponse().id()).isEqualTo(creationContent.timeId()),
                    () -> assertThat(waitingResponse.memberProfileResponse().id()).isEqualTo(creationContent.memberId())
            );
        }

        @DisplayName("테마가 유효하지 않은 경우 대기 데이터를 추가할 수 없다.")
        @Test
        void cannotAddByInvalidTheme() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));
            Reservation reservation = entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, time, theme, member));

            entityManager.flush();

            WaitingCreationContent creationContent =
                    new WaitingCreationContent(NEXT_DAY, theme.getId() + 100, time.getId(), member.getId());

            // when & then
            assertThatThrownBy(() -> waitingService.addWaiting(creationContent))
                    .isInstanceOf(NotFoundThemeException.class)
                    .hasMessage("해당 테마를 찾을 수 없습니다.");

        }

        @DisplayName("예약시간이 유효하지 않은 경우 대기 데이터를 추가할 수 없다.")
        @Test
        void cannotAddByInvalidTime() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));
            Reservation reservation = entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, time, theme, member));

            entityManager.flush();

            WaitingCreationContent creationContent =
                    new WaitingCreationContent(NEXT_DAY, theme.getId(), time.getId() + 100, member.getId());

            // when & then
            assertThatThrownBy(() -> waitingService.addWaiting(creationContent))
                    .isInstanceOf(NotFoundReservationTimeException.class)
                    .hasMessage("해당 예약시간이 존재하지 않습니다.");
        }

        @DisplayName("회원이 유효하지 않은 경우 대기 데이터를 추가할 수 없다.")
        @Test
        void cannotAddByInvalidMember() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));
            Reservation reservation = entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, time, theme, member));

            entityManager.flush();

            WaitingCreationContent creationContent =
                    new WaitingCreationContent(NEXT_DAY, theme.getId(), time.getId(), member.getId() + 100);

            // when & then
            assertThatThrownBy(() -> waitingService.addWaiting(creationContent))
                    .isInstanceOf(NotFoundMemberException.class)
                    .hasMessage("해당 유저를 찾을 수 없습니다.");
        }

        @DisplayName("과거의 대기 데이터를 저장하는 것은 허용하지 않는다.")
        @Test
        void cannotAddByPastWaiting() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));
            Reservation reservation = entityManager.persist(Reservation.createWithoutId(
                    YESTERDAY, ReservationStatus.BOOKED, time, theme, member));

            entityManager.flush();

            WaitingCreationContent creationContent =
                    new WaitingCreationContent(YESTERDAY, theme.getId(), time.getId(), member.getId());

            // when & then
            assertThatThrownBy(() -> waitingService.addWaiting(creationContent))
                    .isInstanceOf(PastWaitingCreationException.class)
                    .hasMessage("과거의 예약 대기를 추가할 수 없습니다.");
        }

        @DisplayName("대기 데이터의 중복 저장은 허용하지 않늗다.")
        @Test
        void cannotAddByDuplicatedWaiting() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));
            Reservation reservation = entityManager.persist(Reservation.createWithoutId(
                    NEXT_DAY, ReservationStatus.BOOKED, time, theme, member));
            Waiting waiting = entityManager.persist(Waiting.createWithoutId(NEXT_DAY, theme, time, member));

            entityManager.flush();

            WaitingCreationContent creationContent =
                    new WaitingCreationContent(NEXT_DAY, theme.getId(), time.getId(), member.getId());

            // when & then
            assertThatThrownBy(() -> waitingService.addWaiting(creationContent))
                    .isInstanceOf(DuplicatedWaitingException.class)
                    .hasMessage("중복된 대기 데이터입니다.");
        }

        @DisplayName("예약이 존재하지 않는 경우 대기 데이터를 저장할 수 없다.")
        @Test
        void cannotAddByEmptyReservation() {
            // given
            ReservationTime time = entityManager.persist(
                    ReservationTime.createWithoutId(LocalTime.of(10, 0)));
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "qwer1234!"));

            entityManager.flush();

            WaitingCreationContent creationContent =
                    new WaitingCreationContent(NEXT_DAY, theme.getId(), time.getId(), member.getId());

            // when & then
            assertThatThrownBy(() -> waitingService.addWaiting(creationContent))
                    .isInstanceOf(NotCreateWaitingInEmptyReservationException.class)
                    .hasMessage("예약이 존재하지 않습니다.");
        }
    }
}
