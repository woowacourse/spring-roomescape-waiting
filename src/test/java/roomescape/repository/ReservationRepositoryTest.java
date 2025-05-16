package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.domain.User;
import roomescape.test.fixture.ReservationFixture;
import roomescape.test.fixture.ReservationTimeFixture;
import roomescape.test.fixture.ThemeFixture;
import roomescape.test.fixture.UserFixture;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User savedMember;
    private Theme savedTheme;
    private ReservationTime savedTime;

    @BeforeEach
    void setUp() {
        // Create and persist test data
        User member = UserFixture.create(Role.ROLE_MEMBER, "member_dummyName", "member_dummyEmail",
                "member_dummyPassword");
        Theme theme = ThemeFixture.create("dummyName", "dummyDescription", "dummyThumbnail");
        ReservationTime time = ReservationTimeFixture.create(LocalTime.of(2, 40));

        savedMember = entityManager.persist(member);
        savedTheme = entityManager.persist(theme);
        savedTime = entityManager.persist(time);

        entityManager.flush();
    }

    private Reservation createReservation(int plusDays, ReservationTime time) {
        LocalDate date = LocalDate.now().plusDays(plusDays);
        return ReservationFixture.createByBookedStatus(date, time, savedTheme, savedMember);
    }

    @DisplayName("유저의 모든 예약 정보들을 조회할 수 있다.")
    @Test
    void findByUser_success_byMember() {
        // given
        List<Reservation> reservations = List.of(ReservationFixture.createByBookedStatus(
                        LocalDate.now().plusDays(1), savedTime, savedTheme, savedMember),
                ReservationFixture.createByBookedStatus(
                        LocalDate.now().plusDays(2), savedTime, savedTheme, savedMember));
        List<Reservation> expectedReservations = reservationRepository.saveAll(reservations);

        // when
        List<Reservation> actualReservations = reservationRepository.findByUser(savedMember);

        // then
        assertThat(actualReservations).containsExactlyInAnyOrderElementsOf(expectedReservations);
    }

    @DisplayName("예약 시간에 해당하는 예약의 존재 여부를 알 수 있다.")
    @Test
    void existsByReservationTime() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(
                ReservationTime.createWithoutId(LocalTime.now()));

        Reservation reservation = createReservation(1, reservationTime);
        reservationRepository.save(reservation);

        // when
        boolean actual = reservationRepository.existsByReservationTime(reservationTime);

        // then
        assertThat(actual).isTrue();
    }
}
