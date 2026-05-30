# SmartSpend Backend Knowledge Base

## Objetivo de esta carpeta
Esta carpeta centraliza el conocimiento funcional y técnico del backend de SmartSpend para facilitar consulta, onboarding y carga en NotebookLM.

## Stack y arquitectura
- Framework principal: Spring Boot.
- Persistencia: Spring Data JPA (repositorios por entidad).
- Seguridad: Spring Security + JWT (`JwtAuthenticationFilter`, `SmartSpendSecConfig`, `SmartSpendUserDetailsService`).
- API: REST controllers bajo prefijo `/api/v1/...`.
- Dominio principal: cuentas bancarias, categorías, transacciones y deudas.

## Entidades de dominio documentadas
1. `01-user.md`
2. `02-bank-account.md`
3. `03-category.md`
4. `04-transaction.md`
5. `05-debt.md`

## Flujo funcional de alto nivel
1. El usuario se registra o autentica (`/api/v1/auth/...`) y obtiene JWT.
2. Crea y consulta sus cuentas bancarias (`/api/v1/accounts`).
3. Registra ingresos/gastos en una cuenta (`/api/v1/transactions`).
4. Clasifica por categorías (`/api/v1/categories`).
5. Consulta analítica y reportes (`/api/v1/charts`, `/api/v1/report`).

## Módulos backend no ligados a una sola entidad

### Auth (`com.smartspend.auth`)
- `AuthController`: endpoints de login, register y Google login.
- `AuthService`: registro, autenticación y federación Google.
- `JwtService`: generación y parseo del token JWT.

### Security (`com.smartspend.security`)
- `SmartSpendSecConfig`: configuración de CORS, rutas públicas, stateless session y filtros.
- `JwtAuthenticationFilter`: extrae JWT del header y coloca el principal (email) en el contexto de seguridad.
- `SmartSpendUserDetailsService`: integra usuario de BD con `UserDetailsService`.

### Charts y análisis (`com.smartspend.charts`)
- `ChartsController` y `ChartsService`: pie/bar/timeline mensual y anual.
- `AnalysisService`: forecast, tendencia por categoría y gastos fijos.

### Reportes (`com.smartspend.report`)
- `ReportController` y `ReportService`: agregación de datos de transacciones y charts para respuesta de reporte consolidada.

## Observaciones técnicas relevantes
- `UserService` y `UserController` actualmente son clases vacías (placeholders).
- El núcleo de negocio está principalmente en `TransactionService` y `BankAccountService`.
- Hay soporte de transacciones recurrentes mediante scheduler diario (`RecurringTransactionScheduler`).
- Existe soporte de imagen en transacciones (`/with-image`) y deudas compartidas por transacción.
