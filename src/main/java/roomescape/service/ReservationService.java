package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_RESERVATION;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
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
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Theme;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.mapper.LoginMemberReservationResponseMapper;
import roomescape.service.mapper.ReservationResponseMapper;

@Service
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final ReservationWaitingRepository waitingRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository,
                              MemberRepository memberRepository, ReservationWaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public ReservationResponse save(ReservationRequest reservationRequest) {
        ReservationTime requestedTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme requestedTheme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member member = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));
        Reservation beforeSave = new Reservation(
                member,
                reservationRequest.date(),
                requestedTime,
                requestedTheme
        );

        validateDuplicateReservation(requestedTime, requestedTheme, beforeSave.getDate());
        validatePastTimeReservation(beforeSave);

        Reservation saved = reservationRepository.save(beforeSave);
        return toResponse(saved);
    }

    private void validateDuplicateReservation(ReservationTime requestedTime, Theme requestedTheme, LocalDate date) {
        boolean isDuplicate = reservationRepository.existsByThemeAndDateAndTime(requestedTheme, date, requestedTime);
        if (isDuplicate) {
            throw new RoomescapeException(DUPLICATE_RESERVATION);
        }
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
        Member requestMember = memberRepository.findById(requestMemberId)
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));
        boolean isReservationMember = reservationRepository.findById(reservationId)
                .stream()
                .map(Reservation::getReservationMember)
                .anyMatch(member -> member.equals(requestMember));
        return requestMember.isAdmin() || isReservationMember;
    }
}
