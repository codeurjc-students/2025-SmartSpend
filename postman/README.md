# SmartSpend Postman Setup

Esta carpeta contiene los archivos de configuración para Postman que te permitirán probar la API de SmartSpend.

## Archivos incluidos

1. **SmartSpend-Environment.postman_environment.json**: Environment con las variables necesarias
2. **SmartSpend-Collection.postman_collection.json**: Collection básica con requests esenciales

## Cómo importar en Postman

### 1. Importar Environment
1. Abre Postman
2. Clic en "Import" (arriba izquierda)
3. Selecciona el archivo `SmartSpend-Environment.postman_environment.json`
4. El environment "SmartSpend Environment" aparecerá en tu lista

### 2. Importar Collection
1. En Postman, clic en "Import"
2. Selecciona el archivo `SmartSpend-Collection.postman_collection.json`
3. La collection "SmartSpend API Collection" aparecerá en tu workspace

### 3. Configurar Environment
1. En la esquina superior derecha, selecciona "SmartSpend Environment"
2. Las variables ya están configuradas para desarrollo local con HTTPS (localhost:8443)
3. **Importante**: Para certificados autofirmados, desactiva SSL verification en Postman:
   - Ve a Settings (⚙️) → General → SSL certificate verification → OFF

## Variables del Environment

- **baseUrl**: `https://localhost:8443` - URL base del backend (HTTPS)
- **apiVersion**: `v1` - Versión de la API
- **token**: `` - Token JWT (se llena automáticamente al hacer login)
- **userId**: `` - ID del usuario autenticado
- **contentType**: `application/json` - Tipo de contenido
- **sslVerify**: `false` - Verificación SSL (false para certificados autofirmados)

## Requests incluidos

### Health Check
- **GET** `/actuator/health` - Verifica que el servidor esté funcionando

### Authentication
- **POST** `/api/auth/login` - Login (guarda automáticamente el token)
- **POST** `/api/auth/register` - Registro de usuario

## Funcionalidades automáticas

- **Auto-token**: El request de login guarda automáticamente el token JWT en el environment
- **Bearer Auth**: La collection está configurada para usar el token automáticamente
- **Scripts**: Logging automático de requests y responses

## Uso recomendado

1. **Configurar SSL en Postman**: Desactiva SSL verification (Settings → General → SSL certificate verification → OFF)
2. Asegúrate de que el backend esté corriendo con SSL (`mvn spring-boot:run -Dspring.profiles.active=ssl`)
3. Ejecuta primero "Health Check" para verificar conectividad HTTPS
4. Usa "Register" para crear un usuario o "Login" para autenticarte
5. El token se guardará automáticamente para requests posteriores

## Próximos pasos

Puedes agregar más requests a la collection según vayas desarrollando nuevos endpoints:
- Transacciones CRUD
- Cuentas bancarias
- Categorías
- Reportes
- etc.