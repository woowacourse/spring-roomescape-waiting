package roomescape.reservation.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.reservation.controller.request.WaitingCreateRequest;
import roomescape.reservation.controller.response.MemberReservationResponse;
import roomescape.reservation.controller.response.WaitingResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.domain.WaitingWithRank;
import roomescape.reservation.service.exception.MemberAlreadyHasThisReservationException;
import roomescape.reservation.service.exception.WaitingDuplicateException;

@Service
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final ReservationService reservationService;

    public WaitingService(final WaitingRepository waitingRepository, final ReservationService reservationService) {
        this.waitingRepository = waitingRepository;
        this.reservationService = reservationService;
    }

    @Transactional
    public WaitingResponse create(MemberResponse memberResponse, WaitingCreateRequest request) {
        ReservationDate reservationDate = new ReservationDate(request.date());

        Reservation reservation = reservationService.findById(
                reservationService.findByReservation(request.themeId(), request.timeId(), reservationDate.getDate())
                        .id()
        );

        if (reservation.getMember().getId().equals(memberResponse.id())) {
            throw new MemberAlreadyHasThisReservationException("[ERROR] 이미 해당 예약이 등록되셨습니다.");
        }

        if (waitingRepository.existsBySameReservation(
                memberResponse.id(), reservation.getTheme().getId(),
                reservation.getReservationTime().getId(), reservation.getDate())) {
            throw new WaitingDuplicateException("[ERROR] 이미 대기열에 등록되어 있습니다.");
        }

        Member member = new Member(memberResponse.id(), new Name(memberResponse.name()),
                new Email(memberResponse.email()), memberResponse.role());
        Waiting waiting = Waiting.create(reservation, member);

        Waiting created = waitingRepository.save(waiting);
        return WaitingResponse.from(created);
    }


    @Transactional
    public void deleteById(Long id) {
        WaitingResponse waitingResponse = findById(id);
        waitingRepository.deleteById(waitingResponse.id());
    }

    @Transactional(readOnly = true)
    public WaitingResponse findById(Long id) {
        Optional<Waiting> waiting = waitingRepository.findById(id);
        if (waiting.isPresent()) {
            return WaitingResponse.from(waiting.get());
        }
        throw new NoSuchElementException("[ERROR] 예약 대기를 찾을 수 없습니다.");
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findAllByMemberId(Long id) {
        List<Waiting> memberWaitings = waitingRepository.findAllByMemberId(id);
        List<Waiting> allWaitings = waitingRepository.findAll();
        Map<Long, List<Waiting>> waitingsByReservationId = allWaitings.stream()
                .collect(Collectors.groupingBy(waiting -> waiting.getReservation().getId()));
        List<WaitingWithRank> waitingWithRanks = memberWaitings.stream()
                .map(waiting -> {
                    Long reservationId = waiting.getReservation().getId();
                    List<Waiting> waitingsForReservation = waitingsByReservationId
                            .getOrDefault(reservationId, Collections.emptyList())
                            .stream()
                            .sorted(Comparator.comparing(Waiting::getId))
                            .toList();
                    long rank = 0;
                    for (int i = 0; i < waitingsForReservation.size(); i++) {
                        if (waitingsForReservation.get(i).getId().equals(waiting.getId())) {
                            rank = i + 1;
                            break;
                        }
                    }
                    return new WaitingWithRank(waiting, rank);
                })
                .toList();
        return waitingWithRanks.stream()
                .map(MemberReservationResponse::from)
                .toList();
    }
}
