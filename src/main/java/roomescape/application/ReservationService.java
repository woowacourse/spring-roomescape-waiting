package roomescape.application;

import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.dto.ReservationCreateServiceRequest;
import roomescape.application.dto.ReservationServiceResponse;
import roomescape.application.dto.ReservationStatusServiceResponse;
import roomescape.domain.ReservationStatus;
import roomescape.domain.entity.GameSchedule;
import roomescape.domain.entity.Member;
import roomescape.domain.entity.Reservation;
import roomescape.domain.repository.ReservationRepository;
import roomescape.exception.NotFoundException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final GameScheduleService gameScheduleService;
    private final MemberService memberService;

    public ReservationService(
            ReservationRepository reservationRepository,
            GameScheduleService gameScheduleService,
            MemberService memberService
    ) {
        this.reservationRepository = reservationRepository;
        this.gameScheduleService = gameScheduleService;
        this.memberService = memberService;
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

        Reservation reservationWithoutId = Reservation.withoutId(member, gameSchedule, ReservationStatus.RESERVED);
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
                .orElseThrow(() -> new NotFoundException("삭제하려는 예약 id가 존재하지 않습니다. id: " + id));
    }

    public List<ReservationStatusServiceResponse> getReservationsByMember(Long memberId) {
        Member member = memberService.getMemberEntityById(memberId);
        List<Reservation> memberReservations = reservationRepository.findByMember(member);
        return memberReservations.stream()
                .map(ReservationService::createReservationStatusDto)
                .toList();
    }

    private static ReservationStatusServiceResponse createReservationStatusDto(Reservation reservation) {
        GameSchedule gameSchedule = reservation.getGameSchedule();
        return new ReservationStatusServiceResponse(
                reservation.getId(),
                gameSchedule.getTheme().getName(),
                gameSchedule.getDate(),
                gameSchedule.getTime().getStartAt(),
                ReservationStatus.name(reservation.getStatus())
        );
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
        try {
            reservationRepository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("삭제하려는 예약 id가 존재하지 않습니다. id: " + id);
        }
    }
}
