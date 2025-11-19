package dev.marko.EmailSender.security;

import dev.marko.EmailSender.auth.OAuth2LoginSuccessHandler;
import dev.marko.EmailSender.entities.Role;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;


@AllArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2LoginSuccessHandler successHandler;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationProvider authenticationProvider(){
        var provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .sessionManagement(c ->
                        c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(c -> c
                        .requestMatchers(HttpMethod.POST,"/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST,"/gmail-smtp/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/auth/confirm").permitAll()
                        .requestMatchers(HttpMethod.POST,"/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST,"/auth/refresh").permitAll()
//                        .requestMatchers(HttpMethod.GET,"/oauth-url").permitAll()
//                        .requestMatchers(HttpMethod.GET,"/callback").permitAll()
                        .requestMatchers(HttpMethod.POST,"/password/**").permitAll()
                        .requestMatchers("/api/pubsub/gmail").permitAll()
                        .requestMatchers("/", "/login**", "/oauth2/**").permitAll()
                        .anyRequest().authenticated()

                ) .oauth2Login(oauth -> oauth
                        .successHandler(successHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint(
                            new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                    c.accessDeniedHandler(
                            ((request,
                              response,
                              accessDeniedException) ->
                                    response.setStatus(HttpStatus.FORBIDDEN.value())
                            ))
                    ;
                })
                ;

        return http.build();
    }
}
