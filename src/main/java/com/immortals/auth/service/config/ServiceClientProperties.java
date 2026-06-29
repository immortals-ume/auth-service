package com.immortals.auth.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures trusted service-to-service clients that authenticate via HTTP Basic.
 *
 * Example YAML:
 * <pre>
 * auth:
 *   service-clients:
 *     clients:
 *       - client-id: catalog-service
 *         client-secret: "{bcrypt}$2a$10$..."
 *         roles:
 *           - ROLE_SERVICE
 *       - client-id: order-service
 *         client-secret: "{bcrypt}$2a$10$..."
 *         roles:
 *           - ROLE_SERVICE
 * </pre>
 *
 * Secrets must be encoded with PasswordEncoderFactories.createDelegatingPasswordEncoder()
 * (prefix {bcrypt}, {noop} for tests, etc.).
 */
@ConfigurationProperties(prefix = "auth.service-clients")
public class ServiceClientProperties {

    private List<ClientEntry> clients = new ArrayList<>();

    public List<ClientEntry> getClients() { return clients; }
    public void setClients(List<ClientEntry> clients) { this.clients = clients; }

    public static class ClientEntry {
        private String clientId;
        private String clientSecret;
        private List<String> roles = List.of("ROLE_SERVICE");

        public String getClientId()            { return clientId; }
        public void setClientId(String v)      { this.clientId = v; }
        public String getClientSecret()        { return clientSecret; }
        public void setClientSecret(String v)  { this.clientSecret = v; }
        public List<String> getRoles()         { return roles; }
        public void setRoles(List<String> v)   { this.roles = v; }
    }
}

