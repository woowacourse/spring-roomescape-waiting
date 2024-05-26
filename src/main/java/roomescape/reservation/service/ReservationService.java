package roomescape.reservation.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.ForbiddenException;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationDetail;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationDetailRepository;
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
    private final ReservationDetailRepository reservationDetailRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberReservationRepository memberReservationRepository;
    private final MemberRepository memberRepository;

    public ReservationService(final ReservationDetailRepository reservationDetailRepository,
                              final ReservationTimeRepository reservationTimeRepository,
                              final ThemeRepository themeRepository,
                              final MemberReservationRepository memberReservationRepository,
                              final MemberRepository memberRepository) {
        this.reservationDetailRepository = reservationDetailRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberReservationRepository = memberReservationRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationsResponse findReservationsByStatus(final ReservationStatus status) {
        List<MemberReservation> memberReservations = memberReservationRepository.findByStatus(status);
        List<ReservationResponse> response = memberReservations.stream()
                .map(ReservationResponse::from)
                .toList();

        return new ReservationsResponse(response);
    }

    public ReservationsResponse findFirstOrderWaitingReservations() {
        List<MemberReservation> firstOrderWaitingReservations = memberReservationRepository
                .findFirstOrderMemberReservationsByStatus(ReservationStatus.WAITING);

        List<ReservationResponse> response = firstOrderWaitingReservations.stream()
                .map(ReservationResponse::from)
                .toList();

        return new ReservationsResponse(response);
    }

    public ReservationTimeInfosResponse findReservationsByDateAndThemeId(final LocalDate date, final Long themeId) {
        List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        Theme theme = themeRepository.getById(themeId);
        List<ReservationDetail> reservations = reservationDetailRepository.findByDateAndTheme(date, theme);

        List<ReservationTimeInfoResponse> response = new ArrayList<>();
        for (ReservationTime time : allTimes) {
            boolean alreadyBooked = false;
            for (ReservationDetail reservation : reservations) {
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
    public void removeMemberReservationById(final Long memberReservationId, final Long requestMemberId, final ReservationStatus status) {
        Member member = memberRepository.getById(requestMemberId);
        MemberReservation memberReservation = memberReservationRepository.getById(memberReservationId);
        if (status.isWaiting()) {
            validateIsWaitingStatus(memberReservation);
        }

        Long reservationMemberId = memberReservation.getMember().getId();
        if (member.isAdmin() || reservationMemberId.equals(requestMemberId)) {
            memberReservationRepository.deleteById(memberReservation.getId());
        } else {
            throw new ForbiddenException(ErrorType.PERMISSION_DOES_NOT_EXIST,
                    String.format("예약 정보에 대한 삭제 권한이 존재하지 않습니다. [memberReservationId: %d]"
                            , memberReservationId));
        }
    }

    @Transactional
    public void approveWaitingReservation(final Long memberReservationId) {
        MemberReservation waitingMemberReservation = memberReservationRepository.getById(memberReservationId);

        validateIsWaitingStatus(waitingMemberReservation);
        validateMemberReservationIsFirstOrder(waitingMemberReservation);

        waitingMemberReservation.changeStatusToReserve();
    }

    private void validateIsWaitingStatus(final MemberReservation memberReservation) {
        if (memberReservation.isReservedStatus()) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA, "'예약대기(WAITING)' 상태의 예약만 예약 승인이 가능합니다.");
        }
    }

    private void validateMemberReservationIsFirstOrder(final MemberReservation memberReservationToApprove) {
        int order = 0;
        List<MemberReservation> memberReservationsOnReservationDetail = memberReservationRepository.findFirstOrderWaitingMemberReservationByReservationDetail(memberReservationToApprove.getReservationDetail());
        for (MemberReservation memberReservation : memberReservationsOnReservationDetail) {
            if (memberReservation.getId().equals(memberReservationToApprove.getId())) {
                break;
            }
            order++;
        }

        if (order != 0) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA,
                    "예약 승인을 요청한 대기 중인 예약이, 1순위 대기 상태가 아니어서 예약 대기 상태를 예약 상태로 변경 수 없습니다.");
        }
    }

    private void validateAlreadyBookedReservationNotExist(final ReservationDetail requestReservation, final Long order) {
        if (order > 0) {
            throw new DataDuplicateException(ErrorType.RESERVATION_WAITING_DUPLICATED,
                    String.format("이미 요청하신 날짜/테마/시간 에 예약 정보가 존재하여 예약할 수 없습니다. '예약 대기'로 요청해주세요. [values: %s/%s/%s]",
                            requestReservation.getReservationTime(), requestReservation.getDate(), requestReservation.getTheme()));
        }
    }

    @Transactional
    public ReservationResponse addMemberReservation(final ReservationRequest request, final Long memberId, final ReservationStatus status) {
        Member member = memberRepository.getById(memberId);
        ReservationDetail requestReservationDetail = getReservationDetail(request);

        MemberReservation memberReservation = memberReservationRepository.save(createMemberReservation(requestReservationDetail, member, status));
        return ReservationResponse.from(memberReservation);
    }

    private ReservationDetail getReservationDetail(final ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.getById(request.timeId());
        Theme theme = themeRepository.getById(request.themeId());

        Optional<ReservationDetail> requestReservationDetail = reservationDetailRepository.findByReservationTimeAndDateAndTheme(time, request.date(), theme);
        return requestReservationDetail.orElseGet(() ->
                reservationDetailRepository.save(request.toEntity(time, theme)));
    }

    private MemberReservation createMemberReservation(final ReservationDetail requestReservationDetail, final Member requestMember, final ReservationStatus status) {
        validateReservation(requestReservationDetail, requestMember, status);
        return new MemberReservation(requestReservationDetail, requestMember, status);
    }

    private void validateReservation(final ReservationDetail requestReservationDetail, final Member requestMember, final ReservationStatus status) {
        LocalDateTime now = LocalDateTime.now();
        long alreadyBookedReservationSize = memberReservationRepository.countByReservationDetail(requestReservationDetail);

        validateDateAndTime(requestReservationDetail, now);
        if (status.isReserved()) {
            validateAlreadyBookedReservationNotExist(requestReservationDetail, alreadyBookedReservationSize);
        } else if (status.isWaiting()) {
            validateAlreadyBookedReservationExist(alreadyBookedReservationSize, requestReservationDetail);
            validateMemberNotWaitingDuplicated(requestReservationDetail, requestMember);
        }
    }

    private void validateDateAndTime(final ReservationDetail reservationDetail, final LocalDateTime now) {
        if (reservationDetail.isPastThen(now)) {
            throw new ValidateException(ErrorType.RESERVATION_PERIOD_IN_PAST,
                    String.format("지난 날짜나 시간은 예약 또는 예약대기가 불가능합니다. [now: %s %s | request: %s %s]",
                            now.toLocalDate(), now.toLocalTime(), reservationDetail.getDate(), reservationDetail.getStartAt()));
        }
    }

    private void validateAlreadyBookedReservationExist(final long alreadyBookedReservationSize, final ReservationDetail reservationToReserve) {
        if (alreadyBookedReservationSize == 0) {
            throw new ValidateException(ErrorType.INVALID_REQUEST_DATA,
                    String.format("요청하신 날짜/시간/테마에 예약 정보가 존재하지 않아 예약 대기를 할 수 없습니다. '예약'으로 요청해주세요. [values: %s/%s/%s]",
                            reservationToReserve.getReservationTime(), reservationToReserve.getDate(), reservationToReserve.getTheme()));
        }
    }

    private void validateMemberNotWaitingDuplicated(final ReservationDetail requestReservation, final Member requestMember) {
        Optional<MemberReservation> memberReservation = memberReservationRepository.findByMemberAndReservationTimeAndDateAndTheme(
                requestMember, requestReservation.getReservationTime(), requestReservation.getDate(), requestReservation.getTheme());

        if (memberReservation.isPresent() && memberReservation.get().isWaitingStatus()) {
            throw new DataDuplicateException(ErrorType.RESERVATION_DUPLICATED,
                    String.format("이미 해당 날짜/시간/테마에 예약대기 중 입니다. [values: %s/%s/%s]",
                            requestReservation.getReservationTime(), requestReservation.getDate(), requestReservation.getTheme()));
        }
    }

//    public ReservationsResponse searchWith(
//            final Long themeId, final Long memberId, final LocalDate dateFrom, final LocalDate dateTo) {
//        Member member = memberRepository.getById(memberId);
//        Theme theme = themeRepository.getById(themeId);
//
//        List<ReservationResponse> response = reservationRepository.searchWith(theme, member, dateFrom, dateTo).stream()
//                .map(ReservationResponse::from)
//                .toList();
//        return new ReservationsResponse(response);
//    }

    public MemberReservationsResponse findUnexpiredReservationByMemberId(final Long memberId) {
        Member member = memberRepository.getById(memberId);
        List<MemberReservation> memberReservations = memberReservationRepository.findAfterAndEqualDateReservationByMemberOrderByIdAsc(member);

        List<MemberReservationResponse> responses = new ArrayList<>();
        for (int order = 0; order < memberReservations.size(); order++) {
            responses.add(MemberReservationResponse.fromEntity(memberReservations.get(order), order));
        }
        return new MemberReservationsResponse(responses);
    }

    //TODO: 지난 날짜/시간의 예약은 ReservationStatus.FINISH 상태로 변경하는 기능 추가 고려
}
