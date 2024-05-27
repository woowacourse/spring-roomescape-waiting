package roomescape.reservation.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Status;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.ReservationSearchCondRequest;
import roomescape.reservation.dto.response.MemberReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.repository.ReservationRepository;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationFactoryService reservationFactoryService;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationFactoryService reservationFactoryService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationFactoryService = reservationFactoryService;
    }

    @Transactional
    public ReservationResponse save(ReservationSaveRequest saveRequest) {
        Reservation reservation = reservationFactoryService.createSuccess(saveRequest);
        validateMemberReservationUnique(reservation);
        validateReservationAvailable(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);

        return ReservationResponse.toResponse(savedReservation);
    }

    private void validateMemberReservationUnique(Reservation reservation) {
        Optional<Reservation> duplicatedReservation = reservationRepository.findFirstByDateAndReservationTimeAndThemeAndMember(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getMember()
        );
        duplicatedReservation.ifPresent(this::throwExceptionByStatus);
    }

    private void throwExceptionByStatus(Reservation memberReservation) {
        if (memberReservation.isWaitingReservation()) {
            throw new IllegalArgumentException("이미 회원이 예약 대기한 내역이 있습니다.");
        }
        if (memberReservation.isSuccessReservation()) {
            throw new IllegalArgumentException("이미 회원이 예약한 내역이 있습니다.");
        }
    }

    private void validateReservationAvailable(Reservation reservation) {
        Optional<Reservation> savedReservation = reservationRepository.findFirstByDateAndReservationTimeAndTheme(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        if (savedReservation.isPresent()) {
            throw new IllegalArgumentException("예약이 다 찼습니다. 예약 대기를 걸어주세요.");
        }
    }

    public ReservationResponse findById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        return ReservationResponse.toResponse(reservation);
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllByStatus(Status.SUCCESS)
                .stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    public List<ReservationResponse> findAllBySearchCond(ReservationSearchCondRequest request) {
        return reservationRepository.findAllByThemeIdAndMemberIdAndDateBetweenAndStatus(
                        request.themeId(),
                        request.memberId(),
                        request.dateFrom(),
                        request.dateTo(),
                        Status.SUCCESS
                ).stream()
                .map(ReservationResponse::toResponse)
                .toList();
    }

    public List<MemberReservationResponse> findReservationsAndWaitingsByMember(LoginMember loginMember) {
        return reservationRepository.findReservationWithRanksByMemberId(loginMember.id())
                .stream()
                .map(MemberReservationResponse::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.deleteById(id);
    }
}
