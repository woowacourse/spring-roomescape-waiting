package roomescape.domain.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.entity.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.entity.Theme;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Member member1;
    private Member member2;
    private Theme theme1;
    private Theme theme2;
    private ReservationTime time1;
    private ReservationTime time2;
    private LocalDate yesterday;
    private LocalDate today;
    private LocalDate tomorrow;

    @BeforeEach
    void setUp() {
        // 데이터베이스 초기화
        entityManager.createQuery("DELETE FROM Reservation").executeUpdate();
        entityManager.createQuery("DELETE FROM Member").executeUpdate();
        entityManager.createQuery("DELETE FROM Theme").executeUpdate();
        entityManager.createQuery("DELETE FROM ReservationTime").executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // 회원 생성
        member1 = Member.withoutId("어드민", "admin@email.com", "password", Role.ADMIN);
        member2 = Member.withoutId("브라운", "brown@email.com", "brown", Role.USER);
        entityManager.persist(member1);
        entityManager.persist(member2);

        // 테마 생성
        theme1 = Theme.withoutId("테마1", "테마 1입니다.", "썸네일1");
        theme2 = Theme.withoutId("테마2", "테마 2입니다.", "썸네일2");
        entityManager.persist(theme1);
        entityManager.persist(theme2);

        // 예약 시간 생성
        time1 = ReservationTime.withoutId(LocalTime.of(10, 0));
        time2 = ReservationTime.withoutId(LocalTime.of(11, 0));
        entityManager.persist(time1);
        entityManager.persist(time2);

        // 날짜 설정 - 특정 날짜 사용
        yesterday = LocalDate.of(2025, 3, 1);
        today = LocalDate.of(2025, 3, 2);
        tomorrow = LocalDate.of(2025, 3, 3);

        // 예약 생성
        Reservation reservation1 = Reservation.withoutId(member1, theme1, yesterday, time1, ReservationStatus.RESERVED);
        Reservation reservation2 = Reservation.withoutId(member2, theme1, today, time2, ReservationStatus.RESERVED);
        Reservation reservation3 = Reservation.withoutId(member1, theme2, tomorrow, time2, ReservationStatus.RESERVED);

        entityManager.persist(reservation1);
        entityManager.persist(reservation2);
        entityManager.persist(reservation3);

        entityManager.flush();
        entityManager.clear();
    }

    @DisplayName("회원의 예약을 검색한다")
    @Test
    void findByMember() {
        // given
        Long memberId = member1.getId();

        // when
        List<Reservation> reservations = reservationRepository.findByMember(member1);

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(reservation -> reservation.getMember().getId().equals(memberId));
    }

    @DisplayName("회원 ID, 테마 ID, 날짜 범위로 예약을 검색한다")
    @Test
    void findByMemberAndThemeAndDateRange() {
        // given
        Long memberId = member1.getId();
        Long themeId = theme1.getId();
        LocalDate dateFrom = yesterday;
        LocalDate dateTo = tomorrow;

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                memberId, themeId, dateFrom, dateTo);

        // then
        assertThat(reservations).hasSize(1);
        assertThat(reservations).allMatch(reservation ->
                reservation.getMember().getId().equals(memberId) &&
                reservation.getTheme().getId().equals(themeId) &&
                reservation.getDate().isAfter(dateFrom.minusDays(1)) &&
                reservation.getDate().isBefore(dateTo.plusDays(1)));
    }

    @DisplayName("모든 파라미터가 null일 때 모든 예약을 반환한다")
    @Test
    void findByMemberAndThemeAndDateRangeWithAllNull() {
        // given
        Long memberId = null;
        Long themeId = null;
        LocalDate dateFrom = null;
        LocalDate dateTo = null;

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                memberId, themeId, dateFrom, dateTo);

        // then
        assertThat(reservations).hasSize(3);
        assertThat(reservations).extracting("member.id")
                .containsExactlyInAnyOrder(member1.getId(), member2.getId(), member1.getId());
        assertThat(reservations).extracting("date")
                .containsExactlyInAnyOrder(yesterday, today, tomorrow);
    }

    @DisplayName("회원 ID만으로 예약을 검색한다")
    @Test
    void findByMemberAndThemeAndDateRangeWithOnlyMemberId() {
        // given
        Long memberId = member1.getId();
        Long themeId = null;
        LocalDate dateFrom = null;
        LocalDate dateTo = null;

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                memberId, themeId, dateFrom, dateTo);

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(reservation -> reservation.getMember().getId().equals(memberId));
    }

    @DisplayName("테마 ID만으로 예약을 검색한다")
    @Test
    void findByMemberAndThemeAndDateRangeWithOnlyThemeId() {
        // given
        Long memberId = null;
        Long themeId = theme1.getId();
        LocalDate dateFrom = null;
        LocalDate dateTo = null;

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                memberId, themeId, dateFrom, dateTo);

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(reservation -> reservation.getTheme().getId().equals(themeId));
    }

    @DisplayName("날짜 범위만으로 예약을 검색한다")
    @Test
    void findByMemberAndThemeAndDateRangeWithOnlyDateRange() {
        // given
        Long memberId = null;
        Long themeId = null;
        LocalDate dateFrom = yesterday;
        LocalDate dateTo = today;

        // when
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                memberId, themeId, dateFrom, dateTo);

        // then
        assertThat(reservations).hasSize(2);
        assertThat(reservations).allMatch(reservation ->
                (reservation.getDate().isEqual(dateFrom) || reservation.getDate().isAfter(dateFrom)) &&
                (reservation.getDate().isEqual(dateTo) || reservation.getDate().isBefore(dateTo)));
    }
}
