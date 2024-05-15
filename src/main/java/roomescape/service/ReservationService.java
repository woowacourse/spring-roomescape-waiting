package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.MemberRepository;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationRepository;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.ReservationTimeRepository;
import roomescape.domain.Theme;
import roomescape.domain.ThemeRepository;
import roomescape.exception.RoomEscapeBusinessException;
import roomescape.service.dto.ReservationConditionRequest;
import roomescape.service.dto.ReservationResponse;
import roomescape.service.dto.ReservationSaveRequest;
import roomescape.service.dto.UserReservationResponse;

@Service
public class ReservationService {

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

    public List<ReservationResponse> findReservationsByCondition(ReservationConditionRequest reservationConditionRequest) {
        List<Reservation> reservations = reservationRepository.findByConditions(
                reservationConditionRequest.dateFrom(),
                reservationConditionRequest.dateTo(),
                themeRepository.findOneById(reservationConditionRequest.themeId()),
                memberRepository.findOneById(reservationConditionRequest.memberId())
        );

        return toReservationResponse(reservations);
    }

    private List<ReservationResponse> toReservationResponse(List<Reservation> reservations) {
        return reservations.stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<UserReservationResponse> findAllUserReservation(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberAndDateGreaterThanEqual(
                findMemberById(memberId),
                LocalDate.now(),
                Sort.by(Order.asc("date"), Order.asc("time.startAt"))
        );

        return reservations.stream()
                .map(reservation -> UserReservationResponse.of(reservation, ReservationStatus.RESERVED))
                .toList();
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

