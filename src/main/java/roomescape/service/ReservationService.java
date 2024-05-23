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

    public ReservationResponse save(ReservationCreateRequest request) {
        Reservation reservation = getReservationForSave(request, Status.CONFIRMED);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse wait(ReservationCreateRequest request) {
        Reservation reservation = getReservationForSave(request, Status.WAITING);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    private Reservation getReservationForSave(ReservationCreateRequest request, Status status) {
        validateDuplicate(request);
        ReservationTime reservationTime = getValidatedTime(request);
        Member member = getValidatedMember(request);
        RoomTheme roomTheme = getValidatedTheme(request);
        return new Reservation(member, request.date(), reservationTime, roomTheme, status);
    }

    private void validateDuplicate(ReservationCreateRequest request) {
        if (reservationRepository.hasSameReservation(request.date(), request.timeId(), request.themeId(),
                request.memberId())) {
            throw new BadRequestException("중복된 예약(대기)를 생성할 수 없습니다.");
        }
    }

    private ReservationTime getValidatedTime(ReservationCreateRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("예약시간을 찾을 수 없습니다. timeId = " + request.timeId()));

        validateOutdatedDateTime(request.date(), time.getStartAt());
        return time;
    }

    private void validateOutdatedDateTime(LocalDate date, LocalTime time) {
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new BadRequestException("지나간 날짜와 시간에 대한 예약을 생성할 수 없습니다.");
        }
    }

    private Member getValidatedMember(ReservationCreateRequest request) {
        return memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. memberId = " + request.memberId()));
    }

    private RoomTheme getValidatedTheme(ReservationCreateRequest request) {
        return roomThemeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다. themeId = " + request.themeId()));
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

    public void deleteById(Long id) {
        reservationRepository.deleteById(id);
    }

    private void validateDateCondition(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom == null || dateTo == null) {
            return;
        }
        if (dateFrom.isAfter(dateTo)) {
            throw new BadRequestException("종료 날짜가 시작 날짜 이전일 수 없습니다.");
        }
    }
}
