package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.ForbiddenException;
import roomescape.global.exception.model.NotFoundException;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.MemberReservationResponse;
import roomescape.reservation.dto.response.MemberReservationsResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeInfoResponse;
import roomescape.reservation.dto.response.ReservationTimeInfosResponse;
import roomescape.reservation.dto.response.ReservationsResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberReservationRepository memberReservationRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationRepository reservationRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberReservationRepository memberReservationRepository,
                              final MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberReservationRepository = memberReservationRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationsResponse findReservationsByStatus(final ReservationStatus status) {
        List<MemberReservation> memberReservations = memberReservationRepository.findByStatus(status);
        List<ReservationResponse> response = memberReservations.stream()
                .map(memberReservation -> ReservationResponse.from(memberReservation.getReservation()))
                .toList();
        return new ReservationsResponse(response);
    }

    public ReservationsResponse findFirstOrderWaitingReservations() {
        List<MemberReservation> waitingMemberReservations = memberReservationRepository.findByStatus(ReservationStatus.WAITING);
        List<ReservationResponse> response = waitingMemberReservations.stream()
                .filter(MemberReservation::isFirstWaitingOrder)
                .map(memberReservation -> ReservationResponse.from(memberReservation.getReservation()))
                .toList();

        return new ReservationsResponse(response);
    }

    public ReservationTimeInfosResponse findReservationsByDateAndThemeId(final LocalDate date, final Long themeId) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        Theme theme = themeRepository.getById(themeId);
        List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        List<ReservationTimeInfoResponse> response = new ArrayList<>();
        for (ReservationTime time : allTimes) {
            boolean alreadyBooked = false;
            for (Reservation reservation : reservations) {
                if (reservation.getReservationTime() == time) {
                    alreadyBooked = true;
                    break;
                }
            }
            response.add(new ReservationTimeInfoResponse(time.getId(), time.getStartAt(), alreadyBooked));
        }

        return new ReservationTimeInfosResponse(response);
    }

    @Transactional
    public void removeReservationById(final Long reservationId, final Long requestMemberId) {
        Member member = memberRepository.getById(requestMemberId);
        Reservation reservation = reservationRepository.getById(reservationId);
        MemberReservation memberReservation = findMemberReservationByReservation(reservation);

        Long reservationMemberId = reservation.getMember().getId();

        if (member.isAdmin() || reservationMemberId.equals(requestMemberId)) {
            memberReservationRepository.deleteById(memberReservation.getId());
            reservationRepository.deleteById(reservation.getId());
            changeOrdersStatus(reservation);
        } else {
            throw new ForbiddenException(ErrorType.PERMISSION_DOES_NOT_EXIST,
                    String.format("예약 정보에 대한 삭제 권한이 존재하지 않습니다. [reservationId: %d, memberReservationId: %d]"
                            , reservationId, memberReservation.getId()));
        }
    }

    @Transactional
    public void approveWaitingReservation(final Long reservationId) {
        Reservation reservation = reservationRepository.getById(reservationId);
        MemberReservation memberReservation = findMemberReservationByReservation(reservation);
        changeWaitingOrdersStatus(reservation);

        memberReservation.changeStatusToReserve();
    }

    private void changeOrdersStatus(final Reservation reservation) {
        List<MemberReservation> waitingReservations = memberReservationRepository.findByReservationTimeAndDateAndThemeOrderByIdAsc(reservation.getReservationTime(), reservation.getDate(), reservation.getTheme());
        for (MemberReservation waitingReservation : waitingReservations) {
            waitingReservation.increaseOrder();
            if (waitingReservation.isReserveOrder()) {
                waitingReservation.changeStatusToReserve();
            }
        }
    }

    @Transactional
    public void removeWaitingReservationById(final Long reservationId, final Long memberId) {
        Member member = memberRepository.getById(memberId);
        Reservation reservation = reservationRepository.getById(reservationId);
        MemberReservation memberReservation = findMemberReservationByReservation(reservation);

        Long reservationMemberId = reservation.getMember().getId();

        if (member.isAdmin() || reservationMemberId.equals(memberId)) {
            memberReservationRepository.deleteById(memberReservation.getId());
            reservationRepository.deleteById(reservation.getId());
            changeWaitingOrdersStatus(reservation);
        } else {
            throw new ForbiddenException(ErrorType.PERMISSION_DOES_NOT_EXIST,
                    String.format("예약 정보에 대한 삭제 권한이 존재하지 않습니다. [reservationId: %d, memberReservationId: %d]"
                            , reservationId, memberReservation.getId()));
        }
    }

    private void changeWaitingOrdersStatus(final Reservation reservation) {
        List<MemberReservation> waitingReservations = memberReservationRepository.findByReservationTimeAndDateAndThemeOrderByIdAsc(reservation.getReservationTime(), reservation.getDate(), reservation.getTheme());
        for (MemberReservation waitingReservation : waitingReservations) {
            if (!waitingReservation.isReserved()) {
                waitingReservation.increaseOrder();
            }
        }
    }

    private MemberReservation findMemberReservationByReservation(final Reservation reservation) {
        return memberReservationRepository.findByReservation(reservation)
                .orElseThrow(() -> new NotFoundException(ErrorType.MEMBER_RESERVATION_NOT_FOUND,
                        ErrorType.MEMBER_RESERVATION_NOT_FOUND.getDescription()));
    }

    @Transactional
    public ReservationResponse addReservation(final ReservationRequest request, final Long memberId) {
        ReservationTime time = reservationTimeRepository.getById(request.timeId());
        Theme theme = themeRepository.getById(request.themeId());
        Member member = memberRepository.getById(memberId);
        List<MemberReservation> alreadyBookedReservations = memberReservationRepository.findByReservationTimeAndDateAndThemeOrderByIdAsc(time, request.date(), theme);
        long order = alreadyBookedReservations.size();

        Reservation requestReservation = request.toEntity(time, theme, member);
        MemberReservation requestMemberReservation = new MemberReservation(requestReservation, member, request.status(), order);

        validateDateAndTime(requestReservation, LocalDateTime.now());
        validateDuplication(requestReservation, requestMemberReservation.getStatus());
        validateCanReserve(requestReservation, requestMemberReservation.getStatus(), order);

        Reservation savedReservation = reservationRepository.save(requestReservation);
        memberReservationRepository.save(requestMemberReservation);
        return ReservationResponse.from(savedReservation);
    }

    private void validateDateAndTime(final Reservation reservation, final LocalDateTime now) {
        if (reservation.isPastThen(now)) {
            throw new ValidateException(ErrorType.RESERVATION_PERIOD_IN_PAST,
                    String.format("지난 날짜나 시간은 예약이 불가능합니다. [now: %s %s | request: %s %s]",
                            now.toLocalDate(), now.toLocalTime(), reservation.getDate(), reservation.getStartAt()));
        }
    }

    private void validateDuplication(final Reservation requestReservation, final ReservationStatus requestStatus) {
        if (requestStatus.isReserved()) {
            validateReservationDuplicate(requestReservation);
        } else if (requestStatus.isWaiting()) {
            validateMemberReservationDuplicate(requestReservation);
        }
    }

    private void validateCanReserve(final Reservation requestReservation, final ReservationStatus requestReserveStatus, final long canReserveOrder) {
        if (requestReserveStatus.isReserved() && canReserveOrder > 0) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA,
                    String.format("이미 요청하신 날짜/테마/시간 에 예약 정보가 존재하여 예약할 수 없습니다. '예약 대기'로 요청해주세요. [values: %s/%s/%s]",
                            requestReservation.getReservationTime(), requestReservation.getDate(), requestReservation.getTheme()));
        }
    }

    private void validateReservationDuplicate(final Reservation requestReservation) {
        Optional<Reservation> reservation = reservationRepository.findByReservationTimeAndDateAndTheme(
                requestReservation.getReservationTime(), requestReservation.getDate(), requestReservation.getTheme());

        if (reservation.isPresent()) {
            throw new DataDuplicateException(ErrorType.RESERVATION_WAITING_DUPLICATED,
                    String.format("이미 해당 날짜/시간/테마에 예약이 존재합니다. [values: %s/%s/%s]",
                            requestReservation.getReservationTime(), requestReservation.getDate(), requestReservation.getTheme()));
        }
    }

    private void validateMemberReservationDuplicate(final Reservation requestReservation) {
        Optional<MemberReservation> memberReservation = memberReservationRepository.findByMemberAndReservationTimeAndDateAndTheme(
                requestReservation.getMember(), requestReservation.getReservationTime(), requestReservation.getDate(), requestReservation.getTheme());

        if (memberReservation.isPresent()) {
            throw new DataDuplicateException(ErrorType.RESERVATION_DUPLICATED,
                    String.format("이미 해당 날짜/시간/테마에 예약 또는 예약대기 중 입니다. [values: %s/%s/%s]",
                            requestReservation.getReservationTime(), requestReservation.getDate(), requestReservation.getTheme()));
        }
    }

    public ReservationsResponse searchWith(
            final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo) {
        Member member = memberRepository.getById(memberId);
        Theme theme = themeRepository.getById(themeId);

        List<ReservationResponse> response = reservationRepository.searchWith(theme, member, dateFrom, dateTo).stream()
                .map(ReservationResponse::from)
                .toList();
        return new ReservationsResponse(response);
    }

    public MemberReservationsResponse findReservationByMemberId(final Long memberId) {
        Member member = memberRepository.getById(memberId);
        List<MemberReservation> reservations = memberReservationRepository.findByMemberOrderByDateTimeAsc(member);

        List<MemberReservationResponse> responses = new ArrayList<>();
        for (MemberReservation memberReservation : reservations) {
            responses.add(MemberReservationResponse.fromEntity(memberReservation));
        }
        return new MemberReservationsResponse(responses);
    }
}
