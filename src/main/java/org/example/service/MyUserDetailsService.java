package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.model.User;
import org.example.repo.UserRepo;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepo userRepository;

    @Override
    public UserDetails loadUserByUsername(@NonNull String Username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(Username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + Username));

        return user;
    }
}
