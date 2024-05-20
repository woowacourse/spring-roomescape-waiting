package roomescape.infrastructure.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberFixture;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationSpec;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;

@DataJpaTest
class JpaReservationRepositoryTest {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2000, 1, 1, 12, 0);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약 시간 id를 가진 예약의 개수를 조회한다.")
    void shouldReturnCountOfReservationWhenReservationTimeUsed() {
        ReservationTime time = createReservation().getTime();
        boolean exists = reservationRepository.existsByTimeId(time.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("날짜, 시간으로 저장된 예약이 있는지 확인한다.")
    void shouldReturnIsExistReservationWhenReservationsNameAndDateAndTimeIsSame() {
        Reservation reservation = createReservation();
        ReservationTime time = reservation.getTime();
        Theme theme = reservation.getTheme();

        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeId(reservation.getDate(), time.getId(),
                theme.getId());
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("날짜, 테마, 사용자로 예약을 조회한다.")
    @Sql(scripts = "/insert-reservations.sql")
    void findByMemberAndThemeBetweenDates() {
        Specification<Reservation> specification = ReservationSpec.where()
                .equalsMemberId(2L)
                .equalsThemeId(3L)
                .greaterThanOrEqualsStartDate(LocalDate.of(1999, 12, 24))
                .lessThanOrEqualsEndDate(LocalDate.of(1999, 12, 29))
                .build();
        List<Reservation> reservations = reservationRepository.findAll(specification);
        assertThat(reservations).hasSize(2);
    }

    @Test
    @DisplayName("memberId로 해당 멤버의 예약을 조회한다.")
    void findAllByMemberIdTest() {
        Member hodol = memberRepository.save(MemberFixture.createMember("호돌"));
        Member pk = memberRepository.save(MemberFixture.createMember("피케이"));
        createReservation(hodol, LocalDate.of(2024, 12, 25));
        createReservation(pk, LocalDate.of(2025, 1, 1));
        List<Reservation> reservations = reservationRepository.findAllByMemberId(hodol.getId());
        assertThat(reservations).hasSize(1);
    }

    private Reservation createReservation() {
        Member member = memberRepository.save(MemberFixture.createMember("오리"));
        LocalDate date = LocalDate.of(2024, 12, 25);
        return createReservation(member, date);
    }

    private Reservation createReservation(Member member, LocalDate date) {
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 0)));
        Theme theme = themeRepository.save(new Theme("theme1", "desc", "url"));
        Reservation reservation = new Reservation(member, date, reservationTime, theme, BASE_TIME);
        entityManager.persist(reservation);
        return reservation;
    }
}
