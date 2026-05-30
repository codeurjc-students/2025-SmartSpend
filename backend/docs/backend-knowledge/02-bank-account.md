# Entidad: BankAccount

## Ubicación
- Paquete: `com.smartspend.bankAccount`
- Clase entidad: `BankAccount`
- Repositorio: `BankAccountRepository`
- Servicio: `BankAccountService`
- Controller: `BankAccountController`
- DTO principal: `CreateBankAccountDTO`

## Propósito funcional
Representa una cuenta financiera del usuario (por ejemplo, cuenta corriente, ahorro, etc.) y su balance actual.

## Estructura de la entidad `BankAccount`
Tabla: `bankAccounts`

Campos principales:
- `id` (`Long`): PK autogenerada.
- `user` (`User`): propietario de la cuenta (`@ManyToOne`, no nulo).
- `accountName` (`String`): nombre de la cuenta.
- `currentBalance` (`BigDecimal`): saldo actual.
- `createdAt` (`LocalDateTime`): fecha de creación (no actualizable).

Relaciones:
- `@ManyToOne` con `User` (ownership).
- `@OneToMany(mappedBy = "account", cascade = ALL, orphanRemoval = true)` con `Transaction`.

## Repositorio: `BankAccountRepository`
Interfaz: extiende `JpaRepository<BankAccount, Long>`.

Métodos relevantes:
- `List<BankAccount> findByUser_UserId(Long userId)`
  - Lista cuentas de un usuario.
- `Optional<BankAccount> findByIdAndUser_UserId(Long id, Long userId)`
  - Consulta segura por cuenta + propietario.
- `long countByUser(User user)`
  - Conteo de cuentas por usuario.

## Servicio: `BankAccountService`
Métodos principales:
- `createBankAccount(CreateBankAccountDTO dto, String email)`
  - Resuelve usuario por email.
  - Inicializa saldo en 0 si `initialBalance` es null.
  - Persiste cuenta nueva.

- `getUserBankAccountsByEmail(String email)`
  - Busca usuario por email.
  - Devuelve cuentas asociadas.

- `getBankAccountByIdAndEmail(Long accountId, String email)`
  - Busca cuenta garantizando ownership.

- `deleteBankAccount(BankAccount account)`
  - Elimina la cuenta.

## Controller: `BankAccountController`
Base path: `/api/v1/accounts`

Endpoints principales:
- `GET /api/v1/accounts`
  - Devuelve cuentas del usuario autenticado.

- `GET /api/v1/accounts/{accountId}`
  - Devuelve detalle de cuenta por ID si pertenece al usuario.

- `POST /api/v1/accounts`
  - Crea cuenta desde `CreateBankAccountDTO`.

- `DELETE /api/v1/accounts/{accountId}`
  - Elimina cuenta del usuario autenticado.

## DTO `CreateBankAccountDTO`
Campos:
- `accountName` (`String`)
- `initialBalance` (`BigDecimal`)

## Reglas de negocio relevantes
- Toda operación se vincula al usuario autenticado (email del principal).
- La cuenta es el ancla para casi toda transacción financiera.
- El balance se ajusta en otras capas (principalmente `TransactionService`), no en el controlador.
