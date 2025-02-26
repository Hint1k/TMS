package com.demo.tms.config;

import com.demo.tms.jwt.JwtService;
import com.demo.tms.repository.UserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private SecurityConfig securityConfig;

//    @BeforeEach
//    void setUp() {
//        securityConfig = new SecurityConfig(userRepository, jwtService);
//    }

    @Test
    void testSecurityFilterChainBean() throws Exception {
        // Mocking HttpSecurity with deep stubbing to simulate method chaining
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        // Creating an empty filter list and adding a custom OncePerRequestFilter
        List<Filter> filters = new ArrayList<>();
        filters.add(new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                filterChain.doFilter(request, response); // Continue filter chain
            }
        });

        // Constructing DefaultSecurityFilterChain with a path matcher and filters
        DefaultSecurityFilterChain securityFilterChain = new DefaultSecurityFilterChain(
                new AntPathRequestMatcher("/**"), filters);

        // Stubbing HttpSecurity methods for the test scenario
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
        when(httpSecurity.build()).thenReturn(securityFilterChain);

        // Invoking the method to test
        SecurityFilterChain result = securityConfig.securityFilterChain(httpSecurity);

        // Verifying that the build method was called once and asserting the result is not null
        assertNotNull(result);
        verify(httpSecurity, times(1)).build();
    }

    @Test
    void testUserDetailsServiceBean() {
        UserDetailsService userDetailsService = securityConfig.userDetailsService();
        assertNotNull(userDetailsService);
        assertInstanceOf(CustomUserDetailsService.class, userDetailsService);
    }

    @Test
    void testDaoAuthenticationProviderBean() throws Exception {
        // Initialize the UserDetailsService and PasswordEncoder
        UserDetailsService userDetailsService = new CustomUserDetailsService(userRepository);
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Create the DaoAuthenticationProvider using the config method
        DaoAuthenticationProvider provider = securityConfig.daoAuthenticationProvider(userDetailsService, encoder);

        // Assert that the provider is not null
        assertNotNull(provider);

        // Use reflection to access the protected getUserDetailsService method
        Method getUserDetailsServiceMethod =
                DaoAuthenticationProvider.class.getDeclaredMethod("getUserDetailsService");
        getUserDetailsServiceMethod.setAccessible(true);
        UserDetailsService actualUserDetailsService =
                (UserDetailsService) getUserDetailsServiceMethod.invoke(provider);
        // Verify that the expected UserDetailsService matches the one in the provider
        assertEquals(userDetailsService, actualUserDetailsService);

        // Use reflection to access the protected getPasswordEncoder method
        Method getPasswordEncoderMethod =
                DaoAuthenticationProvider.class.getDeclaredMethod("getPasswordEncoder");
        getPasswordEncoderMethod.setAccessible(true);
        PasswordEncoder actualEncoder = (PasswordEncoder) getPasswordEncoderMethod.invoke(provider);
        // Verify that the expected PasswordEncoder matches the one in the provider
        assertEquals(encoder, actualEncoder);
    }

    @Test
    void testAuthenticationManagerBean() throws Exception {
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        DaoAuthenticationProvider authProvider = mock(DaoAuthenticationProvider.class);

        AuthenticationManagerBuilder builder = mock(AuthenticationManagerBuilder.class);
        when(httpSecurity.getSharedObject(AuthenticationManagerBuilder.class)).thenReturn(builder);
        when(builder.build()).thenReturn(mock(AuthenticationManager.class));

        AuthenticationManager authenticationManager = securityConfig.authenticationManager(httpSecurity, authProvider);

        assertNotNull(authenticationManager);
        verify(builder, times(1)).authenticationProvider(authProvider);
    }

//    @Test
//    void testSecurityFilterChainBean() throws Exception {
//        HttpSecurity httpSecurity = mock(HttpSecurity.class);
//        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
//        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
//        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
//        when(httpSecurity.build()).thenReturn((DefaultSecurityFilterChain) mock(SecurityFilterChain.class));
//
//        SecurityFilterChain filterChain = securityConfig.securityFilterChain(httpSecurity);
//
//        assertNotNull(filterChain);
//        verify(httpSecurity, times(1)).build();
//    }

//@ExtendWith(MockitoExtension.class)
//class SecurityConfigTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private JwtService jwtService;
//
//    @Mock
//    private UserDetailsService userDetailsService;
//
//    @Mock
//    private DaoAuthenticationProvider authProvider;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private HttpSecurity httpSecurity;
//
//    @InjectMocks
//    private SecurityConfig securityConfig;
//
//    @BeforeEach
//    void setUp() {
//        securityConfig = new SecurityConfig(userRepository, jwtService);
//    }
//
//    @Test
//    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
//        assertInstanceOf(PasswordEncoder.class, securityConfig.passwordEncoder());
//    }
//
//    @Test
//    void userDetailsService_ShouldReturnCustomUserDetailsService() {
//        assertInstanceOf(UserDetailsService.class, securityConfig.userDetailsService());
//    }
//
//    @Test
//    void daoAuthenticationProvider_ShouldBeConfiguredCorrectly() {
//        // Spy on the actual provider to verify the setter method invocations.
//        DaoAuthenticationProvider provider = spy(new DaoAuthenticationProvider());
//
//        // Manually call the configuration method to simulate the behavior.
//        securityConfig.daoAuthenticationProvider(userDetailsService, passwordEncoder);
//
//        // Verify that the setter methods were called correctly.
//        verify(provider).setUserDetailsService(userDetailsService);
//        verify(provider).setPasswordEncoder(passwordEncoder);
//    }
//
//    @Test
//    void authenticationManager_ShouldBeCreatedWithoutErrors() {
//        assertThrows(RuntimeException.class, () -> securityConfig.authenticationManager(httpSecurity, authProvider));
//    }
//
//    @Test
//    void securityFilterChain_ShouldConfigureHttpSecurity() throws Exception {
//        HttpSecurity http = mock(HttpSecurity.class);
//        when(http.csrf(any())).thenReturn(http);
//        when(http.authorizeHttpRequests(any())).thenReturn(http);
//        when(http.addFilterBefore(any(JwtAuthenticationFilter.class), any())).thenReturn(http);
//
//        SecurityFilterChain filterChain = securityConfig.securityFilterChain(http);
//        assertNotNull(filterChain);
//        verify(http, times(1)).authorizeHttpRequests(any());
//        verify(http, times(1)).addFilterBefore(any(JwtAuthenticationFilter.class), any());
//    }
}