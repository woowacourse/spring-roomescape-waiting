package roomescape.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import roomescape.domain.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.WaitingRepository;
import roomescape.persistence.dto.WaitingWithRankData;
import roomescape.service.dto.result.MemberBookingResult;

@Service
public class MyPageService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;

    public MyPageService(ReservationRepository reservationRepository, WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    public List<MemberBookingResult> getMyBookings(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        List<WaitingWithRankData> waitings = waitingRepository.findWaitingsWithRankByMemberId(
                memberId);

        return Stream.concat(
                        reservations.stream().map(MemberBookingResult::from),
                        waitings.stream().map(data -> MemberBookingResult.from(data.waiting(), data.rank() + 1))
                ).sorted(Comparator.comparing(MemberBookingResult::reservationDateTime))
                .toList();
    }
}
