package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.user.Member;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.service.dto.output.ReservationOutput;

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
        final Reservation reservation = reservationCreateValidator.validateReservationInput(input);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationOutput.toOutput(savedReservation);
    }

    public List<ReservationOutput> getAllReservations() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return ReservationOutput.toOutputs(reservations);
    }

    public List<ReservationOutput> getAllMyReservations(final Member member) {
        final List<Reservation> reservations = reservationRepository.findAllByMember(member);
        return ReservationOutput.toOutputs(reservations);
    }

    public List<ReservationOutput> searchReservation(final ReservationSearchInput input) {
        final List<Reservation> themeReservations = reservationRepository.getReservationByThemeIdAndMemberIdAndDateBetween(
                input.themeId(), input.memberId(), new ReservationDate(input.fromDate()),
                new ReservationDate(input.toDate()));
        return ReservationOutput.toOutputs(themeReservations);
    }

    public void deleteReservation(final long id) {
        reservationRepository.deleteById(id);
    }
}
