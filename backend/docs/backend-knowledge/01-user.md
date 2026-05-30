# Entidad: User

## Ubicación
- Paquete: `com.smartspend.user`
- Clase entidad: `User`
- Repositorio: `UserRepository`
- Servicio: `UserService` (placeholder)
- Controller: `UserController` (placeholder)

## Propósito funcional
Representa al usuario propietario de la información financiera (cuentas, categorías y acceso autenticado al sistema).

## Estructura de la entidad `User`
Tabla: `users`

Campos principales:
- `userId` (`Long`): PK autogenerada.
- `userName` (`String`): nombre visible del usuario.
- `userEmail` (`String`): email usado como identidad de login.
- `userHashedPassword` (`String`): contraseña cifrada para autenticación local.

Relaciones:
- `@OneToMany(mappedBy = "user")` con `BankAccount` (`userBankAccounts`).
- `@OneToMany(mappedBy = "user")` con `Category` (`userCategories`).

Notas:
- Usa Lombok (`@Getter`, `@Setter`, `@Builder`, `@AllArgsConstructor`).
- Tiene constructor vacío y constructor manual con nombre/email/password.

## Repositorio: `UserRepository`
Interfaz: extiende `JpaRepository<User, Long>`.

Métodos relevantes:
- `Optional<User> findByUserEmail(String email)`
  - Método clave para autenticación/autorización por email.
  - Usado transversalmente en múltiples servicios.

## Servicio: `UserService`
Estado actual:
- Clase creada pero sin métodos implementados.
- Actualmente la lógica de usuario reside en `AuthService` y servicios de dominio que resuelven usuario por email.

## Controller: `UserController`
Estado actual:
- Clase creada pero sin endpoints implementados.

## Integración con seguridad y auth
- `SmartSpendUserDetailsService` carga `User` por `userEmail` para Spring Security.
- `AuthService.register(...)` crea un `User` nuevo y hashea password con `PasswordEncoder`.
- `AuthService.login(...)` autentica por email/password y emite JWT.
- `AuthService.googleLogin(...)` crea o reutiliza `User` desde identidad Google.

## Riesgos y mejoras sugeridas
- Falta restricción explícita de unicidad para `userEmail` a nivel de DB (sería recomendable).
- Conviene mover validaciones de formato email/password a una capa DTO con Bean Validation.
- Si se habilitan endpoints en `UserController`, asegurar ownership y no exponer `userHashedPassword`.
