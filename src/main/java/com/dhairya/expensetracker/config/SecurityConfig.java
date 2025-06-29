package com.dhairya.expensetracker.config;

import com.dhairya.expensetracker.eventProducer.UserInfoProducer;
import com.dhairya.expensetracker.filters.JwtAuthFilter;
import com.dhairya.expensetracker.repository.UserRepository;
import com.dhairya.expensetracker.service.UserDetailsImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
@Configuration
@EnableMethodSecurity
@Data
public class SecurityConfig {

    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final UserDetailsImpl userDetailsServiceImpl;
    @Autowired
    private final JwtAuthFilter jwtAuthFilter;
    @Autowired
    private CustomAuthenticationEntryPoint customAuthEntryPoint;
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository,PasswordEncoder passwordEncoder,UserInfoProducer userInfoProducer) {
        return new UserDetailsImpl(userRepository,passwordEncoder,userInfoProducer);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,JwtAuthFilter jwtAuthFilter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable).cors(CorsConfigurer::disable);

        http.authorizeHttpRequests((auth)->auth.requestMatchers("/auth/v1/login","/auth/v1/refreshToken","/auth/v1/signup","/auth/v1/forgotPassword","/auth/v1/reset/password/**","/auth/v1/oauth/callback").permitAll().anyRequest().authenticated());

        http.sessionManagement(sess->sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsServiceImpl);
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager(); // retrieves the default authentication manager in spring boot.
    }

    


}
