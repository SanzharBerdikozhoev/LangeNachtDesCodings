package whz.pti.eva.auth;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

/**
 * Sicherheitskonfigurationsklasse definiert Zugriffsregeln Anmelde-/Abmeldekonfiguration,
 * Authentifizierungsanbieter und allgemeine Autorisierungslogik für die Anwendung.
 */
@Configuration
@EnableMethodSecurity
class SecurityConfig {

    private @Lazy UserDetailsService userDetailsService;

    private final LoginSuccessHandler loginSuccessHandler;

    SecurityConfig(LoginSuccessHandler loginSuccessHandler) {
        this.loginSuccessHandler = loginSuccessHandler;
    }

    /**
     * Legt fest, welche Pfade von Spring Security vollständig ignoriert werden sollen.
     * Prinzipiell wird dies für die statische Ressourcen verwendet.
     *
     * @return WebSecurityCustomizer
     */
    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) ->
                web.ignoring()
                        .requestMatchers("/h2-console/**")
                        .requestMatchers("/console/**")
                        .requestMatchers("/css/**", "/js/**", "/images/**");
    }

    /**
     * Definiert die Hauptfilterkette von Spring Security:
     * URL-Autorisierungsregeln, Konfigurationen, Remember-me-Unterstützung usw.
     *
     * @param http HttpSecurity builder
     * @return SecurityFilterChain
     * @throws Exception, wenn die Sicherheitskonfiguration fehlschlägt
     */
    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/", "/login", "/register", "/css/**").permitAll()
                                .requestMatchers("/cart/**").access(new WebExpressionAuthorizationManager("isAnonymous() or (hasAuthority('USER') and !hasAuthority('ADMIN'))"))
                                .requestMatchers("/ordered/**").hasAuthority("USER")
                                .requestMatchers("/pizzas/**", "/customers", "/customers/create", "/customers/*/block").hasAuthority("ADMIN")
                                .anyRequest()
                                .authenticated())
                .formLogin(formLogin ->
                        formLogin.loginPage("/login")
//                                .defaultSuccessUrl("/", false)
                                .successHandler(loginSuccessHandler)
                                .failureUrl("/login?error")
                                .usernameParameter("loginName").permitAll())
                .logout(logout ->
                        logout.logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID", "ANON_USER_ID"))
                .rememberMe(withDefaults());
        return http.build();
    }

    /**
     * Konfiguriert den AuthenticationManager mit DAO-basiertem Provider.
     * Dieser Provider lädt Benutzer aus der Datenbank und validiert Passwort-Hashes.
     *
     * @param userDetailsService Der benutzerdefinierte Benutzerdaten-Dienst
     * @param passwordEncoder    Der Passwort-Verschlüsseler
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(@Lazy UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    /**
     * Definiert den in der gesamten Anwendung verwendeten Passwort-Encoder.
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
