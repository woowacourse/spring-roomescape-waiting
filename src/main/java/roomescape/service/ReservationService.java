package roomescape.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationReadOnly;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.ReservationTimeRepository;
import roomescape.domain.reservation.Role;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.ThemeRepository;
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

    @Transactional
    public ReservationResponse saveReservation(ReservationSaveRequest reservationSaveRequest) {
        Reservation reservation = createReservation(reservationSaveRequest);

        validateDuplicateReservation(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return new ReservationResponse(savedReservation);
    }

    private Reservation createReservation(ReservationSaveRequest reservationSaveRequest) {
        Member member = findMemberById(reservationSaveRequest.memberId());
        LocalDate date = reservationSaveRequest.date();
        ReservationTime time = findTimeById(reservationSaveRequest.timeId());
        Theme theme = findThemeById(reservationSaveRequest.themeId());

        boolean isAlreadyBooked = reservationRepository.existsByDateAndTimeAndTheme(date, time, theme);

        return Reservation.create(member, date, time, theme, isAlreadyBooked);
    }

    private void validateDuplicateReservation(Reservation reservation) {
        boolean isReservationExist = reservationRepository.existsByDateAndTimeAndThemeAndMember(
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                reservation.getMember()
        );

        if (isReservationExist) {
            throw new RoomEscapeBusinessException("이미 존재하는 예약입니다.");
        }
    }

    @Transactional
    public void deleteReservation(Long id, Role role) {
        Reservation foundReservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomEscapeBusinessException("존재하지 않는 예약입니다."));

        if (role == Role.USER && foundReservation.isBooked()) {
            throw new RoomEscapeBusinessException("이미 예약되어 삭제할 수 없습니다.");
        }

        reservationRepository.delete(foundReservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findReservationsByCondition(
            ReservationConditionRequest reservationConditionRequest) {
        List<ReservationReadOnly> reservations = reservationRepository.findByConditions(
                reservationConditionRequest.dateFrom(),
                reservationConditionRequest.dateTo(),
                reservationConditionRequest.themeId(),
                reservationConditionRequest.memberId()
        );

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserReservationResponse> findAllUserReservation(Long memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberAndDateGreaterThanEqual(
                findMemberById(memberId),
                LocalDate.now(),
                Sort.by(Order.asc("date"), Order.asc("time.startAt"))
        );

        return reservations.stream()
                .map(UserReservationResponse::from)
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

    public Long findMemberIdById(Long id) {
        return reservationRepository.findMemberIdById(id);
    }
}

