package roomescape.service;

import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;
import static roomescape.exception.ExceptionType.PERMISSION_DENIED;
import static roomescape.service.mapper.ReservationResponseMapper.toResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.BaseEntity;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.finder.MemberFinder;
import roomescape.service.finder.ReservationFinder;
import roomescape.service.mapper.LoginMemberReservationResponseMapper;
import roomescape.service.mapper.ReservationResponseMapper;

@Service
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationWaitingRepository waitingRepository;
    private final ReservationFinder reservationFinder;
    private final MemberFinder memberFinder;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationWaitingRepository waitingRepository,
                              ReservationFinder reservationFinder, MemberFinder memberFinder) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.reservationFinder = reservationFinder;
        this.memberFinder = memberFinder;
    }

    public ReservationResponse save(ReservationRequest reservationRequest) {
        Reservation beforeSave = reservationFinder.createWhenNotExists(reservationRequest);

        validatePastTimeReservation(beforeSave);

        Reservation saved = reservationRepository.save(beforeSave);
        return toResponse(saved);
    }

    private void validatePastTimeReservation(Reservation beforeSave) {
        if (beforeSave.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream()
                .map(ReservationResponseMapper::toResponse)
                .toList();
    }

    public List<ReservationResponse> findByMemberAndThemeBetweenDates(long memberId, long themeId, LocalDate start,
                                                                      LocalDate end) {
        return reservationRepository.findByMemberAndThemeBetweenDates(memberId, themeId, start, end)
                .stream()
                .map(ReservationResponseMapper::toResponse)
                .toList();
    }

    public List<LoginMemberReservationResponse> findByMemberId(long memberId) {
        return reservationRepository.findByMemberId(memberId)
                .stream()
                .map(LoginMemberReservationResponseMapper::toResponse)
                .toList();
    }

    public void delete(long requestMemberId, long reservationId) {
        if (!canDelete(requestMemberId, reservationId)) {
            throw new RoomescapeException(PERMISSION_DENIED);
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION));
        List<ReservationWaiting> waitings = new ArrayList<>(waitingRepository.findByReservation(reservation));
        if (waitings.isEmpty()) {
            reservationRepository.delete(reservationId);
            return;
        }

        waitings.sort(Comparator.comparing(BaseEntity::getCreateAt));
        reservation.updateReservationMember(waitings.get(0).getWaitingMember());
        waitingRepository.delete(waitings.get(0).getId());
    }

    private boolean canDelete(long requestMemberId, long reservationId) {
        Member requestMember = memberFinder.findById(requestMemberId);
        boolean isReservationMember = reservationRepository.findById(reservationId)
                .stream()
                .map(Reservation::getReservationMember)
                .anyMatch(member -> member.equals(requestMember));
        return requestMember.isAdmin() || isReservationMember;
    }
}
