package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationDate;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.service.dto.output.ReservationOutput;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationCreateValidator reservationCreateValidator;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationCreateValidator reservationCreateValidator) {
        this.reservationRepository = reservationRepository;
        this.reservationCreateValidator = reservationCreateValidator;
    }

    public ReservationOutput createReservation(final ReservationInput input) {
        final ReservationInfo reservationInfo = reservationCreateValidator.validateReservationInput(input);
        final ReservationInfo savedReservationInfo = reservationRepository.save(reservationInfo);
        return ReservationOutput.toOutput(savedReservationInfo);
    }

    public List<ReservationOutput> getAllReservations() {
        final List<ReservationInfo> reservationInfos = reservationRepository.findAll();
        return ReservationOutput.toOutputs(reservationInfos);
    }

    public List<ReservationOutput> getAllMyReservations(final long memberId) {
        final List<ReservationInfo> reservationInfos = reservationRepository.findAllByMemberId(memberId);
        return ReservationOutput.toOutputs(reservationInfos);
    }

    public List<ReservationOutput> searchReservation(final ReservationSearchInput input) {
        final List<ReservationInfo> themeReservationInfos = reservationRepository.getReservationByThemeIdAndMemberIdAndDateBetween(
                input.themeId(), input.memberId(), new ReservationDate(input.fromDate()), new ReservationDate(input.toDate()));
        return ReservationOutput.toOutputs(themeReservationInfos);
    }

    public void deleteReservation(final long id) {
        reservationRepository.deleteById(id);
    }
}
