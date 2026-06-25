package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.SessionRequest;
import roomescape.controller.dto.SessionResponse;
import roomescape.domain.Session;
import roomescape.service.SessionService;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> sessions() {
        return ResponseEntity.ok(convertToResponses(sessionService.allSessions()));
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(@RequestBody @Valid SessionRequest request) {
        Session session = sessionService.createSession(request.date(), request.timeId(), request.themeId());
        return ResponseEntity.created(URI.create("/sessions/" + session.getId()))
                .body(SessionResponse.from(session));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<SessionResponse>> createSessionsForDate(@RequestParam LocalDate date) {
        return ResponseEntity.ok(convertToResponses(sessionService.createSessionsForDate(date)));
    }

    private List<SessionResponse> convertToResponses(List<Session> sessions) {
        return sessions.stream().map(SessionResponse::from).toList();
    }
}
