package roomescape.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;
import roomescape.domain.Status;
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

@Service
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

    @Transactional
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public List<MyReservationResponse> findMyReservations(AuthInfo authInfo) {
        return reservationRepository.findMyReservations(authInfo.id())
                .stream()
                .map(MyReservationResponse::from)
                .toList();
    }

    @Transactional
    public List<ReservationResponse> findBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        validateDateCondition(dateFrom, dateTo);
        Specification<Reservation> spec = Specification.where(ReservationSpecification.hasThemeId(themeId))
                .and(ReservationSpecification.hasMemberId(memberId))
                .and(ReservationSpecification.fromDate(dateFrom))
                .and(ReservationSpecification.toDate(dateTo));

        return reservationRepository.findAll(spec).stream()
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
    public void cancelWaiting(Long id, AuthInfo authInfo) {
        Member member = memberRepository.findById(authInfo.id())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        Reservation reservation = reservationRepository.findByIdAndStatus(id, Status.WAITING)
                .orElseThrow(() -> new BadRequestException("예약을 찾을 수 없습니다."));

        reservation.validateAuthorization(member);

        reservation.updateStatus(Status.WAITING_CANCEL);
        reservationRepository.save(reservation);
    }

    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
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
        reservationRepository.findByDateAndTimeAndThemeAndMember(
                        reservation.getDate(),
                        reservation.getTime(),
                        reservation.getTheme(),
                        reservation.getMember())
                .ifPresent(reservation::validateDuplication);
    }
}
