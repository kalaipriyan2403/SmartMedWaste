package com.smartmedwaste.backend.security;

import com.smartmedwaste.backend.entity.AppUser;
import com.smartmedwaste.backend.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        AppUser user = appUserRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with phone: " + phone));
        return new UserPrincipal(user);
    }
}