package roomescape.service;

import static roomescape.exception.ExceptionType.DUPLICATE_WAITING;
import static roomescape.exception.ExceptionType.NOT_FOUND_MEMBER;
import static roomescape.exception.ExceptionType.NOT_FOUND_RESERVATION_TIME;
import static roomescape.exception.ExceptionType.NOT_FOUND_THEME;
import static roomescape.exception.ExceptionType.PAST_TIME_RESERVATION;
import static roomescape.exception.ExceptionType.PERMISSION_DENIED;
import static roomescape.exception.ExceptionType.WAITING_WITHOUT_RESERVATION;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.Role;
import roomescape.domain.Theme;
import roomescape.dto.LoginMemberReservationResponse;
import roomescape.dto.ReservationRequest;
import roomescape.dto.ReservationWaitingResponse;
import roomescape.exception.RoomescapeException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.mapper.LoginMemberReservationResponseMapper;
import roomescape.service.mapper.ReservationWaitingResponseMapper;

@Service
public class ReservationWaitingService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationWaitingRepository waitingRepository;

    public ReservationWaitingService(ReservationRepository reservationRepository,
                                     ReservationTimeRepository reservationTimeRepository,
                                     MemberRepository memberRepository,
                                     ThemeRepository themeRepository, ReservationWaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
    }

    //Todo Reservation 서비스와 Waiting Service 사이에 중복 제거 => 추가적인 계층 분리?
    public ReservationWaitingResponse save(ReservationRequest reservationRequest) {
        ReservationTime requestedTime = reservationTimeRepository.findById(reservationRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_RESERVATION_TIME));
        Theme requestedTheme = themeRepository.findById(reservationRequest.themeId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_THEME));
        Member waitingMember = memberRepository.findById(reservationRequest.memberId())
                .orElseThrow(() -> new RoomescapeException(NOT_FOUND_MEMBER));
        Reservation reservation = reservationRepository.findByThemeAndDateAndTime(requestedTheme,
                        reservationRequest.date(), requestedTime)
                .orElseThrow(() -> new RoomescapeException(WAITING_WITHOUT_RESERVATION));
        validatePastTimeReservation(reservation);
        validateDuplicateWaiting(reservation, waitingMember);

        ReservationWaiting beforeSave = new ReservationWaiting(reservation, waitingMember);
        ReservationWaiting save = waitingRepository.save(beforeSave);

        return ReservationWaitingResponseMapper.toResponse(save, waitingRepository.findByReservation(reservation));
    }

    private void validatePastTimeReservation(Reservation beforeSave) {
        if (beforeSave.isBefore(LocalDateTime.now())) {
            throw new RoomescapeException(PAST_TIME_RESERVATION);
        }
    }

    private void validateDuplicateWaiting(Reservation reservation, Member waitingMember) {
        boolean isDuplicate = waitingRepository.existsByReservationAndWaitingMember(reservation, waitingMember);
        if (isDuplicate) {
            throw new RoomescapeException(DUPLICATE_WAITING);
        }
    }

    public List<ReservationWaitingResponse> findAll() {
        return waitingRepository.findAll().stream()
                .map(ReservationWaitingResponseMapper::toResponseWithoutPriority)
                .toList();
    }

    public List<LoginMemberReservationResponse> findByMemberId(long memberId) {
        List<ReservationWaiting> allByMemberId = waitingRepository.findAllByMemberId(memberId);
        return allByMemberId
                .stream()
                .map(waiting -> ReservationWaitingResponseMapper.toResponse(waiting, allByMemberId))
                .map(LoginMemberReservationResponseMapper::from)
                .toList();
    }

    public void delete(long memberId, long waitingId) {
        if (canDelete(memberId, waitingId)) {
            waitingRepository.delete(waitingId);
        } else {
            throw new RoomescapeException(PERMISSION_DENIED);
        }
    }

    private boolean canDelete(long memberId, long waitingId) {
        Role role = memberRepository.findById(memberId)
                .map(Member::getRole)
                .orElse(Role.MEMBER);
        return Role.ADMIN.equals(role) || waitingRepository.findAllByMemberId(memberId).stream()
                .map(ReservationWaiting::getId)
                .anyMatch(id -> id == waitingId);
    }
}
