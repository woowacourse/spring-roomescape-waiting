package roomescape.auth.session;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class SessionRegistry implements HttpSessionListener, HttpSessionAttributeListener {
    private final ConcurrentHashMap<Long, HttpSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if (!"memberId".equals(event.getName())) {
            return;
        }
        Long memberId = (Long) event.getValue();
        HttpSession newSession = event.getSession();

        HttpSession existing = sessions.put(memberId, newSession);
        if (existing != null && !existing.getId().equals(newSession.getId())) {
            try {
                existing.invalidate();
            } catch (IllegalStateException ignored) {
            }
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        try {
            if (session.getAttribute("memberId") instanceof Long memberId) {
                sessions.remove(memberId, session);
            }
        } catch (IllegalStateException ignored) {
        }
    }
}
