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
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.dto.business.ReservationTimeCreationContent;
import roomescape.dto.business.ReservationTimeWithBookState;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.exception.local.AlreadyReservedTimeException;
import roomescape.exception.local.DuplicateReservationException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;

@DataJpaTest
class ReservationTimeServiceTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    private ReservationTimeService timeService;

    @BeforeEach
    void setup() {
        timeService = new ReservationTimeService(
                reservationTimeRepository,
                reservationRepository);
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

        User member = entityManager.persist(
                User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));

        ReservationTime timeAt10 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        ReservationTime timeAt11 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(11, 0)));
        ReservationTime timeAt12 = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(12, 0)));

        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, timeAt10, theme, member));
        entityManager.persist(Reservation.createWithoutId(
                TODAY, ReservationStatus.BOOKED, timeAt11, theme, member));

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
                        .containsExactly(false, false, true)
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
                    .isInstanceOf(DuplicateReservationException.class)
                    .hasMessage("이미 등록되어 있는 예약 시간입니다.");
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
        void cannotDeleteDuplicatedReservationTime() {
            // given
            Theme theme = entityManager.persist(
                    Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));

            User member = entityManager.persist(
                    User.createWithoutId(Role.ROLE_MEMBER, "회원", "member@test.com", "password123"));

            ReservationTime time = entityManager.persist(ReservationTime.createWithoutId(LocalTime.of(10, 0)));

            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, time, theme, member));

            entityManager.flush();

            // when & then
            assertThatThrownBy(() -> timeService.deleteReservationTimeById(time.getId()))
                    .isInstanceOf(AlreadyReservedTimeException.class)
                    .hasMessage("예약에서 사용 중인 시간입니다.");
        }
    }
}
