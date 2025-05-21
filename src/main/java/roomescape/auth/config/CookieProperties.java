package roomescape.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cookie")
public class CookieProperties {

    private String domain;
    private long maxAge;

    public String getDomain() {
        return domain;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public void setMaxAge(final long maxAge) {
        this.maxAge = maxAge;
    }
}
