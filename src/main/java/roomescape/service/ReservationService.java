package roomescape.service;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.RoomTheme;
import roomescape.domain.Status;
import roomescape.exception.BadRequestException;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationSearchSpecification;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.RoomThemeRepository;
import roomescape.service.dto.AuthInfo;
import roomescape.service.dto.request.ReservationCreateRequest;
import roomescape.service.dto.response.ListResponse;
import roomescape.service.dto.response.MyReservationResponse;
import roomescape.service.dto.response.ReservationResponse;

@Service
@Transactional
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

    public ListResponse<ReservationResponse> findAll() {
        List<ReservationResponse> responses = reservationRepository.findAll()
                .stream()
                .filter(r -> r.getStatus() == Status.CONFIRMED)
                .map(ReservationResponse::from)
                .toList();

        return new ListResponse<>(responses);
    }

    public ListResponse<MyReservationResponse> findMyReservations(AuthInfo authInfo) {
        List<MyReservationResponse> responses = reservationRepository.findMyReservations(authInfo.id()).stream()
                .map(r -> MyReservationResponse.from(r.getReservation(), r.getWaitingOrder()))
                .toList();

        return new ListResponse<>(responses);
    }

    public ListResponse<ReservationResponse> findBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        validateDateCondition(dateFrom, dateTo);
        Specification<Reservation> spec = new ReservationSearchSpecification()
                .themeId(themeId)
                .memberId(memberId)
                .startFrom(dateFrom)
                .endAt(dateTo)
                .build();

        List<ReservationResponse> responses = reservationRepository.findAll(spec).stream()
                .filter(r -> r.getStatus() == Status.CONFIRMED)
                .map(ReservationResponse::from)
                .toList();

        return new ListResponse<>(responses);
    }

    public ReservationResponse save(ReservationCreateRequest reservationCreateRequest) {
        ReservationTime reservationTime = reservationTimeRepository.findById(reservationCreateRequest.timeId())
                .orElseThrow(
                        () -> new NotFoundException("예약시간을 찾을 수 없습니다. timeId = " + reservationCreateRequest.timeId()));
        validateOutdatedDateTime(reservationCreateRequest.date(), reservationTime.getStartAt());
        Member member = memberRepository.findById(reservationCreateRequest.memberId())
                .orElseThrow(() -> new NotFoundException(
                        "사용자를 찾을 수 없습니다. memberId = " + reservationCreateRequest.memberId()));
        RoomTheme roomTheme = roomThemeRepository.findById(reservationCreateRequest.themeId())
                .orElseThrow(
                        () -> new NotFoundException("테마를 찾을 수 없습니다. themeId = " + reservationCreateRequest.themeId()));
        Status status = getStatus(reservationCreateRequest);

        Reservation reservation = reservationCreateRequest.toReservation(member, reservationTime, roomTheme, status);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    private void validateDateCondition(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom.isAfter(dateTo)) {
            throw new BadRequestException("종료 날짜가 시작 날짜 이전일 수 없습니다.");
        }
    }

    private void validateOutdatedDateTime(LocalDate date, LocalTime time) {
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new BadRequestException("지나간 날짜와 시간에 대한 예약을 생성할 수 없습니다.");
        }
    }

    private Status getStatus(ReservationCreateRequest request) {
        boolean exists = reservationRepository.existsByDateAndTimeIdAndThemeId(request.date(), request.timeId(),
                request.themeId());
        if (exists) {
            return Status.WAITING;
        }
        return Status.CONFIRMED;
    }
}
