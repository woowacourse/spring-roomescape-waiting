package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static roomescape.test.fixture.DateFixture.TODAY;

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
import roomescape.dto.business.ReservationTimeCreationContent;
import roomescape.dto.business.ReservationTimeWithBookState;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.BadRequestException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.WaitingRepository;

@DataJpaTest
class ReservationTimeServiceTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private WaitingRepository waitingRepository;

    private ReservationTimeService timeService;

    @BeforeEach
    void setup() {
        timeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository,
                waitingRepository);
    }

    @DisplayName("모든 예약을 조회할 수 있다.")
    @Test
    void canFindAllReservations() {
        // given
        entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(11, 0)));
        entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(12, 0)));

        entityManager.flush();

        // when
        List<ReservationTimeResponse> allReservationTimes = timeService.findAllReservationTimes();

        // then
        assertThat(allReservationTimes).hasSize(3);
    }

    @DisplayName("특정 테마와 날짜에 대한 예약시간을 예약 가능 여부와 함께 조회할 수 있다.")
    @Test
    void canFindReservationsWithBookState() {
        // given
        Theme theme = entityManager.persist(
                Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));

        Member member = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123!"));

        ReservationTime timeAt10 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        ReservationTime timeAt11 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(11, 0)));
        ReservationTime timeAt12 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(12, 0)));

        entityManager.persist(Reservation.createWithoutId(
                TODAY, timeAt10, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, timeAt11, theme, member));

        entityManager.flush();

        // when
        List<ReservationTimeWithBookState> timesWithBookState =
                timeService.findReservationTimesWithBookState(theme.getId(), TODAY);

        // then
        assertAll(
                () -> assertThat(timesWithBookState)
                        .extracting(ReservationTimeWithBookState::id)
                        .containsExactly(timeAt10.getId(), timeAt11.getId(), timeAt12.getId()),
                () -> assertThat(timesWithBookState)
                        .extracting(ReservationTimeWithBookState::alreadyBooked)
                        .containsExactly(true, true, false)
        );
    }

    @Nested
    @DisplayName("예약 시간을 추가할 수 있다.")
    public class addReservationTime {

        @DisplayName("예약 시간을 성공적으로 추가할 수 있다.")
        @Test
        void canAddReservationTime() {
            // given
            ReservationTimeCreationContent creationContent = new ReservationTimeCreationContent(LocalTime.of(10, 0));

            // when
            ReservationTimeResponse response = timeService.addReservationTime(creationContent);

            // then
            ReservationTime expectedTime = entityManager.find(ReservationTime.class, response.id());
            assertAll(
                    () -> assertThat(response.id()).isEqualTo(expectedTime.getId()),
                    () -> assertThat(response.startAt()).isEqualTo(expectedTime.getStartAt())
            );
        }

        @DisplayName("중복된 에약 시간은 추가할 수 없다.")
        @Test
        void cannotAddDuplicatedReservationTime() {
            // given
            ReservationTime savedTime = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));

            ReservationTimeCreationContent duplicationCreation =
                    new ReservationTimeCreationContent(savedTime.getStartAt());

            entityManager.flush();

            // when & then
            assertThatThrownBy(() -> timeService.addReservationTime(duplicationCreation))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("중복된 예약시간입니다.");
        }
    }

    @Nested
    @DisplayName("예약 시간을 삭제할 수 있다.")
    public class deleteReservationTimeById {

        @DisplayName("예약 시간을 성공적으로 추가할 수 있다.")
        @Test
        void canDeleteReservationTime() {
            // given
            ReservationTime time = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));

            entityManager.flush();

            // when
            timeService.deleteReservationTimeById(time.getId());

            // then
            assertThat(entityManager.find(ReservationTime.class, time.getId())).isNull();
        }

        @DisplayName("이미 해당 시간에 예약이 존재할 경우 예약을 추가할 수 없다.")
        @Test
        void cannotDeleteReservedTimeByReservation() {
            // given
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));

            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123!"));

            ReservationTime time = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));

            entityManager.persist(Reservation.createWithoutId(
                    TODAY, time, theme, member));

            entityManager.flush();

            // when & then
            assertThatThrownBy(() -> timeService.deleteReservationTimeById(time.getId()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("이미 예약이 존재하는 예약 시간입니다.");
        }

        @DisplayName("이미 해당 시간에 예약 대기가 존재할 경우 예약을 추가할 수 없다.")
        @Test
        void cannotDeleteReservedTimeByWaiting() {
            // given
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));

            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123!"));

            ReservationTime time = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));

            entityManager.persist(Waiting.createWithoutId(TODAY, theme, time, member));

            entityManager.flush();

            // when & then
            assertThatThrownBy(() -> timeService.deleteReservationTimeById(time.getId()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("이미 예약 대기가 존재하는 예약 시간입니다.");
        }
    }
}
