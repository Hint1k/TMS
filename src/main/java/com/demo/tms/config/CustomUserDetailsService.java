package com.demo.tms.config;

import com.demo.tms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@code CustomUserDetailsService} implements the {@link UserDetailsService} interface
 * to load user details from the database based on the user's email.
 * It retrieves the user's information and grants appropriate authorities for authentication and authorization.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Constructs a new {@code CustomUserDetailsService} with the provided {@link UserRepository}.
     *
     * @param userRepository The {@link UserRepository} used to fetch user data from the database.
     */
    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads the user details by email for authentication.
     * <p>
     * This method queries the {@link UserRepository} to retrieve the user by their email. If the user is found,
     * it returns a {@link org.springframework.security.core.userdetails.User} object with the user's email, password,
     * enabled status, and granted authorities. If the user is not found, it throws a {@link UsernameNotFoundException}.
     * </p>
     *
     * @param email The email of the user to be authenticated.
     * @return A {@link UserDetails} object containing the user's information.
     * @throws UsernameNotFoundException If no user with the specified email is found in the repository.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(user -> {
                    GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getAuthority());
                    return new org.springframework.security.core.userdetails.User(
                            user.getEmail(),
                            user.getPassword(),
                            user.isEnabled(),
                            true, true, true,
                            List.of(authority)
                    );
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}