package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationServiceResponse;
import roomescape.application.dto.ReservationStatusServiceResponse;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.entity.Waiting;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GameScheduleService gameScheduleService;
    private final MemberService memberService;
    private final WaitingService waitingService;
    private final MessageSource messageSource;

    public ReservationService(
            ReservationRepository reservationRepository,
            GameScheduleService gameScheduleService,
            MemberService memberService,
            WaitingService waitingService,
            MessageSource messageSource
    ) {
        this.reservationRepository = reservationRepository;
        this.gameScheduleService = gameScheduleService;
        this.memberService = memberService;
        this.waitingService = waitingService;
        this.messageSource = messageSource;
    }

    @Transactional
    public ReservationServiceResponse registerReservation(ReservationCreateServiceRequest request) {
        GameSchedule gameSchedule = gameScheduleService.getGameScheduleForReservation(
                request.date(),
                request.timeId(),
                request.themeId()
        );
        validateNotDuplicate(gameSchedule.getId());
        Member member = memberService.getMemberEntityById(request.memberId());

        Reservation reservationWithoutId = Reservation.withoutId(member, gameSchedule);
        Reservation reservation = reservationRepository.save(reservationWithoutId);
        return ReservationServiceResponse.from(reservation);
    }

    private void validateNotDuplicate(Long id) {
        boolean duplicated = reservationRepository.existsByGameScheduleId(id);
        if (duplicated) {
            throw new IllegalArgumentException("이미 예약된 일시입니다. 예약대기를 이용해주세요.");
        }
    }

    public List<ReservationServiceResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ReservationServiceResponse.from(reservations);
    }

    public Reservation getReservationEntityById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당하는 예약 id가 존재하지 않습니다. id: " + id));
    }

    public List<ReservationStatusServiceResponse> getMyReservationsByMember(Long memberId) {
        List<ReservationStatusServiceResponse> reservations = getReservationsByMember(memberId);
        List<ReservationStatusServiceResponse> waitings = waitingService.getWaitingsByMember(memberId);

        return Stream.concat(reservations.stream(), waitings.stream())
                .toList();
    }

    public List<ReservationStatusServiceResponse> getReservationsByMember(Long memberId) {
        List<Reservation> memberReservations = reservationRepository.findByMemberId(memberId);
        return memberReservations.stream()
                .map(reservation -> ReservationStatusServiceResponse.of(reservation, messageSource))
                .toList();
    }

    public List<ReservationServiceResponse> searchReservationsWith(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationRepository.findByMemberAndThemeAndDateRange(
                memberId,
                themeId,
                dateFrom,
                dateTo
        );
        return ReservationServiceResponse.from(reservations);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = getReservationEntityById(id);
        GameSchedule gameSchedule = reservation.getGameSchedule();
        Optional<Waiting> firstWaiting = waitingService.findFirstWaitingEntityByGameSchedule(gameSchedule);
        reservationRepository.deleteById(id);
        firstWaiting.ifPresent(this::approveWaiting);
    }

    private void approveWaiting(Waiting waiting) {
        Reservation reservationWithoutId = Reservation.withoutId(waiting.getMember(), waiting.getGameSchedule());
        reservationRepository.save(reservationWithoutId);
        waitingService.deleteWaiting(waiting.getId());
    }
}
