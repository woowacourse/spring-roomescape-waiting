package roomescape.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.time.ReservationTime;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.reservation.ReservationFilter;
import roomescape.dto.reservation.ReservationRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.dto.reservation.UserReservationResponse;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.util.DateUtil;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository timeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository,
            WaitingRepository waitingRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public Long addReservation(ReservationRequest request) {
        Reservation reservation = convertReservation(request, Status.RESERVED);
        validateAddable(reservation);
        return reservationRepository.save(reservation).getId();
    }

    public Long addReservationWaiting(ReservationRequest request) {
        Reservation reservation = convertReservation(request, Status.WAITING);
        validateAddableWaiting(reservation);
        Reservation savedReservation = reservationRepository.save(reservation);
        addWaiting(savedReservation);
        return savedReservation.getId();
    }

    public List<ReservationResponse> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public ReservationResponse getReservation(Long id) {
        Reservation reservation = findReservationById(id);
        return ReservationResponse.from(reservation);
    }

    public List<UserReservationResponse> getReservationByMemberId(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);
        return convertUserReservationResponses(reservations);
    }

    public List<ReservationResponse> getReservationsByFilter(ReservationFilter filter) {
        List<Reservation> reservations = reservationRepository.findByMemberOrThemeOrDateRange(
                filter.getMemberId(),
                filter.getThemeId(),
                filter.getStartDate(),
                filter.getEndDate()
        );
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public void deleteReservation(Long id) {
        Reservation reservation = findReservationById(id);
        reservationRepository.deleteById(reservation.getId());
    }

    private Reservation convertReservation(ReservationRequest request, Status status) {
        ReservationTime reservationTime = findReservationTime(request.timeId());
        Theme theme = findTheme(request.themeId());
        Member member = findMember(request.memberId());
        return request.toEntityByReserved(reservationTime, theme, member, status);
    }

    private List<UserReservationResponse> convertUserReservationResponses(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::createUserReservationResponse)
                .toList();
    }

    private UserReservationResponse createUserReservationResponse(Reservation reservation) {
        if (reservation.getStatus() == Status.RESERVED) {
            return UserReservationResponse.creat(reservation);
        }

        Waiting waiting = findWaitingByReservationId(reservation.getId());
        return UserReservationResponse.createByWaiting(waiting);
    }

    private void addWaiting(Reservation savedReservation) {
        int waitingOrder = reservationRepository.countByDateAndTimeAndThemeAndStatus(
                savedReservation.getDate(),
                savedReservation.getTime(),
                savedReservation.getTheme(),
                savedReservation.getStatus()
        );
        waitingRepository.save(new Waiting(savedReservation, waitingOrder));
    }

    private Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약 정보 입니다.",
                        new Throwable("reservation_id : " + id)
                ));
    }

    private ReservationTime findReservationTime(Long timeId) {
        return timeRepository.findById(timeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 예약시간 정보 입니다.",
                        new Throwable("time_id : " + timeId)
                ));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 테마 정보 입니다.",
                        new Throwable("theme_id : " + themeId)
                ));
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 잘못된 사용자 정보 입니다.",
                        new Throwable("member_id : " + memberId)
                ));
    }

    private Waiting findWaitingByReservationId(Long reservationId) {
        return waitingRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "[ERROR] 예약 정보와 일치하는 대기 정보가 존재하지 않습니다.",
                        new Throwable("reservation_id : " + reservationId)
                ));
    }

    private void validateAddable(Reservation reservation) {
        validateUnPassedDate(reservation.getDate(), reservation.getTime().getStartAt());
        validateReservationNotDuplicate(reservation);
    }

    private void validateAddableWaiting(Reservation reservation) {
        validateUnPassedDate(reservation.getDate(), reservation.getTime().getStartAt());
        validateWaitingDuplicate(reservation);
    }

    private void validateUnPassedDate(LocalDate date, LocalTime time) {
        if (DateUtil.isPastDateTime(date, time)) {
            throw new IllegalArgumentException(
                    "[ERROR] 지나간 날짜와 시간은 예약이 불가능합니다.",
                    new Throwable("생성 예약 시간 : " + date + " " + time)
            );
        }
    }

    private void validateReservationNotDuplicate(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId())
        ) {
            throw new IllegalArgumentException(
                    "[ERROR] 해당 시간에 동일한 테마가 예약되어있어 예약이 불가능합니다.",
                    new Throwable("생성 예약 정보 : " + reservation)
            );
        }
    }

    private void validateWaitingDuplicate(Reservation reservation) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(
                reservation.getDate(),
                reservation.getTime().getId(),
                reservation.getTheme().getId(),
                reservation.getMember().getId())
        ) {
            throw new IllegalArgumentException(
                    "[ERROR] 이미 사용자에게 등록되거나 대기중인 예약이 있습니다..",
                    new Throwable("생성 예약 대기 정보 : " + reservation)
            );
        }
    }
}
