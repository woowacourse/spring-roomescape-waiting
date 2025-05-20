package roomescape.reservation.application.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

@ActiveProfiles("test")
@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @DisplayName("회원ID, 테마ID, 날짜 범위로 예약 목록을 조회한다")
    void findAllByMemberIdAndThemeIdAndDateBetweenTest() {
        // given
        TestData testData = setUpTestData();

        // when
        List<Reservation> nullResult = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                null, null, null, null);
        List<Reservation> memberIdResult = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                testData.member1.getId(), null, null, null);
        List<Reservation> themeResult = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                null, testData.theme1.getId(), null, null);
        List<Reservation> dateResult = reservationRepository.findAllByMemberIdAndThemeIdAndDateBetween(
                null, null, LocalDate.now().minusDays(3), LocalDate.now());

        // then
        assertAll(
                () -> assertThat(nullResult).hasSize(4),
                () -> assertThat(memberIdResult).hasSize(2),
                () -> assertThat(themeResult).hasSize(2),
                () -> assertThat(dateResult).hasSize(4)
        );
    }

    private TestData setUpTestData() {
        Member member1 = createAndSaveMemberFixture();
        Member member2 = createAndSaveMemberFixture();
        Theme theme1 = createAndSaveThemeFixture();
        Theme theme2 = createAndSaveThemeFixture();
        ReservationTime reservationTime = createAndSaveReservationTimeFixture();

        createAndSaveReservationFixture(member1, theme1, LocalDate.now(), reservationTime);
        createAndSaveReservationFixture(member1, theme2, LocalDate.now().minusDays(1), reservationTime);
        createAndSaveReservationFixture(member2, theme1, LocalDate.now().minusDays(2), reservationTime);
        createAndSaveReservationFixture(member2, theme2, LocalDate.now().minusDays(3), reservationTime);
        return new TestData(member1, member2, theme1, theme2);
    }

    private record TestData(
            Member member1,
            Member member2,
            Theme theme1,
            Theme theme2
    ) {
    }

    private void createAndSaveReservationFixture(Member member, Theme theme, LocalDate date,
                                                 ReservationTime reservationTime) {
        Reservation reservation = Reservation.createReserved(member, theme, date, reservationTime);
        entityManager.persist(reservation);
    }

    private Member createAndSaveMemberFixture() {
        Member member = new Member("이메일", "패스워드", "이름", Role.USER);
        entityManager.persist(member);
        return member;
    }

    private Theme createAndSaveThemeFixture() {
        Theme theme = new Theme("테마명", "설명", "thumbnail");
        entityManager.persist(theme);
        return theme;
    }

    private ReservationTime createAndSaveReservationTimeFixture() {
        ReservationTime reservationTime = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(reservationTime);
        return reservationTime;
    }
}
