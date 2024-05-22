package roomescape.service;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.domain.repository.MemberRepository;
import roomescape.domain.repository.ReservationRepository;
import roomescape.domain.repository.ReservationTimeRepository;
import roomescape.domain.repository.ThemeRepository;
import roomescape.exception.customexception.RoomEscapeBusinessException;
import roomescape.service.dto.request.ReservationConditionRequest;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.ReservationResponses;
import roomescape.service.dto.response.UserReservationResponse;
import roomescape.service.dto.response.UserReservationResponses;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {
    private static final int RESERVED_RANK = 0;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse saveReservation(ReservationSaveRequest reservationSaveRequest) {
        Reservation reservation = createReservation(reservationSaveRequest);

        validateUnique(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    private Reservation createReservation(ReservationSaveRequest reservationSaveRequest) {
        return new Reservation(
                findMemberById(reservationSaveRequest.memberId()),
                reservationSaveRequest.date(),
                findTimeById(reservationSaveRequest.timeId()),
                findThemeById(reservationSaveRequest.themeId())
        );
    }

    private void validateUnique(Reservation reservation) {
        boolean isReservationExist = reservationRepository.existsByDateAndTimeAndTheme(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );

        if (isReservationExist) {
            throw new RoomEscapeBusinessException("이미 존재하는 예약입니다.");
        }
    }

    public void deleteReservation(Long id) {
        Reservation foundReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약입니다."));

        reservationRepository.delete(foundReservation);
    }

    public ReservationResponses findReservationsByCondition(ReservationConditionRequest reservationConditionRequest) {
        List<Reservation> reservations = reservationRepository.findByConditions(
                Optional.ofNullable(reservationConditionRequest.dateFrom()),
                Optional.ofNullable(reservationConditionRequest.dateTo()),
                reservationConditionRequest.themeId(),
                reservationConditionRequest.memberId()
        );

        return toReservationResponses(reservations);
    }

    private ReservationResponses toReservationResponses(List<Reservation> reservations) {
        List<ReservationResponse> reservationResponses = reservations.stream()
                .map(ReservationResponse::new)
                .toList();
        return new ReservationResponses(reservationResponses);
    }

    public UserReservationResponses findAllUserReservation(Long memberId) {
        Member user = findMemberById(memberId);
        List<UserReservationResponse> waitingReservations = makeWaitingReservations(user);
        List<UserReservationResponse> userReservationResponses = findUserReservations(findUserReservations(user));
        return getAllUserReservations(userReservationResponses, waitingReservations);
    }

    private UserReservationResponses getAllUserReservations(
            List<UserReservationResponse> reservations,
            List<UserReservationResponse> waitingReservations
    ) {
        List<UserReservationResponse> allReservations = new ArrayList<>();
        allReservations.addAll(reservations);
        allReservations.addAll(waitingReservations);
        return new UserReservationResponses(allReservations);
    }

    private List<UserReservationResponse> findUserReservations(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> UserReservationResponse.of(reservation, ReservationStatus.RESERVED, RESERVED_RANK))
                .toList();
    }

    private List<UserReservationResponse> makeWaitingReservations(Member user) {
        return user.getWaitings().stream()
                .map(waiting -> new UserReservationResponse(waiting.getId(), waiting.getReservation(), ReservationStatus.WAITING, waiting.getRank() + 1))
                .toList();
    }

    private List<Reservation> findUserReservations(Member user) {
        return reservationRepository.findByMemberAndDateGreaterThanEqual(
                user,
                LocalDate.now(),
                dateAscAndTimeAsc()
        );
    }

    private Sort dateAscAndTimeAsc() {
        Sort.TypedSort<Reservation> sort = Sort.sort(Reservation.class);
        Sort dateSort = sort.by(Reservation::getDate).ascending();
        Sort timeSort = sort.by(Reservation::getTime).ascending();
        return dateSort.and(timeSort);
    }

    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new RoomEscapeBusinessException("회원이 존재하지 않습니다."));
    }

    private Theme findThemeById(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 테마입니다."));
    }

    private ReservationTime findTimeById(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약 시간입니다."));
    }
}

