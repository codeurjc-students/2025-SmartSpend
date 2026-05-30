# Entidad: Debt

## Ubicación
- Paquete: `com.smartspend.transaction`
- Clase entidad: `Debt`
- Repositorio: `DebtRepository`
- Lógica de negocio: integrada en `TransactionService`

## Propósito funcional
Modela deudas compartidas asociadas a una transacción (por ejemplo, cuando un gasto se divide entre varias personas).

## Estructura de la entidad `Debt`
Tabla: `debts`

Campos principales:
- `id` (`Long`): PK autogenerada.
- `name` (`String`): nombre del deudor.
- `amount` (`BigDecimal`): monto adeudado.
- `isPaid` (`Boolean`, default `false`): estado de pago.

Relaciones:
- `@ManyToOne(fetch = LAZY)` con `Transaction` mediante `transaction_id`.

## Repositorio: `DebtRepository`
Interfaz: extiende `JpaRepository<Debt, Long>`.

Métodos relevantes:
- `Optional<Debt> findByTransaction_Account_IdAndNameAndAmount(Long accountId, String name, BigDecimal amount)`
  - Búsqueda de deuda concreta en una cuenta (usada al revertir ajustes).

- `List<Debt> findPendingDebtsByUserId(Long userId, Pageable pageable)`
  - Query JPQL para obtener deudas no pagadas de un usuario, ordenadas por fecha de transacción y ID.

## Lógica de negocio donde participa `Debt`
En `TransactionService`:
- `attachSharedDebts(...)`
  - Crea entidades `Debt` al guardar transacciones con deuda compartida.

- `markDebtAsPaid(transactionId, debtId, userEmail)`
  - Marca deuda como pagada.
  - Genera transacción de ajuste (`INCOME`, `excludeFromStats=true`) para reflejar cobro.

- `getPendingDebtsSummary(userEmail, limit)`
  - Devuelve resumen de deudas pendientes para UI.

- `deleteAdjustmentAndRestoreDebt(adjustmentId, email)`
  - Elimina ajuste financiero y revierte estado de deuda a no pagada.

- `deleteTransaction(...)`
  - Si se elimina una transacción con deudas pagadas, localiza y elimina ajustes vinculados.

## Integración API
No tiene controller dedicado.
Se expone indirectamente a través de endpoints de `TransactionController`, principalmente:
- `PATCH /api/v1/transactions/{transactionId}/debts/{debtId}/pay`
- `GET /api/v1/transactions/pending-summary`

## Reglas de negocio críticas
- `Debt` depende completamente del ciclo de vida de `Transaction` (orphan removal por lado de transacción).
- El estado `isPaid` tiene impacto directo en balance por creación/eliminación de ajustes.
- Debe preservarse consistencia entre:
  - estado de deuda,
  - transacción de ajuste,
  - balance de la cuenta.
