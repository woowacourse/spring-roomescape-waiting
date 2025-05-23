package roomescape.service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.response.MyReservationResponse;
import roomescape.entity.Member;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaWaitingRepository;

@Service
@Transactional
public class MyReservationService {

    private final JpaReservationRepository reservationRepository;
    private final JpaWaitingRepository waitingRepository;

    public MyReservationService(JpaReservationRepository reservationRepository,
        JpaWaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
    }

    @Transactional(readOnly = true)
    public List<MyReservationResponse> findReservationsAndWaitingsByMemberId(Member member) {
        List<MyReservationResponse> checkedReservations = getCheckedReservation(member);
        List<MyReservationResponse> waitingReservations = getWaitingReservation(member);

        return Stream.concat(checkedReservations.stream(), waitingReservations.stream())
            .sorted(Comparator.comparing(MyReservationResponse::date))
            .toList();
    }

    private List<MyReservationResponse> getCheckedReservation(Member member) {
        return reservationRepository.findByMemberId(member.getId()).stream()
            .map(MyReservationResponse::from)
            .toList();
    }

    private List<MyReservationResponse> getWaitingReservation(Member member) {
        return waitingRepository.findByMemberId(member.getId()).stream()
            .map(w -> MyReservationResponse.from(w, String.format("%d번째", w.getRank())))
            .toList();
    }
}
