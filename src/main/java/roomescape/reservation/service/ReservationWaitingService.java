package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.service.MemberService;
import roomescape.reservation.controller.request.ReservationRequest;
import roomescape.reservation.controller.request.WaitingCreateRequest;
import roomescape.reservation.controller.response.MemberReservationResponse;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.controller.response.WaitingResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationDateTime;
import roomescape.reservation.domain.Waiting;
import roomescape.reservation.service.exception.MemberAlreadyHasThisReservationException;
import roomescape.reservation.service.exception.WaitingDuplicateException;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.ReservationTime;
import roomescape.time.service.ReservationTimeService;

@Service
public class ReservationWaitingService {
    private final ReservationRepository reservationRepository;
    private final WaitingRepository waitingRepository;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;
    private final MemberService memberService;

    public ReservationWaitingService(
            final ReservationRepository reservationRepository,
            final WaitingRepository waitingRepository,
            final ReservationTimeService reservationTimeService,
            final ThemeService themeService,
            final MemberService memberService
    ) {
        this.reservationRepository = reservationRepository;
        this.waitingRepository = waitingRepository;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
        this.memberService = memberService;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();
        return ReservationResponse.from(reservations);
    }

    @Transactional
    public ReservationResponse createReservation(Long memberId, ReservationRequest request) {
        Long timeId = request.timeId();
        ReservationDate reservationDate = new ReservationDate(request.date());
        if (reservationRepository.existsByReservationDateAndReservationTimeId(reservationDate, timeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 찼습니다.");
        }
        Member member = memberService.findById(memberId);
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        ReservationDateTime reservationDateTime = new ReservationDateTime(reservationDate, reservationTime);
        Theme theme = themeService.findById(request.themeId());
        Reservation created = reservationRepository.save(Reservation.create(reservationDateTime.getReservationDate()
                .getDate(), reservationTime, theme, member));
        return ReservationResponse.from(created);
    }

    @Transactional
    public ReservationResponse createReservationByName(String name, ReservationRequest request) {
        Long timeId = request.timeId();
        ReservationDate reservationDate = new ReservationDate(request.date());
        if (reservationRepository.existsByReservationDateAndReservationTimeId(reservationDate, timeId)) {
            throw new IllegalArgumentException("[ERROR] 이미 예약이 찼습니다.");
        }
        Member member = memberService.findByName(name);
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        ReservationDateTime reservationDateTime = new ReservationDateTime(reservationDate, reservationTime);
        Theme theme = themeService.findById(request.themeId());
        Reservation created = reservationRepository.save(Reservation.create(reservationDateTime.getReservationDate()
                .getDate(), reservationTime, theme, member));
        return ReservationResponse.from(created);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAllReservationsByFilter(Long memberId, Long themeId, LocalDate start,
                                                                 LocalDate end) {
        return ReservationResponse.from(
                reservationRepository.findByFilter(memberId, themeId, start, end)
        );
    }

    @Transactional(readOnly = true)
    public Reservation findReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("[ERROR] 예약을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findAllReservationsByMemberId(Long id) {
        return reservationRepository.findAllByMemberId(id).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    @Transactional
    public WaitingResponse createWaiting(MemberResponse memberResponse, WaitingCreateRequest request) {
        ReservationDate reservationDate = new ReservationDate(request.date());

        Optional<Reservation> reservationOptional = reservationRepository.findByThemeIdAndReservationTimeIdAndReservationDate_reservationDate(
                request.themeId(), request.timeId(), reservationDate.getDate());

        if (reservationOptional.isEmpty()) {
            throw new NoSuchElementException("[ERROR] 예약을 찾을 수 없습니다.");
        }

        Reservation reservation = reservationOptional.get();

        if (reservation.getMember().getId().equals(memberResponse.id())) {
            throw new MemberAlreadyHasThisReservationException("[ERROR] 이미 해당 예약이 등록되셨습니다.");
        }

        if (waitingRepository.existsBySameReservation(
                memberResponse.id(), reservation.getTheme().getId(),
                reservation.getReservationTime().getId(), reservation.getDate())) {
            throw new WaitingDuplicateException("[ERROR] 이미 대기열에 등록되어 있습니다.");
        }

        Member member = new Member(memberResponse.id(), new Name(memberResponse.name()),
                new Email(memberResponse.email()), memberResponse.role());
        Waiting waiting = Waiting.create(reservation, member);

        Waiting created = waitingRepository.save(waiting);
        return WaitingResponse.from(created);
    }

    @Transactional
    public void deleteWaitingById(Long id) {
        WaitingResponse waitingResponse = findWaitingById(id);
        waitingRepository.deleteById(waitingResponse.id());
    }

    @Transactional(readOnly = true)
    public WaitingResponse findWaitingById(Long id) {
        Optional<Waiting> waiting = waitingRepository.findById(id);
        if (waiting.isPresent()) {
            return WaitingResponse.from(waiting.get());
        }
        throw new NoSuchElementException("[ERROR] 예약 대기를 찾을 수 없습니다.");
    }

    @Transactional(readOnly = true)
    public List<MemberReservationResponse> findWaitingsWithRankByMemberId(Long memberId) {
        return waitingRepository.findWaitingsWithRankByMemberId(memberId).stream()
                .map(MemberReservationResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservationAndUpdateWaiting(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 예약을 찾을 수 없습니다."));

        Optional<Waiting> firstWaitingOptional = waitingRepository.findFirstOrderById(
                reservation.getTheme().getId(),
                reservation.getReservationTime().getId(),
                reservation.getDate()
        );
        if (firstWaitingOptional.isPresent()) {
            Waiting firstWaiting = firstWaitingOptional.get();
            reservation.updateMember(firstWaiting.getMember());
            waitingRepository.deleteById(firstWaiting.getId());
            reservationRepository.save(reservation);
            return;
        }
        reservationRepository.deleteById(reservationId);
    }
} 
