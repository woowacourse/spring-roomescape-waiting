package roomescape.reservation.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;

@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class ConcurrencyTest {

    @Autowired
    ReservationService reservationService;

    @Autowired
    MemberRepository memberRepository;

    ReservationSaveRequest reservationSaveRequest;
    List<Member> members = new ArrayList<>();

    @BeforeEach
    void setUp() {
        reservationSaveRequest = new ReservationSaveRequest(1L, LocalDate.now().plusDays(1L), 1L);
        members.add(memberRepository.findById(1L).get());
        members.add(memberRepository.findById(2L).get());
        members.add(memberRepository.findById(3L).get());
    }


    @Test
    void concurrencyReserveTest() throws InterruptedException {
        int numberOfThreads = 3;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            service.execute(() -> {
                reservationService.reserve(reservationSaveRequest, members.get(index));
                latch.countDown();
            });
        }
        latch.await();

        List<Reservation> allReservations = reservationService.getAllReservations();
        assertThat(allReservations).hasSize(8);
        assertThat(allReservations.get(5).getStatus()).isEqualTo(Status.RESERVED);
        assertThat(allReservations.get(6).getStatus()).isEqualTo(Status.PENDING);
        assertThat(allReservations.get(7).getStatus()).isEqualTo(Status.PENDING);
    }
}
