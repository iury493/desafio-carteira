package com.example.carteira.service;

import com.example.carteira.exceptions.DuplicateResourceException;
import com.example.carteira.exceptions.UserNotFoundException;
import com.example.carteira.model.User;
import com.example.carteira.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
         if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException(
                String.format("Email '%s' já está cadastrado", user.getEmail())
            );
        }
        
        if (userRepository.existsByDocumento(user.getDocumento())) {
            throw new DuplicateResourceException(
                String.format("Documento '%s' já está cadastrado", user.getDocumento())
            );
        }
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + id));
    }

}
