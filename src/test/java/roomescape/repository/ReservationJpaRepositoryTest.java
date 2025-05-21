package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;
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
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

@DataJpaTest
public class ReservationJpaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private ReservationRepository reservationRepository;

    private ReservationTime reservationTime;
    private Theme theme;
    private Member member;

    @BeforeEach
    void setup() {
        reservationTime = entityManager.persist(
                ReservationTime.createWithoutId(LocalTime.of(10, 0)));
        theme = entityManager.persist(
                Theme.createWithoutId("테마", "테마 설명", "thumbnail.jpg"));
        member = entityManager.persist(
                Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123"));
    }

    @Nested
    @DisplayName("필터를 통해 예약을 조회할 수 있다")
    class findReservationsByFilter {

        @Test
        @DisplayName("필터 조건으로 특정 유저의 예약을 조회할 수 있다")
        void canFindReservationsByMemberFilter() {
            // given
            Member otherMember = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "다른회원", "otherMember@test.com", "password123"));
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, theme, otherMember));
            entityManager.flush();

            // when
            List<Reservation> reservations = reservationRepository.findReservationsByFilter(
                    member.getId(), theme.getId(), TODAY, TODAY.plusDays(7));

            // then
            assertAll(
                    () -> assertThat(reservations).hasSize(1),
                    () -> assertThat(reservations.getFirst().getMember()).isEqualTo(member)
            );
        }

        @Test
        @DisplayName("필터 조건으로 특정 테마의 예약을 조회할 수 있다")
        void canFindReservationsByThemeFilter() {
            // given
            Theme otherTheme = entityManager.persist(
                    Theme.createWithoutId("다른테마", "설명", "thumbnail.jpg"));
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, theme, member));
            entityManager.persist(Reservation.createWithoutId(
                    TODAY, ReservationStatus.BOOKED, reservationTime, otherTheme, member));
            entityManager.flush();

            // when
            List<Reservation> reservations = reservationRepository.findReservationsByFilter(
                    member.getId(), theme.getId(), TODAY, TODAY.plusDays(7));

            // then
            assertAll(
                    () -> assertThat(reservations).hasSize(1),
                    () -> assertThat(reservations.getFirst().getTheme()).isEqualTo(theme)
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
                    () -> assertThat(reservations).extracting(Reservation::getDate)
                            .containsExactlyInAnyOrder(TODAY, NEXT_DAY)
            );
        }
    }
}
