package com.example.carteira.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.*;

@Entity
@Table(name = "users", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_email", columnNames = "email"),
           @UniqueConstraint(name = "uk_documento", columnNames = "documento")
       })
public class User {

    public interface PublicView {}
    public interface InternalView extends PublicView {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(PublicView.class)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    @JsonView(PublicView.class)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ser válido")
    @Column(nullable = false, unique = true, length = 100)
    @JsonView(PublicView.class)
    private String email;

    @NotBlank(message = "Documento é obrigatório")
    @Pattern(regexp = "\\d+", message = "Documento deve conter apenas números")
    @Size(min = 11, max = 14, message = "Documento deve ter entre 11 e 14 dígitos")
    @Column(nullable = false, unique = true, length = 14)
    @JsonView(PublicView.class)
    private String documento;

    @NotBlank(message = "Senha é obrigatória")
    @Column(nullable = false)
    @JsonView(InternalView.class)
    private String senha;

    @NotNull(message = "Saldo é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Saldo não pode ser negativo")
    @Column(nullable = false, precision = 19, scale = 2)
    @JsonView(PublicView.class)
    private BigDecimal saldo;

    public User() {
        this.saldo = BigDecimal.ZERO;
    }
    public User(String nome, String email, String documento, String senha, BigDecimal saldo) {
        this.nome = nome;
        this.email = email;
        this.documento = documento;
        this.senha = senha;
        this.saldo = saldo != null ? saldo : BigDecimal.ZERO;
    }

    public boolean isLojista() {
        return documento != null && documento.length() > 11;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
}