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
import roomescape.repository.ReservationExistSpecification;
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
        validateIsReservationExist(request);
        Reservation reservation = getReservationForSave(request, Status.CONFIRMED);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
    }

    public ReservationResponse wait(ReservationCreateRequest request) {
        validateDuplicate(request);
        Reservation reservation = getReservationForSave(request, Status.WAITING);
        Reservation savedReservation = reservationRepository.save(reservation);
        return ReservationResponse.from(savedReservation);
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

    private Reservation getReservationForSave(ReservationCreateRequest request, Status status) {
        ReservationTime reservationTime = getValidatedTime(request);
        Member member = getValidatedMember(request);
        RoomTheme roomTheme = getValidatedTheme(request);
        return new Reservation(member, request.date(), reservationTime, roomTheme, status);
    }

    private ReservationTime getValidatedTime(ReservationCreateRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new NotFoundException("예약시간을 찾을 수 없습니다. timeId = " + request.timeId()));

        validateOutdatedDateTime(request.date(), time.getStartAt());
        return time;
    }

    private Member getValidatedMember(ReservationCreateRequest request) {
        return memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다. memberId = " + request.memberId()));
    }

    private RoomTheme getValidatedTheme(ReservationCreateRequest request) {
        return roomThemeRepository.findById(request.themeId())
                .orElseThrow(() -> new NotFoundException("테마를 찾을 수 없습니다. themeId = " + request.themeId()));
    }

    private void validateIsReservationExist(ReservationCreateRequest request) {
        Specification<Reservation> spec = new ReservationExistSpecification()
                .themeId(request.themeId())
                .timeId(request.timeId())
                .date(request.date())
                .build();

        if (reservationRepository.exists(spec)) {
            throw new BadRequestException("이미 예약된 시간입니다.");
        }
    }

    private void validateDuplicate(ReservationCreateRequest request) {
        Specification<Reservation> specification = new ReservationExistSpecification()
                .memberId(request.memberId())
                .themeId(request.themeId())
                .timeId(request.timeId())
                .date(request.date())
                .build();

        if (reservationRepository.exists(specification)) {
            throw new BadRequestException("같은 시간,테마에 대한 예약(대기)은 한 번만 가능합니다.");
        }
    }

    private void validateOutdatedDateTime(LocalDate date, LocalTime time) {
        LocalDateTime now = LocalDateTime.now(Clock.systemDefaultZone());
        if (LocalDateTime.of(date, time).isBefore(now)) {
            throw new BadRequestException("지나간 날짜와 시간에 대한 예약을 생성할 수 없습니다.");
        }
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
