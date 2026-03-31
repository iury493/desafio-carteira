package com.example.carteira.service;

import com.example.carteira.exceptions.InsufficientBalanceException;
import com.example.carteira.exceptions.UnauthorizedUserException;
import com.example.carteira.exceptions.UserNotFoundException;
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
        if(fromId == toId || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new UnauthorizedUserException("Operação inválida. Verifique as informações.");
        }
        
        User from = userRepository.findById(fromId)
            .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + fromId));
        User to = userRepository.findById(toId).orElseThrow(() -> new UserNotFoundException("Usuário não encontrado com ID: " + toId));

        if (from.isLojista()) {
            throw new UnauthorizedUserException("Ação não permitida para lojistas");
        }

        if (from.getSaldo().compareTo(valor) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente");
        }

        from.setSaldo(from.getSaldo().subtract(valor));
        to.setSaldo(to.getSaldo().add(valor));

        userRepository.save(from);
        userRepository.save(to);
    }
}
