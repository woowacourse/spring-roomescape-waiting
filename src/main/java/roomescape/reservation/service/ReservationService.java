package roomescape.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.system.exception.error.ErrorType;
import roomescape.system.exception.model.DataDuplicateException;
import roomescape.system.exception.model.ForbiddenException;
import roomescape.system.exception.model.NotFoundException;
import roomescape.system.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationSpecification;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.FilteredReservationRequest;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.MemberReservationResponse;
import roomescape.reservation.dto.response.MemberReservationsResponse;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationTimeInfoResponse;
import roomescape.reservation.dto.response.ReservationTimeInfosResponse;
import roomescape.reservation.dto.response.ReservationsResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ReservationTimeService reservationTimeService;
    private final MemberService memberService;
    private final ThemeService themeService;

    public ReservationService(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ReservationTimeService reservationTimeService,
            final MemberService memberService,
            final ThemeService themeService
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationTimeService = reservationTimeService;
        this.memberService = memberService;
        this.themeService = themeService;
    }

    public ReservationsResponse findAllReservations() {
        final List<ReservationResponse> response = reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return new ReservationsResponse(response);
    }

    public ReservationTimeInfosResponse findReservationsByDateAndThemeId(final LocalDate date, final Long themeId) {
        final List<ReservationTime> allTimes = reservationTimeRepository.findAll();
        final Theme theme = themeService.findThemeById(themeId);
        final List<Reservation> reservations = reservationRepository.findByDateAndTheme(date, theme);

        final List<ReservationTimeInfoResponse> response = getReservationTimeInfoResponses(
                allTimes, reservations);

        return new ReservationTimeInfosResponse(response);
    }

    private List<ReservationTimeInfoResponse> getReservationTimeInfoResponses(
            final List<ReservationTime> allTimes,
            final List<Reservation> reservations
    ) {
        return allTimes.stream()
                .map(time -> new ReservationTimeInfoResponse(
                        time.getId(),
                        time.getStartAt(),
                        reservations.stream()
                                .anyMatch(reservation -> reservation.getReservationTime() == time))
                )
                .toList();
    }

    public Reservation findReservationById(final Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.RESERVATION_NOT_FOUND,
                        String.format("예약(Reservation) 정보가 존재하지 않습니다. [reservationId: %d]", id)));
    }

    public void removeReservationById(final Long targetReservationId, final Long myMemberId) {
        final Member requestMember = memberService.findMemberById(myMemberId);
        final Reservation requestReservation = findReservationById(targetReservationId);

        /*
        TODO: 주석 삭제
            1. 예약된 예약이 있으면 삭제한다.
                1.1 내가 삭제하려는 예약이 이미 예약된 경우
                    1.1.1 대기중인 예약이 있다면, 삭제 후 가장 처음 대기 예약의 상태를 예약됨으로 바꿔준다.
                    1.1.2 아니라면, 그냥 삭제한다.
                1.2 내가 삭제하려는 예약이 대기중인 예약인 경우
                    1.2.1 대기중인 예약이 있어도, 그냥 지워주기만 하면 된다.

         */

        if (requestMember.isAdmin() || requestReservation.getMemberId().equals(myMemberId)) {
            reservationRepository.delete(requestReservation);
            Optional<Reservation> waitingOptional = reservationRepository.findFirstByReservationTimeAndDateAndThemeAndReservationStatusOrderById(
                    requestReservation.getReservationTime(),
                    requestReservation.getDate(),
                    requestReservation.getTheme(),
                    ReservationStatus.WAITING
            );

            if (requestReservation.isReserved() && waitingOptional.isPresent()) {
                Reservation waitingReservation = waitingOptional.get();
                reservationRepository.delete(waitingReservation);
                reservationRepository.save(new Reservation(
                        waitingReservation.getDate(),
                        waitingReservation.getReservationTime(),
                        waitingReservation.getTheme(),
                        waitingReservation.getMember(),
                        ReservationStatus.RESERVED
                ));
            }
            return;
        }

        throw new ForbiddenException(
                ErrorType.PERMISSION_DOES_NOT_EXIST,
                "예약(Reservation) 정보에 대한 삭제 권한이 존재하지 않습니다."
        );
    }

    public ReservationResponse addReservation(final ReservationRequest request, final Long memberId) {
        final LocalDateTime now = LocalDateTime.now();
        final LocalDate requestDate = request.date();

        final ReservationTime requestTime = reservationTimeService.findTimeById(request.timeId());
        final Theme requestTheme = themeService.findThemeById(request.themeId());
        final Member member = memberService.findMemberById(memberId);

        validateDateAndTime(requestDate, requestTime, now);
//        validateReservationDuplicate(request, requestTheme);

        // TODO: 예약대기 추가 시, ReservationStatus 값 설정 로직 추가

        /*
            TODO: 주석 삭제
            1. 예약 추가
            2. reservationRepository에서 (X일,Y시간,Z테마)인 예약을 가져온다.
                2.1 예약이 있고 요청이 예약 대기라면 예약을 저장소에 추가한다.
                2.2 예약이 없고 요청이 예약이라면 상태를 예약됨으로 바꾸고 저장소에 추가한다.
                2.3 예약이 있고 요청이 예약이라면 예외가 발생한다. <- 불가능한 경우, API가 유효하지 않은 것으로 간주
                2.4 예약이 없고 요청이 예약 대기라면 예외가 발생한다. <- 불가능한 경우, API가 유효하지 않은 것으로 간주

           3. 요청에서 예약과 예약 대기를 구분하지 못하도록 했는데 위에는 무슨 경우니?
                3.1 예약은 항상 대기상태다.
                3.2 예약저장소에 예약이 있으면 예약을 그대로 집어넣는다.
                3.3 예약저장소에 예약이 없으면 예약을 예약됨으로 바꾸고 집어넣는다.
         */

        Optional<Reservation> optional = reservationRepository.findFirstByReservationTimeAndDateAndThemeAndReservationStatusOrderById(
                requestTime, requestDate, requestTheme, ReservationStatus.RESERVED
        );
        ReservationStatus state = optional.isEmpty() ? ReservationStatus.RESERVED : ReservationStatus.WAITING;
        Reservation saved = reservationRepository.save(
                new Reservation(requestDate, requestTime, requestTheme, member, state));
        return ReservationResponse.from(saved);
    }

    private void validateDateAndTime(
            final LocalDate requestDate,
            final ReservationTime requestReservationTime,
            final LocalDateTime now
    ) {
        if (isReservationInPast(requestDate, requestReservationTime, now)) {
            throw new ValidateException(
                    ErrorType.RESERVATION_PERIOD_IN_PAST,
                    String.format("지난 날짜나 시간은 예약이 불가능합니다. [now: %s %s | request: %s %s]",
                            now.toLocalDate(), now.toLocalTime(), requestDate, requestReservationTime.getStartAt())
            );
        }
    }

    private boolean isReservationInPast(
            final LocalDate requestDate,
            final ReservationTime requestReservationTime,
            final LocalDateTime now
    ) {
        final LocalDate today = now.toLocalDate();
        final LocalTime nowTime = now.toLocalTime();

        if (requestDate.isBefore(today)) {
            return true;
        }
        return requestDate.isEqual(today) && requestReservationTime.getStartAt().isBefore(nowTime);
    }

    private void validateReservationDuplicate(
            final ReservationRequest reservationRequest,
            final Theme theme
    ) {
        final ReservationTime time = reservationTimeService.findTimeById(reservationRequest.timeId());

        Optional<Reservation> optional = reservationRepository.findFirstByReservationTimeAndDateAndThemeAndReservationStatusOrderById(
                time, reservationRequest.date(), theme, ReservationStatus.RESERVED);

        if (optional.isPresent()) {
            throw new DataDuplicateException(ErrorType.RESERVATION_DUPLICATED,
                    String.format("이미 해당 날짜/시간/테마에 예약이 존재합니다. [values: %s]", reservationRequest));
        }
    }

    public ReservationsResponse findFilteredReservations(final FilteredReservationRequest request) {
        final Specification<Reservation> specification = getReservationSpecification(request);

        final List<ReservationResponse> response = reservationRepository.findAll(specification)
                .stream()
                .map(ReservationResponse::from)
                .toList();

        return new ReservationsResponse(response);
    }

    private Specification<Reservation> getReservationSpecification(
            final FilteredReservationRequest request
    ) {
        Specification<Reservation> specification = (root, query, criteriaBuilder) -> null;
        if (request.themeId() != null) {
            specification = specification.and(
                    ReservationSpecification.withTheme(themeService.findThemeById(request.themeId()))
            );
        }
        if (request.memberId() != null) {
            specification = specification.and(
                    ReservationSpecification.withMember(memberService.findMemberById(request.memberId())));
        }
        if (request.dateFrom() != null) {
            specification = specification.and(ReservationSpecification.withDateFrom(request.dateFrom()));
        }
        if (request.dateTo() != null) {
            specification = specification.and(ReservationSpecification.withDateTo(request.dateTo()));
        }
        return specification;
    }

    public MemberReservationsResponse findReservationByMemberId(final Long memberId) {
        final Member member = memberService.findMemberById(memberId);
        final List<Reservation> reservations = reservationRepository.findByMember(member);
        final List<MemberReservationResponse> responses = reservations.stream()
                .map(MemberReservationResponse::from)
                .toList();
        return new MemberReservationsResponse(responses);
    }
}
