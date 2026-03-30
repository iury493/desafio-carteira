
import com.example.carteira.exceptions.InsufficientBalanceException;
import com.example.carteira.exceptions.UnauthorizedUserException;
import com.example.carteira.exceptions.UserNotFoundException;
import com.example.carteira.model.User;
import com.example.carteira.repository.UserRepository;
import com.example.carteira.service.TransferService;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransferService transferService;

    private User remetente;
    private User destinatario;

    @BeforeEach
    void setup() {
        remetente = criarUsuario(1L, "12345678901", new BigDecimal("100"), false);
        destinatario = criarUsuario(2L, "98765432100", new BigDecimal("50"), false);
    }

    @Test
    @DisplayName("Deve transferir com sucesso quando todos os dados são válidos")
    void deveTransferirComSucesso() {
        BigDecimal valorTransferencia = new BigDecimal("30");
        BigDecimal saldoEsperadoRemetente = new BigDecimal("70");
        BigDecimal saldoEsperadoDestinatario = new BigDecimal("80");

        when(userRepository.findById(1L)).thenReturn(Optional.of(remetente));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinatario));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.transfer(1L, 2L, valorTransferencia);

        assertEquals(saldoEsperadoRemetente, remetente.getSaldo());
        assertEquals(saldoEsperadoDestinatario, destinatario.getSaldo());
        
        verify(userRepository, times(1)).save(remetente);
        verify(userRepository, times(1)).save(destinatario);
        verify(userRepository, times(2)).findById(any(Long.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando remetente e destinatário são o mesmo usuário")
    void naoDevePermitirTransferenciaParaSiMesmo() {
        BigDecimal valorTransferencia = new BigDecimal("30");

        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                transferService.transfer(1L, 1L, valorTransferencia)
        );

        assertEquals("Operação inválida. Verifique as informações.", ex.getMessage());
        
        // Verificar que o repository nunca foi chamado
        verify(userRepository, never()).findById(any(Long.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o remetente é lojista")
    void naoDevePermitirTransferenciaDeLojista() {
        User lojista = criarUsuario(1L, "12345678000199", new BigDecimal("100"), true);
        BigDecimal valorTransferencia = new BigDecimal("10");

        when(userRepository.findById(1L)).thenReturn(Optional.of(lojista));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinatario));

        UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
                transferService.transfer(1L, 2L, valorTransferencia)
        );

        assertEquals("Ação não permitida para lojistas", ex.getMessage());
        
        // Verificar que o save nunca foi chamado
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o saldo do remetente é insuficiente")
    void naoDevePermitirSaldoInsuficiente() {
        User remetenteComSaldoBaixo = criarUsuario(1L, "12345678901", new BigDecimal("20"), false);
        BigDecimal valorTransferencia = new BigDecimal("30");

        when(userRepository.findById(1L)).thenReturn(Optional.of(remetenteComSaldoBaixo));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinatario));

        InsufficientBalanceException ex = assertThrows(InsufficientBalanceException.class, () ->
                transferService.transfer(1L, 2L, valorTransferencia)
        );

        assertEquals("Saldo insuficiente", ex.getMessage());
        
        // Verificar que o saldo não foi alterado
        assertEquals(new BigDecimal("20"), remetenteComSaldoBaixo.getSaldo());
        assertEquals(new BigDecimal("50"), destinatario.getSaldo());
        
        // Verificar que o save nunca foi chamado
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o remetente não existe")
    void deveLancarErroSeRemetenteNaoExistir() {
        BigDecimal valorTransferencia = new BigDecimal("10");
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () ->
                transferService.transfer(1L, 2L, valorTransferencia)
        );

        assertEquals("Usuário não encontrado com ID: 1", ex.getMessage());
        
        // Verificar que o save nunca foi chamado
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando o destinatário não existe")
    void deveLancarErroSeDestinatarioNaoExistir() {
        BigDecimal valorTransferencia = new BigDecimal("10");
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(remetente));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () ->
                transferService.transfer(1L, 2L, valorTransferencia)
        );

        assertEquals("Usuário não encontrado com ID: 1", ex.getMessage());
        
        // Verificar que o save nunca foi chamado
        verify(userRepository, never()).save(any(User.class));
    }

  @Test
@DisplayName("Deve lançar exceção quando o valor da transferência é zero")
void naoDevePermitirTransferenciaComValorZero() {
    BigDecimal valorTransferencia = BigDecimal.ZERO;

    UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
            transferService.transfer(1L, 2L, valorTransferencia)
    );

    assertEquals("Operação inválida. Verifique as informações.", ex.getMessage());
    
    verify(userRepository, never()).findById(anyLong());
    verify(userRepository, never()).save(any(User.class));
}

    @Test
    @DisplayName("Deve lançar exceção quando o valor da transferência é negativo")
    void naoDevePermitirTransferenciaComValorNegativo() {
    BigDecimal valorTransferencia = new BigDecimal("-10");

    UnauthorizedUserException ex = assertThrows(UnauthorizedUserException.class, () ->
            transferService.transfer(1L, 2L, valorTransferencia)
    );

    assertEquals("Operação inválida. Verifique as informações.", ex.getMessage());
    
    verify(userRepository, never()).findById(anyLong());
    verify(userRepository, never()).save(any(User.class));
}
    @Test
    @DisplayName("Deve transferir valor exato do saldo do remetente")
    void devePermitirTransferenciaComValorExatoDoSaldo() {
        BigDecimal valorTransferencia = new BigDecimal("100");
        BigDecimal saldoEsperadoRemetente = BigDecimal.ZERO;
        BigDecimal saldoEsperadoDestinatario = new BigDecimal("150");

        when(userRepository.findById(1L)).thenReturn(Optional.of(remetente));
        when(userRepository.findById(2L)).thenReturn(Optional.of(destinatario));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        transferService.transfer(1L, 2L, valorTransferencia);

        assertEquals(saldoEsperadoRemetente, remetente.getSaldo());
        assertEquals(saldoEsperadoDestinatario, destinatario.getSaldo());
        
        verify(userRepository, times(1)).save(remetente);
        verify(userRepository, times(1)).save(destinatario);
    }

    private User criarUsuario(Long id, String documento, BigDecimal saldo, boolean isLojista) {
        User user = new User();
        user.setId(id);
        user.setDocumento(documento);
        user.setSaldo(saldo);
        // Se for lojista, o documento deve ter mais de 11 caracteres
        if (isLojista && documento.length() <= 11) {
            user.setDocumento(documento + "0001");
        }
        return user;
    }
}