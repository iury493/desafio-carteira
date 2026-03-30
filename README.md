Projeto desenvolvido como desafio técnico para vaga de Desenvolvedor Backend Java Jr.

---

## Tecnologias utilizadas

- Java 17
- Spring Boot
- Spring Data JPA
- H2 Database (em memória)
- Maven
- JUnit 5 + Mockito

---

## Funcionalidades

### Cadastro de usuários
- Nome completo
- Email (único)
- CPF/CNPJ (único)
- Senha
- Saldo inicial

---

### Transferência entre usuários

Regras implementadas:

- ✅ Transferência entre usuários
- ❌ Lojistas (CNPJ) não podem enviar
- ✅ Usuários comuns (CPF) podem enviar e receber
- ❌ Não permite saldo negativo
- ✅ Operação deve ser atômica

---

### Consulta de saldo

Retorna:
- Dados básicos do usuário
- Saldo atual

---

## Como rodar o projeto

```bash
# Clonar repositório
git clone https://github.com/seu-usuario/desafio-carteira.git

# Entrar na pasta
cd desafio-carteira

# Rodar aplicação
./mvnw spring-boot:run 
# ou
mvn spring-boot:run

#------------Comandos Docker------------#

# Build da imagem
docker build -t carteira-app .

# Rodar o container
docker run -p 8080:8080 carteira-app

# Rodar com docker-compose
docker-compose up -d

# Ver logs
docker logs -f carteira-app

# Parar o container
docker stop carteira-app

# Remover o container
docker rm carteira-app