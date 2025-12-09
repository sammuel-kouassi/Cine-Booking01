package com.example.cine_booking.service;

import com.example.cine_booking.exception.BusinessException;
import com.example.cine_booking.model.User;
import com.example.cine_booking.model.enums.UserRole;
import com.example.cine_booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        // On utilise un Set mutable pour ajouter le rôle
        // Note: Si 'roles' est immuable, il faut recréer le Set.
        Set<UserRole> newRoles = new HashSet<>(user.getRoles());
        newRoles.add(UserRole.ADMIN);

        user.setRoles(newRoles);
        userRepository.save(user);
    }
}
