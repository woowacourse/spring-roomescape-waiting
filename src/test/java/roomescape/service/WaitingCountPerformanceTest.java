package roomescape.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingRepository;
import roomescape.domain.reservation.dto.WaitingWithRank;
import roomescape.domain.reservation.slot.ReservationSlot;
import roomescape.domain.reservation.slot.ReservationTime;
import roomescape.domain.reservation.slot.ReservationTimeRepository;
import roomescape.domain.reservation.slot.Theme;
import roomescape.domain.reservation.slot.ThemeRepository;

@DataJpaTest
class WaitingCountPerformanceTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @BeforeEach
    void setUp() {
        Member member1 = memberRepository.findById(1L).get();
        Member member2 = memberRepository.findById(2L).get();

        ReservationTime time = reservationTimeRepository.findById(1L).get();
        Theme theme = themeRepository.findById(1L).get();
        LocalDate today = LocalDate.parse("2024-06-01");

        List<Reservation> reservations = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            LocalDate date = today.plusDays(i);
            ReservationSlot slot = new ReservationSlot(date, time, theme);
            Reservation reservation = new Reservation(member1, slot);

            reservationRepository.save(reservation);
            reservations.add(reservation);
        }

        for (Reservation reservation : reservations) {
            reservation.addWaiting(member2);
        }

        entityManager.flush();
        entityManager.clear();
    }

    @DisplayName("한방 쿼리 성능")
    @Test
    void once() {
        // given
        Member member = memberRepository.findById(2L).get();
        long beforeTime = System.currentTimeMillis();

        // when
        List<WaitingWithRank> waitings = waitingRepository.findWaitingRankByMemberAndDateAfter(
                member, LocalDate.parse("2024-06-01"));

        // then
        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        System.out.println("실행 시간(ms): " + diffTime);
    }

    @DisplayName("쿼리 분리 성능")
    @Test
    void twice() {
        // given
        Member member = memberRepository.findById(2L).get();
        long beforeTime = System.currentTimeMillis();

        // when
        List<Waiting> waitings = waitingRepository.findByMemberAndDateAfter(
                member, LocalDate.parse("2024-06-01"));

        List<WaitingWithRank> results = waitings.stream()
                .map(waiting -> {
                    Long rank = waitingRepository.countRank(waiting.getReservation(), waiting.getId());
                    return new WaitingWithRank(waiting, rank);
                })
                .toList();

        // then
        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        System.out.println("실행 시간(ms): " + diffTime);
    }
}
