package roomescape.service;

import org.springframework.stereotype.Service;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationSearchInput;
import roomescape.service.dto.output.ReservationOutput;

import java.util.List;

import static roomescape.exception.ExceptionDomainType.RESERVATION;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationInfoCreateValidator reservationInfoCreateValidator;

    public ReservationService(final ReservationRepository reservationRepository, final MemberRepository memberRepository,
                              final ReservationInfoCreateValidator reservationInfoCreateValidator) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationInfoCreateValidator = reservationInfoCreateValidator;
    }

    public ReservationOutput createReservation(final ReservationInput input) {
        final ReservationInfo reservationInfo = reservationInfoCreateValidator.validateReservationInput(input.parseReservationInfoInput());
        if (reservationRepository.existsByReservationInfoId(reservationInfo.getId())) {
            throw new AlreadyExistsException(RESERVATION, reservationInfo.getLocalDateTimeFormat());
        }
        final Member member = memberRepository.getById(input.memberId());
        final Reservation reservation = reservationRepository.save(new Reservation(member, reservationInfo));
        return ReservationOutput.toOutput(reservation);
    }

    public List<ReservationOutput> getAllReservations() {
        final List<Reservation> reservationInfos = reservationRepository.findAll();
        return ReservationOutput.toOutputs(reservationInfos);
    }

    public List<ReservationOutput> getAllMyReservations(final long memberId) {
        final List<Reservation> reservationInfos = reservationRepository.findAllByMemberId(memberId);
        return ReservationOutput.toOutputs(reservationInfos);
    }

    public List<ReservationOutput> searchReservation(final ReservationSearchInput input) {
        final List<Reservation> themeReservationInfos = reservationRepository.getReservationByReservationInfoThemeIdAndMemberIdAndReservationInfoDateBetween(
                input.themeId(), input.memberId(), new ReservationDate(input.fromDate()), new ReservationDate(input.toDate()));
        return ReservationOutput.toOutputs(themeReservationInfos);
    }

    public void deleteReservation(final long id) {
        reservationRepository.deleteById(id);
    }
}
