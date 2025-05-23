package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dto.query.WaitingWithRank;
import roomescape.dto.request.AdminCreateReservationRequest;
import roomescape.dto.request.CreateReservationRequest;
import roomescape.dto.request.CreateWaitingRequest;
import roomescape.dto.request.LoginMemberRequest;
import roomescape.entity.ConfirmedReservation;
import roomescape.entity.WaitingReservation;

@Service
public class ReservationService {
    private final ConfirmReservationService confirmReservationService;
    private final WaitingReservationService waitingReservationService;

    public ReservationService(ConfirmReservationService confirmReservationService,
                              WaitingReservationService waitingReservationService) {
        this.confirmReservationService = confirmReservationService;
        this.waitingReservationService = waitingReservationService;
    }

    public ConfirmedReservation addReservation(CreateReservationRequest request,
                                               LoginMemberRequest loginMemberRequest) {
        return confirmReservationService.addReservation(request, loginMemberRequest);
    }

    public ConfirmedReservation addConfirmReservationByAdmin(AdminCreateReservationRequest request) {
        return confirmReservationService.addReservationByAdmin(request);
    }

    public List<ConfirmedReservation> findAllConfirmReservation() {
        return confirmReservationService.findAll();
    }

    public List<ConfirmedReservation> findAllConfirmReservationByFilter(
            Long memberId,
            Long themeId,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        return confirmReservationService.findAllByFilter(memberId, themeId, dateFrom, dateTo);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Optional<ConfirmedReservation> deleteReservation = confirmReservationService.findById(id);
        if (deleteReservation.isEmpty()) {
            return;
        }
        ConfirmedReservation confirmedReservation = deleteReservation.get();
        Optional<WaitingReservation> waitingReservation = waitingReservationService.findFirstWaitingByDateAndThemeAndTime(
                confirmedReservation.getDate(),
                confirmedReservation.getTheme(),
                confirmedReservation.getReservationTime());
        confirmReservationService.deleteReservation(id);
        if (waitingReservation.isEmpty()){
            return;
        }
        confirmReservationService.addReservation(waitingReservation.get());
        waitingReservationService.deleteById(waitingReservation.get().getId());
    }

    public List<ConfirmedReservation> findAllReservationByMember(final Long memberId) {
        return confirmReservationService.findAllReservationByMember(memberId);
    }

    public WaitingReservation addWaiting(CreateWaitingRequest request, LoginMemberRequest loginMemberRequest) {
        return waitingReservationService.addWaiting(request, loginMemberRequest);
    }

    public List<WaitingWithRank> findALlWaitingWithRank(Long memberId) {
        return waitingReservationService.findALlWaitingWithRank(memberId);
    }

    public void deleteWaiting(Long id) {
        waitingReservationService.deleteById(id);
    }

    public List<WaitingReservation> findAllWaitingReservation() {
        return waitingReservationService.findAll();
    }
}
