package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationSpecification;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.AuthInfo;
import roomescape.service.dto.request.ReservationCreateRequest;
import roomescape.service.dto.response.MyReservationResponse;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.ReservationWaitingResponse;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final RoomThemeRepository roomThemeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            RoomThemeRepository roomThemeRepository,
            MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.roomThemeRepository = roomThemeRepository;
        this.memberRepository = memberRepository;
    }

    public List<ReservationResponse> findAll() {
        return reservationRepository.findAllReservations()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<MyReservationResponse> findMyReservations(AuthInfo authInfo) {
        return reservationRepository.findMyReservationWithRank(authInfo.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    public List<ReservationWaitingResponse> findReservationsWaiting() {
        return reservationRepository.findAllWaitings()
                .stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    public List<ReservationResponse> findBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        validateDateCondition(dateFrom, dateTo);
        Specification<Reservation> specification = Specification.where(ReservationSpecification.hasThemeId(themeId))
                .and(ReservationSpecification.hasMemberId(memberId))
                .and(ReservationSpecification.fromDate(dateFrom))
                .and(ReservationSpecification.toDate(dateTo));

        return reservationRepository.findBy(specification).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse save(ReservationCreateRequest reservationCreateRequest) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationCreateRequest.timeId())
                .orElseThrow(() -> new NotFoundException("예약시간을 찾을 수 없습니다."));
        Member member = memberRepository.findById(reservationCreateRequest.memberId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        RoomTheme roomTheme = roomThemeRepository.findById(reservationCreateRequest.themeId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다."));
        Reservation reservation = reservationCreateRequest.toReservation(member, reservationTime, roomTheme);

        validateOutdatedDateTime(reservation.getDate(), reservationTime.getStartAt());
        validateDuplication(reservation);

        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    @Transactional
    public void deleteWaiting(Long id, AuthInfo authInfo) {
        Member member = memberRepository.findById(authInfo.id())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        Reservation reservation = reservationRepository.findWaitingReservationById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));

        reservation.validateAuthorization(member);

        reservation.delete();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findReservationById(id)
                .orElseThrow(() -> new NotFoundException("예약을 찾을 수 없습니다."));
        Optional<Reservation> reservationWaiting = reservationRepository.findLatestWaitingReservation();

        reservation.delete();
        reservationWaiting.ifPresent(Reservation::delete);
    }

    private void validateDateCondition(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BadRequestException("날짜를 잘못 입력하셨습니다.");
        }
    }

    private void validateOutdatedDateTime(LocalDate date, LocalTime time) {
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new BadRequestException("지나간 날짜와 시간에 대한 예약을 생성할 수 없습니다.");
        }
    }

    private void validateDuplication(Reservation reservation) {
        reservationRepository.findMemberReservation(
                        reservation.getDate(),
                        reservation.getTime(),
                        reservation.getTheme(),
                        reservation.getMember())
                .ifPresent(reservation::validateDuplication);
    }
}
