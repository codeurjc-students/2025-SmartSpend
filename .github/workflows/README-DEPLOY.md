# Deployment CI/CD - SmartSpend

## Descripcion

Esta guia documenta el despliegue de SmartSpend en **Render**, con base de datos **Aiven** desacoplada.

## Flujo general

1. Se publica una nueva version del proyecto.
2. CI valida build y pruebas.
3. Se publica la imagen Docker correspondiente.
4. Render despliega la nueva version del servicio.
5. Se valida salud de la aplicacion y conectividad con Aiven.

## Secrets recomendados en GitHub

Configura los secretos del repositorio desde la configuracion de GitHub Actions.

### Docker Hub

| Secret | Descripcion |
|--------|-------------|
| `DOCKER_HUB_USERNAME` | Usuario de Docker Hub |
| `DOCKER_HUB_TOKEN` | Token de acceso de Docker Hub |

### Render (si se usa despliegue por API/hook)

| Secret | Descripcion |
|--------|-------------|
| `RENDER_API_KEY` | API key de Render |
| `RENDER_SERVICE_ID` | Identificador del servicio en Render |

### Base de datos (Aiven)

| Secret | Descripcion |
|--------|-------------|
| `AIVEN_DB_HOST` | Host de la base de datos |
| `AIVEN_DB_PORT` | Puerto de conexion |
| `AIVEN_DB_NAME` | Nombre de la base de datos |
| `AIVEN_DB_USERNAME` | Usuario de base de datos |
| `AIVEN_DB_PASSWORD` | Contrasena de base de datos |
| `AIVEN_DB_SSL_MODE` | Modo SSL para MySQL |

## URL de produccion

- https://smartspend-dev.onrender.com

## Checklist operativo

- Verificar que CI finaliza en verde.
- Verificar que el servicio en Render queda en estado saludable.
- Verificar conexion a Aiven tras despliegue.
- Validar endpoints criticos y login.

## Troubleshooting rapido

- Si falla el arranque: revisar logs del servicio en Render.
- Si falla la BD: revisar variables `AIVEN_DB_*` y reglas de red en Aiven.
- Si no se actualiza version: revisar que la imagen/tag publicada coincide con la configurada en Render.
