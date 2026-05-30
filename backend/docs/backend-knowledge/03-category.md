# Entidad: Category

## Ubicación
- Paquete: `com.smartspend.category`
- Clase entidad: `Category`
- Repositorio: `CategoryRepository`
- Servicio: `CategoryService`
- Controller: `CategoryController`

## Propósito funcional
Clasifica transacciones de ingreso/gasto para filtros, visualización y analítica.

## Estructura de la entidad `Category`
Tabla: `categories`

Campos principales:
- `id` (`Long`): PK autogenerada.
- `name` (`String`, max 20): nombre de categoría.
- `color` (`String`): color para UI (default `#6c757d`).
- `icon` (`String`, max 10): identificador de icono.
- `type` (`TransactionType`): `INCOME` o `EXPENSE`.
- `isDefault` (`Boolean`): indica si es categoría del sistema.
- `user` (`User`): null en categorías globales/default, con valor en categorías personalizadas.

Relaciones:
- `@ManyToOne` con `User`.
- `@OneToMany(mappedBy = "category")` con `Transaction`.

## Repositorio: `CategoryRepository`
Interfaz: extiende `JpaRepository<Category, Long>`.

Métodos relevantes:
- `List<Category> findByIsDefaultTrueAndType(TransactionType type)`
  - Categorías por defecto del sistema por tipo.

- `List<Category> findByUserUserIdAndType(Long userId, TransactionType type)`
  - Categorías personalizadas por usuario y tipo.

- `List<Category> findByUserUserId(Long userId)`
  - Todas las categorías custom del usuario.

- `Category findByName(String name)`
  - Búsqueda por nombre.

- `long countByIsDefaultTrue()`
  - Conteo de categorías del sistema.

## Servicio: `CategoryService`
Método principal:
- `getCategoriesForDropdown(String userEmail, TransactionType type)`
  - Obtiene usuario por email.
  - Trae categorías default + custom.
  - Fusiona ambas listas para uso en selector del frontend.

## Controller: `CategoryController`
Base path: `/api/v1/categories`

Endpoint principal:
- `GET /api/v1/categories?type=INCOME|EXPENSE`
  - Requiere usuario autenticado.
  - Responde lista de categorías válidas para el tipo solicitado.

## Reglas de negocio relevantes
- Convivencia de dos tipos de categorías:
  - Sistema (`isDefault = true`, `user = null`).
  - Usuario (`isDefault = false`, `user != null`).
- La operación principal actual es de lectura para selección en formularios de transacción.
