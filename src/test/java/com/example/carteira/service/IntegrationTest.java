package com.example.carteira.service;

import com.example.carteira.CarteiraApplication;
import com.example.carteira.exceptions.DuplicateResourceException;
import com.example.carteira.exceptions.InsufficientBalanceException;
import com.example.carteira.exceptions.UnauthorizedUserException;
import com.example.carteira.exceptions.UserNotFoundException;
import com.example.carteira.model.User;
import com.example.carteira.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CarteiraApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Integração")
class IntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private UserRepository userRepository;

    private User remetente;
    private User destinatario;
    private User lojista;

    @BeforeEach
    void setUp() {
        // Limpar o banco antes de cada teste
        userRepository.deleteAll();

        remetente = new User();
        remetente.setNome("João Silva");
        remetente.setEmail("joao@email.com");
        remetente.setDocumento("12345678901");
        remetente.setSenha("123456");
        remetente.setSaldo(new BigDecimal("100.00"));
        remetente = userService.createUser(remetente);

        destinatario = new User();
        destinatario.setNome("Maria Souza");
        destinatario.setEmail("maria@email.com");
        destinatario.setDocumento("98765432100");
        destinatario.setSenha("123456");
        destinatario.setSaldo(new BigDecimal("50.00"));
        destinatario = userService.createUser(destinatario);

        lojista = new User();
        lojista.setNome("Loja do João");
        lojista.setEmail("loja@joao.com");
        lojista.setDocumento("12345678000199");
        lojista.setSenha("123456");
        lojista.setSaldo(new BigDecimal("1000.00"));
        lojista = userService.createUser(lojista);
    }

    @Nested
    @DisplayName("Testes de Criação de Usuário")
    class UserCreationTests {

        @Test
        @DisplayName("Deve criar usuário com sucesso")
        void deveCriarUsuarioComSucesso() {
            User novoUsuario = new User();
            novoUsuario.setNome("Carlos Lima");
            novoUsuario.setEmail("carlos@email.com");
            novoUsuario.setDocumento("11122233344");
            novoUsuario.setSenha("123456");
            novoUsuario.setSaldo(new BigDecimal("200.00"));

            User usuarioCriado = userService.createUser(novoUsuario);
            assertNotNull(usuarioCriado.getId());
            assertEquals("Carlos Lima", usuarioCriado.getNome());
            assertEquals("carlos@email.com", usuarioCriado.getEmail());
            assertEquals("11122233344", usuarioCriado.getDocumento());
            assertEquals(new BigDecimal("200.00"), usuarioCriado.getSaldo());
            
            User usuarioEncontrado = userService.findById(usuarioCriado.getId());
            assertNotNull(usuarioEncontrado);
        }

        @Test
        @DisplayName("Deve criar usuário com saldo inicial zero quando não informado")
        void deveCriarUsuarioComSaldoZero() {
            User novoUsuario = new User();
            novoUsuario.setNome("Ana Santos");
            novoUsuario.setEmail("ana@email.com");
            novoUsuario.setDocumento("55566677788");
            novoUsuario.setSenha("123456");

            User usuarioCriado = userService.createUser(novoUsuario);

            assertEquals(BigDecimal.ZERO, usuarioCriado.getSaldo());
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar usuário com email duplicado")
        void deveLancarExcecaoEmailDuplicado() {
            User usuarioDuplicado = new User();
            usuarioDuplicado.setNome("João Duplicado");
            usuarioDuplicado.setEmail("joao@email.com"); // Email repetido
            usuarioDuplicado.setDocumento("99988877766");
            usuarioDuplicado.setSenha("123456");
            usuarioDuplicado.setSaldo(new BigDecimal("100.00"));

            DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
                userService.createUser(usuarioDuplicado);
            });

            assertTrue(exception.getMessage().contains("Email 'joao@email.com' já está cadastrado"));
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar usuário com documento duplicado")
        void deveLancarExcecaoDocumentoDuplicado() {
            User usuarioDuplicado = new User();
            usuarioDuplicado.setNome("Maria Duplicada");
            usuarioDuplicado.setEmail("maria.duplicada@email.com");
            usuarioDuplicado.setDocumento("12345678901"); // Documento já existe
            usuarioDuplicado.setSenha("123456");
            usuarioDuplicado.setSaldo(new BigDecimal("100.00"));

            DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
                userService.createUser(usuarioDuplicado);
            });

            assertTrue(exception.getMessage().contains("Documento '12345678901' já está cadastrado"));
        }
    }

    @Nested
    @DisplayName("Testes de Busca de Usuário")
    class UserFindTests {

        @Test
        @DisplayName("Deve encontrar usuário por ID")
        void deveEncontrarUsuarioPorId() {
            User usuarioEncontrado = userService.findById(remetente.getId());

            assertNotNull(usuarioEncontrado);
            assertEquals(remetente.getId(), usuarioEncontrado.getId());
            assertEquals(remetente.getNome(), usuarioEncontrado.getNome());
            assertEquals(remetente.getEmail(), usuarioEncontrado.getEmail());
        }

        @Test
        @DisplayName("Deve lançar exceção ao buscar usuário inexistente")
        void deveLancarExcecaoUsuarioInexistente() {
            Long idInexistente = 999L;

            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                userService.findById(idInexistente);
            });

            assertTrue(exception.getMessage().contains("Usuário não encontrado com ID: " + idInexistente));
        }
    }

    @Nested
    @DisplayName("Testes de Transferência")
    class TransferTests {

        @Test
        @DisplayName("Deve transferir com sucesso entre usuários comuns")
        void deveTransferirComSucesso() {
            BigDecimal valorTransferencia = new BigDecimal("30.00");

            transferService.transfer(remetente.getId(), destinatario.getId(), valorTransferencia);

            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(new BigDecimal("70.00"), remetenteAtualizado.getSaldo());
            assertEquals(new BigDecimal("80.00"), destinatarioAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Deve transferir valor exato do saldo do remetente")
        void deveTransferirValorExatoDoSaldo() {
            BigDecimal valorTransferencia = new BigDecimal("100.00");

            transferService.transfer(remetente.getId(), destinatario.getId(), valorTransferencia);

            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(0.0, remetenteAtualizado.getSaldo().doubleValue());
            assertEquals(new BigDecimal("150.00"), destinatarioAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Não deve permitir transferência de lojista")
        void naoDevePermitirTransferenciaDeLojista() {
            BigDecimal valorTransferencia = new BigDecimal("50.00");

            UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () -> {
                transferService.transfer(lojista.getId(), destinatario.getId(), valorTransferencia);
            });

            assertEquals("Ação não permitida para lojistas", exception.getMessage());

            User lojistaAtualizado = userService.findById(lojista.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(new BigDecimal("1000.00"), lojistaAtualizado.getSaldo());
            assertEquals(new BigDecimal("50.00"), destinatarioAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Não deve permitir transferência com saldo insuficiente")
        void naoDevePermitirSaldoInsuficiente() {
            BigDecimal valorTransferencia = new BigDecimal("200.00");

            InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
                transferService.transfer(remetente.getId(), destinatario.getId(), valorTransferencia);
            });

            assertEquals("Saldo insuficiente", exception.getMessage());

            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(new BigDecimal("100.00"), remetenteAtualizado.getSaldo());
            assertEquals(new BigDecimal("50.00"), destinatarioAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Não deve permitir transferência para si mesmo")
        void naoDevePermitirTransferenciaParaSiMesmo() {
            BigDecimal valorTransferencia = new BigDecimal("30.00");

            UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () -> {
                transferService.transfer(remetente.getId(), remetente.getId(), valorTransferencia);
            });

            assertEquals("Operação inválida. Verifique as informações.", exception.getMessage());

            User remetenteAtualizado = userService.findById(remetente.getId());
            assertEquals(new BigDecimal("100.00"), remetenteAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Não deve permitir transferência com valor zero")
        void naoDevePermitirTransferenciaComValorZero() {
            BigDecimal valorTransferencia = BigDecimal.ZERO;

            UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () -> {
                transferService.transfer(remetente.getId(), destinatario.getId(), valorTransferencia);
            });

            assertEquals("Operação inválida. Verifique as informações.", exception.getMessage());

            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(new BigDecimal("100.00"), remetenteAtualizado.getSaldo());
            assertEquals(new BigDecimal("50.00"), destinatarioAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Não deve permitir transferência com valor negativo")
        void naoDevePermitirTransferenciaComValorNegativo() {
            BigDecimal valorTransferencia = new BigDecimal("-30.00");

            UnauthorizedUserException exception = assertThrows(UnauthorizedUserException.class, () -> {
                transferService.transfer(remetente.getId(), destinatario.getId(), valorTransferencia);
            });

            assertEquals("Operação inválida. Verifique as informações.", exception.getMessage());

            // Verificar que os saldos não foram alterados
            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(new BigDecimal("100.00"), remetenteAtualizado.getSaldo());
            assertEquals(new BigDecimal("50.00"), destinatarioAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Deve lançar exceção quando remetente não existe")
        void deveLancarErroQuandoRemetenteNaoExiste() {
            Long idInexistente = 999L;
            BigDecimal valorTransferencia = new BigDecimal("30.00");

            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                transferService.transfer(idInexistente, destinatario.getId(), valorTransferencia);
            });

            assertTrue(exception.getMessage().contains("Usuário não encontrado com ID: " + idInexistente));
        }

        @Test
        @DisplayName("Deve lançar exceção quando destinatário não existe")
        void deveLancarErroQuandoDestinatarioNaoExiste() {
            Long idInexistente = 999L;
            BigDecimal valorTransferencia = new BigDecimal("30.00");

            // Act & Assert
            UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
                transferService.transfer(remetente.getId(), idInexistente, valorTransferencia);
            });

            assertTrue(exception.getMessage().contains("Usuário não encontrado com ID: " + idInexistente));
        }

        @Test
        @DisplayName("Deve permitir transferência com valores decimais")
        void devePermitirTransferenciaComValoresDecimais() {
            BigDecimal valorTransferencia = new BigDecimal("33.33");

            transferService.transfer(remetente.getId(), destinatario.getId(), valorTransferencia);

            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(new BigDecimal("66.67"), remetenteAtualizado.getSaldo());
            assertEquals(new BigDecimal("83.33"), destinatarioAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Deve permitir múltiplas transferências sequenciais")
        void devePermitirMultiplasTransferencias() {
            BigDecimal primeiraTransferencia = new BigDecimal("30.00");
            BigDecimal segundaTransferencia = new BigDecimal("20.00");

            transferService.transfer(remetente.getId(), destinatario.getId(), primeiraTransferencia);
            transferService.transfer(remetente.getId(), destinatario.getId(), segundaTransferencia);

            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(new BigDecimal("50.00"), remetenteAtualizado.getSaldo()); // 100 - 30 - 20 = 50
            assertEquals(new BigDecimal("100.00"), destinatarioAtualizado.getSaldo()); // 50 + 30 + 20 = 100
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Lojista")
    class LojistaTests {

        @Test
        @DisplayName("Deve identificar corretamente usuário comum (CPF)")
        void deveIdentificarUsuarioComum() {
            User usuarioComum = new User();
            usuarioComum.setDocumento("12345678901");

            assertFalse(usuarioComum.isLojista());
        }

        @Test
        @DisplayName("Deve identificar corretamente lojista (CNPJ)")
        void deveIdentificarLojista() {
            User usuarioLojista = new User();
            usuarioLojista.setDocumento("12345678000199");

            assertTrue(usuarioLojista.isLojista());
        }
    }

    @Nested
    @DisplayName("Testes de Transações e Atomicidade")
    class TransactionTests {

        @Test
        @DisplayName("Deve garantir que ambos os saldos são atualizados corretamente")
        void deveAtualizarAmbosSaldos() {
            BigDecimal valorTransferencia = new BigDecimal("45.00");
            BigDecimal saldoEsperadoRemetente = new BigDecimal("55.00");
            BigDecimal saldoEsperadoDestinatario = new BigDecimal("95.00");

            transferService.transfer(remetente.getId(), destinatario.getId(), valorTransferencia);

            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(saldoEsperadoRemetente, remetenteAtualizado.getSaldo());
            assertEquals(saldoEsperadoDestinatario, destinatarioAtualizado.getSaldo());
        }

        @Test
        @DisplayName("Deve manter consistência dos dados após múltiplas operações")
        void deveManterConsistencia() {
            List<BigDecimal> transferencias = List.of(
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("15.00"),
                new BigDecimal("5.00")
            );
            
            BigDecimal totalTransferido = transferencias.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            for (BigDecimal valor : transferencias) {
                transferService.transfer(remetente.getId(), destinatario.getId(), valor);
            }

            User remetenteAtualizado = userService.findById(remetente.getId());
            User destinatarioAtualizado = userService.findById(destinatario.getId());

            assertEquals(new BigDecimal("100.00").subtract(totalTransferido), remetenteAtualizado.getSaldo());
            assertEquals(new BigDecimal("50.00").add(totalTransferido), destinatarioAtualizado.getSaldo());
        }
    }
}