package roomescape.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.WaitingWithRank;
import roomescape.domain.user.Member;
import roomescape.repository.ReservationRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.service.dto.output.ReservationOutput;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final CreateValidator createValidator;

    public ReservationService(final ReservationRepository reservationRepository,
                              final WaitingRepository waitingRepository,
                              final CreateValidator createValidator) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.createValidator = createValidator;
    }

    public ReservationOutput createReservation(final ReservationInput input) {
        final Reservation reservation = createValidator.validateReservationInput(input);
        final Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationOutput.toOutput(savedReservation);
    }

    public List<ReservationOutput> getAllReservations() {
        final List<Reservation> reservations = reservationRepository.findAll();
        return ReservationOutput.toOutputs(reservations);
    }

    public List<ReservationOutput> getAllMyReservations(final Member member) {
        final List<Reservation> reservations = reservationRepository.findAllByMember(member);
        final List<WaitingWithRank> waitings = waitingRepository.findWaitingsWithRankByMemberId(member.getId());
        return ReservationOutput.toOutputs(reservations, waitings);
    }

    public List<ReservationOutput> searchReservation(final ReservationSearchInput input) {
        final List<Reservation> themeReservations = reservationRepository.getReservationByThemeIdAndMemberIdAndDateBetween(
                input.themeId(), input.memberId(), new ReservationDate(input.fromDate()),
                new ReservationDate(input.toDate()));
        return ReservationOutput.toOutputs(themeReservations);
    }

    public void deleteReservation(final long id) {
        Optional<Reservation> reservation = reservationRepository.findById(id);
        if (reservation.isPresent()) {
            reservationRepository.deleteById(id);
            approveWaiting(reservation.get());
        }
    }

    private void approveWaiting(Reservation reservation) {
        Optional<Waiting> waiting = waitingRepository.findFirstByDateAndTimeAndThemeOrderByCreatedAt(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
        waiting.ifPresent(value -> reservationRepository.save(new Reservation(
                null,
                value.getDate(),
                value.getTime(),
                value.getTheme(),
                value.getMember()
        )));
        waiting.ifPresent(waitingRepository::delete);
    }
}
