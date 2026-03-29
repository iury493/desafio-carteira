
package com.example.carteira.service;

import com.example.carteira.model.User;
import com.example.carteira.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final UserRepository userRepository;

    public TransferService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void transfer(Long fromId, Long toId, BigDecimal valor) {
        User from = userRepository.findById(fromId).orElseThrow();
        User to = userRepository.findById(toId).orElseThrow();

        if (from.isLojista()) {
            throw new RuntimeException("Ação não permitida para lojistas");
        }

        if (from.getSaldo().compareTo(valor) < 0) {
            throw new RuntimeException("Saldo insuficiente");
        }

        from.setSaldo(from.getSaldo().subtract(valor));
        to.setSaldo(to.getSaldo().add(valor));

        userRepository.save(from);
        userRepository.save(to);
    }
}
