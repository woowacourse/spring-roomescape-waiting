package roomescape.repository.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.enums.Role;
import roomescape.domain.enums.Waiting;
import roomescape.repository.member.MemberRepository;

@DataJpaTest
@TestPropertySource(properties = "spring.sql.init.mode=never")
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member(null, "name", "email@email.com", "password", Role.USER);
        memberRepository.save(member);
    }

    @Test
    @DisplayName("특정 기간 동안 가장 많이 예약된 상위 테마들을 조회할 수 있다")
    void findTopThemesByReservationCountBetweenTest() {
        // given
        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);
        LocalDate outOfRangeDate = LocalDate.of(2025, 6, 1);

        Theme[] themes = new Theme[12];
        for (int i = 0; i < 12; i++) {
            themes[i] = new Theme("theme" + i, "description", "thumbnail");
            entityManager.persist(themes[i]);
        }

        for (int i = 0; i < 12; i++) {
            createReservationsInRange(themes[i], 15 - i, startDate, member);
        }

        createReservationsInRange(themes[10], 20, outOfRangeDate, member);
        createReservationsInRange(themes[11], 30, outOfRangeDate, member);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Theme> topThemes = reservationRepository.findTopThemesByReservationCountBetween(startDate, endDate);

        // then
        Assertions.assertAll(
                () -> {
                    assertThat(topThemes).hasSize(12);
                    for (int i = 0; i < 12; i++) {
                        assertThat(topThemes.get(i).getId()).isEqualTo(themes[i].getId());
                    }
                }
        );
    }

    @Test
    @DisplayName("특정 테마, 회원, 특정 기간에 대한 예약 목록을 조회할 수 있다")
    void findAllByThemeIdAndMemberIdAndDateBetweenTest() {
        // given
        Theme theme1 = new Theme("theme1", "description", "thumbnail");
        Theme theme2 = new Theme("theme2", "description", "thumbnail");
        entityManager.persist(theme1);
        entityManager.persist(theme2);

        Member otherMember = new Member(null, "other", "other@email.com", "password", Role.USER);
        memberRepository.save(otherMember);

        LocalDate startDate = LocalDate.of(2025, 5, 1);
        LocalDate endDate = LocalDate.of(2025, 5, 31);
        LocalDate outOfRangeDate = LocalDate.of(2025, 6, 1);

        createReservationsInRange(theme1, 3, startDate, member);
        createReservationsInRange(theme2, 2, startDate, member);
        createReservationsInRange(theme1, 2, startDate, otherMember);
        createReservationsInRange(theme1, 1, outOfRangeDate, member);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Reservation> reservations = reservationRepository
                .findAllByThemeIdAndMemberIdAndDateBetween(theme1.getId(), member.getId(), startDate, endDate);

        // then
        Assertions.assertAll(
                () -> assertThat(reservations).hasSize(3),
                () -> assertThat(reservations)
                        .allMatch(reservation ->
                                reservation.getTheme().getId().equals(theme1.getId()) &&
                                        reservation.getMember().getId().equals(member.getId()) &&
                                        !reservation.getDate().isBefore(startDate) &&
                                        !reservation.getDate().isAfter(endDate)
                        )
        );
    }

    @Test
    @DisplayName("특정 날짜, 시간, 테마의 예약 개수를 반환할 수 있다")
    void countByDateAndTimeAndThemeTest() {
        // given
        LocalDate date = LocalDate.of(2025, 5, 1);
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);

        Theme theme1 = new Theme("theme1", "description", "thumbnail");
        entityManager.persist(theme1);

        for (int i = 0; i < 3; i++) {
            ReservationStatus reservationStatus = new ReservationStatus(Waiting.CONFIRMED, null);
            entityManager.persist(reservationStatus);

            Reservation reservation = new Reservation(date, time, theme1, member, reservationStatus);
            entityManager.persist(reservation);
        }

        ReservationStatus otherDateStatus = new ReservationStatus(Waiting.CONFIRMED, null);
        entityManager.persist(otherDateStatus);

        Reservation otherDateReservation = new Reservation(date.plusDays(1), time, theme1, member, otherDateStatus);
        entityManager.persist(otherDateReservation);

        entityManager.flush();
        entityManager.clear();

        // when
        long result = reservationRepository.countByDateAndTimeAndTheme(date, time, theme1);

        // then
        assertThat(result).isEqualTo(3);
    }

    @Test
    @DisplayName("대기 예약 취소 시 이후 대기 예약들의 우선순위가 1씩 감소한다.")
    void updateAllWaitingReservationsAfterPriority() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        entityManager.persist(time);
        Theme theme = new Theme("테마", "설명", "이미지");
        entityManager.persist(theme);
        Member member = new Member(null, "이름", "이메일", "비밀번호", Role.USER);
        entityManager.persist(member);

        // 대기 예약들 생성 (우선순위: 1, 2, 3, 4)
        List<Reservation> waitingReservations = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            ReservationStatus status = new ReservationStatus(Waiting.WAITING, (long) i);
            Reservation reservation = new Reservation(date, time, theme, member, status);
            waitingReservations.add(reservation);
        }

        waitingReservations.forEach(reservation -> {
            entityManager.persist(reservation.getStatus());
            entityManager.persist(reservation);
        });

        entityManager.flush();
        entityManager.clear();

        // when
        reservationRepository.updateAllWaitingReservationsAfterPriority(date, time, theme, 2L); // 우선 순위 2번이던 예약이 취소됨
        entityManager.flush();
        entityManager.clear();

        // then
        List<Reservation> updatedReservations = reservationRepository.findAll();
        assertThat(updatedReservations).hasSize(4);

        Assertions.assertAll(
                () -> {
                    assertThat(updatedReservations.get(0).getStatus().getPriority()).isEqualTo(1L);
                    assertThat(updatedReservations.get(1).getStatus().getPriority()).isEqualTo(2L);
                    assertThat(updatedReservations.get(2).getStatus().getPriority()).isEqualTo(2L);
                    assertThat(updatedReservations.get(3).getStatus().getPriority()).isEqualTo(3L);
                }
        );
    }

    private void createReservationsInRange(Theme theme, int count, LocalDate startDate, Member targetMember) {
        for (int i = 0; i < count; i++) {
            ReservationTime time = new ReservationTime(LocalTime.of(10, 0).plusHours(i % 8));
            entityManager.persist(time);

            ReservationStatus reservationStatus = new ReservationStatus(Waiting.CONFIRMED, null);
            entityManager.persist(reservationStatus);

            Reservation reservation = new Reservation(startDate.plusDays(i % 7), time, theme, targetMember,
                    reservationStatus);
            entityManager.persist(reservation);
        }
    }
}
