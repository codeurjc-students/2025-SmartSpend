# Entidad: Transaction

## Ubicación
- Paquete: `com.smartspend.transaction`
- Clase entidad: `Transaction`
- Repositorio: `TransactionRepository`
- Servicio principal: `TransactionService`
- Controller: `TransactionController`
- Clases de soporte: `TransactionSpecification`, `TransactionMapper`, `RecurringTransactionScheduler`

## Propósito funcional
Representa un movimiento financiero (ingreso/gasto), con soporte para recurrencia, imagen adjunta, deudas compartidas y trazabilidad de balance.

## Estructura de la entidad `Transaction`
Tabla: `transactions`

Campos principales:
- `id` (`Long`): PK autogenerada.
- `title` (`String`, max 30)
- `description` (`String`, max 100)
- `amount` (`BigDecimal`): monto total de la transacción.
- `date` (`LocalDate`)
- `type` (`TransactionType`): `INCOME` o `EXPENSE`.
- `recurrence` (`Recurrence`): `NONE`, `DAILY`, `WEEKLY`, `MONTHLY`, `YEARLY`.
- `isRecurringSeriesParent` (`Boolean`): marca transacción semilla de recurrencia.
- `nextRecurrenceDate` (`LocalDate`): próxima fecha de generación.
- `beforeBalance` (`BigDecimal`): saldo de cuenta previo al movimiento.
- `effectiveAmount` (`BigDecimal`): monto efectivo para estadísticas.
- `excludeFromStats` (`Boolean`): excluye de agregados analíticos.

Soporte de imagen:
- `imageData` (`byte[]`, `LONGBLOB`)
- `imageType` (`String`)
- `imageName` (`String`)

Relaciones:
- `@ManyToOne` con `BankAccount` (`account`).
- `@ManyToOne` con `Category` (`category`).
- `@OneToMany(mappedBy = "transaction", cascade = ALL, orphanRemoval = true)` con `Debt` (`sharedDebts`).

Métodos de entidad destacados:
- `hasImage()`: indica si hay imagen asociada.
- `getImageBase64()`: serializa binario de imagen para respuesta.

## Repositorio: `TransactionRepository`
Interfaz: `JpaRepository<Transaction, Long>` + `JpaSpecificationExecutor<Transaction>`.

Consultas clave:
- `findByAccount_User_UserIdOrderByDateDesc(...)`
- `findByAccountIdAndLimit(...)` (nativa, últimos N movimientos)
- `findByAccountIdOrderByDateDesc(...)` (paginado)
- `findBalanceUpToDate(...)` (saldo acumulado hasta fecha)
- `findByAccountAndDateRangeAndType(...)` (series temporales)
- `findCategoryTotalsByAccountAndDateRangeAndType(...)` (totales por categoría)
- `findTotalByAccountAndDateRangeAndType(...)` (total por tipo)
- `findPendingRecurringTransactions(...)` (scheduler)
- `findAdjustments(...)` (ajustes de deuda)

## Servicio: `TransactionService`

### Lectura y consulta
- `getTransactionById(Long transactionId)`
- `findAll(String email)`
- `getRecentTransactionsByAccount(Long accountId, int limit, String email)`
- `getTransactionsByAccount(...)`
  - Usa `TransactionSpecification.filterTransactions(...)` para filtros dinámicos (texto, tipo, fechas, montos, categoría, deudas pendientes).

### Escritura y actualización
- `saveTransaction(CreateTransactionDto dto, String userEmail)`
  - Valida ownership de cuenta.
  - Calcula recurrencia y `nextRecurrenceDate`.
  - Calcula `effectiveAmount` (compatible con deudas compartidas).
  - Actualiza balance de cuenta y persiste.

- `saveTransactionWithImage(CreateTransactionWithImageDto dto, String userEmail)`
  - Valida y procesa imagen (`ImageUtils`).
  - Persiste transacción con metadatos de imagen.

- `updateTransaction(Long transactionId, CreateTransactionDto dto, String userEmail)`
  - Actualiza campos base.
  - Recalcula impacto de balance restando valor anterior y aplicando nuevo.

### Deudas y ajustes
- `markDebtAsPaid(Long transactionId, Long debtId, String userEmail)`
  - Marca deuda como pagada.
  - Crea transacción de ajuste tipo `INCOME` con `excludeFromStats = true`.
  - Actualiza balance.

- `getPendingDebtsSummary(String userEmail, int limit)`
  - Resume deudas pendientes para widgets/listados.

- `deleteAdjustmentAndRestoreDebt(Long adjustmentId, String email)`
  - Revierte ajuste, desmarca deuda como no pagada y corrige balance.

### Borrado
- `deleteTransaction(Long transactionId, String email)`
  - Reversa impacto de balance según tipo.
  - Si hay deudas pagadas asociadas, elimina también ajustes relacionados y corrige saldo.

### Helpers de negocio críticos
- `resolveEffectiveAmount(...)`
- `attachSharedDebts(...)`
- `upadateAccountBalance(...)` (nombre actual con typo en código)
- `calculateNextRecurrenceDate(...)`

## Controller: `TransactionController`
Base path: `/api/v1/transactions`

Endpoints clave:
- `GET /api/v1/transactions`
- `GET /api/v1/transactions/{transactionId}`
- `GET /api/v1/transactions/account/{accountId}?limit=...`
- `GET /api/v1/transactions/account/{accountId}/paginated` (filtros + pageable)
- `GET /api/v1/transactions/pending-summary`
- `POST /api/v1/transactions`
- `POST /api/v1/transactions/with-image`
- `PUT /api/v1/transactions/{transactionId}`
- `PATCH /api/v1/transactions/{transactionId}/debts/{debtId}/pay`
- `DELETE /api/v1/transactions/{transactionId}`

## Soporte de recurrencia automática
`RecurringTransactionScheduler`:
- Cron diario: `0 0 0 * * ?`.
- Busca transacciones recurrentes vencidas.
- Genera transacción hija no recurrente.
- Avanza `nextRecurrenceDate` del padre.
- Ajusta balance de la cuenta.

## DTOs principales relacionados
- `CreateTransactionDto`
- `CreateTransactionWithImageDto`
- `TransactionResponseDto`
- `PendingDebtSummaryDto`
- `DebtDto`
- `DebtResponseDto`

## Reglas de negocio críticas
- Todas las operaciones están sujetas a ownership de cuenta/usuario.
- Balance se mantiene consistente aplicando/revirtiendo impactos en cada alta/edición/baja.
- Transacciones de ajuste por deuda son ingresos técnicos, no estadísticas de negocio.
