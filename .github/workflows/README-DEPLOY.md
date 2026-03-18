# 🚀 GitHub Actions CI/CD - Deployment Pipeline

## 📋 Descripción

Este pipeline automatiza el despliegue de SmartSpend en producción cada vez que se publica una release en GitHub.

## 🔄 Flujo de Trabajo

1. **Trigger**: Se activa al publicar una release en GitHub
2. **Build**: Construye la imagen Docker (Frontend Angular + Backend Spring Boot)
3. **Push**: Sube la imagen a Docker Hub con el tag de la versión
4. **Deploy**: Se conecta por SSH a EC2 y actualiza la aplicación
5. **Verify**: Verifica que el contenedor esté corriendo correctamente

## 🔐 Secrets Requeridos

Configura los siguientes secrets en tu repositorio de GitHub:

**Ruta**: `Settings` → `Secrets and variables` → `Actions` → `New repository secret`

### Docker Hub

| Secret | Descripción | Ejemplo |
|--------|-------------|---------|
| `DOCKER_HUB_USERNAME` | Tu usuario de Docker Hub | `juanmabustos` |
| `DOCKER_HUB_TOKEN` | Access Token de Docker Hub | `dckr_pat_xxxxx...` |

**Cómo obtener el token de Docker Hub:**
1. Accede a [Docker Hub](https://hub.docker.com/)
2. Ve a `Account Settings` → `Security` → `New Access Token`
3. Dale un nombre (ej: "GitHub Actions")
4. Selecciona permisos: `Read, Write, Delete`
5. Copia el token generado

### AWS EC2

| Secret | Descripción | Ejemplo |
|--------|-------------|---------|
| `EC2_HOST` | IP o dominio de tu instancia EC2 | `ec2-X-X-X-X.compute.amazonaws.com` |
| `EC2_USERNAME` | Usuario SSH (normalmente `ubuntu`) | `ubuntu` |
| `EC2_SSH_KEY` | Clave privada SSH completa | `-----BEGIN RSA PRIVATE KEY-----...` |

**Cómo obtener la clave SSH:**
```bash
# Copia el contenido completo de tu archivo .pem
cat ~/path/to/your-ec2-key.pem
```

**Importante**: Copia TODO el contenido, incluyendo:
```
-----BEGIN RSA PRIVATE KEY-----
... (todo el contenido)
-----END RSA PRIVATE KEY-----
```

## 📦 Cómo Crear una Release

### Opción 1: Desde la interfaz de GitHub

1. Ve a tu repositorio en GitHub
2. Click en `Releases` (en la barra lateral derecha)
3. Click en `Create a new release`
4. En "Choose a tag", escribe la versión: `v0.1.0` (o la que corresponda)
5. Click en `Create new tag: v0.1.0 on publish`
6. Dale un título: "Release v0.1.0"
7. Describe los cambios en el release
8. Click en `Publish release`

### Opción 2: Desde la terminal

```bash
# Asegúrate de estar en la rama main y tener los últimos cambios
git checkout main
git pull

# Crea y sube el tag
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin v0.1.0

# Luego ve a GitHub y publica la release basada en ese tag
```

## 🎯 Formato de Versiones

El pipeline soporta ambos formatos:
- Con prefijo `v`: `v0.1.0`, `v1.0.0`, `v1.2.3`
- Sin prefijo: `0.1.0`, `1.0.0`, `1.2.3`

El workflow automáticamente eliminará el prefijo `v` si está presente.

## 🔍 Verificación del Deployment

Después del deployment, puedes verificar que todo funcione:

### 1. En GitHub Actions
- Ve a la pestaña `Actions` de tu repositorio
- Verás el workflow `Deploy to Production` en ejecución
- Revisa los logs de cada paso

### 2. En tu servidor EC2
```bash
# Conéctate por SSH
ssh -i your-key.pem ubuntu@your-ec2-host

# Ve al directorio de la aplicación
cd ~/smartspend

# Verifica que el contenedor esté corriendo
docker-compose ps

# Revisa los logs
docker-compose logs -f app

# Verifica la versión de la imagen
docker images | grep smartspend
```

### 3. En tu aplicación
- Accede a: `https://your-ec2-host`
- La aplicación debería estar corriendo con la nueva versión

## 📝 Estructura del Servidor EC2

El pipeline asume la siguiente estructura en tu servidor:

```
~/smartspend/
├── docker-compose.yml
├── .env
└── (otros archivos de configuración)
```

**Importante**: 
- El archivo `.env` debe existir con todas las variables de entorno necesarias
- El directorio `~/smartspend` debe existir antes del primer deployment
- Docker y Docker Compose deben estar instalados

## 🛠️ Mantenimiento

### Actualizar a una nueva versión

```bash
# 1. Sube tus cambios a main
git add .
git commit -m "feat: nueva funcionalidad"
git push origin main

# 2. Crea una nueva release
git tag -a v0.2.0 -m "Release version 0.2.0"
git push origin v0.2.0

# 3. Publica la release en GitHub
# El deployment se ejecutará automáticamente
```

### Rollback a una versión anterior

Si algo sale mal, puedes hacer rollback manualmente:

```bash
# Conéctate al servidor
ssh -i your-key.pem ubuntu@your-ec2-host

cd ~/smartspend

# Edita docker-compose.yml para usar la versión anterior
nano docker-compose.yml
# Cambia: image: username/smartspend:0.2.0
# Por: image: username/smartspend:0.1.0

# Reinicia con la versión anterior
docker-compose pull app
docker-compose up -d --no-deps app
```

## 🐛 Troubleshooting

### El build falla
- Verifica que el Dockerfile esté correcto
- Revisa los logs en GitHub Actions
- Asegúrate de que todos los archivos necesarios estén en el repositorio

### No puede conectar por SSH
- Verifica que `EC2_SSH_KEY` tenga el formato correcto (con headers BEGIN/END)
- Verifica que `EC2_HOST` sea accesible desde internet
- Verifica las reglas del Security Group en AWS (puerto 22 abierto para GitHub Actions)

### El contenedor no arranca
- Conéctate por SSH y revisa los logs: `docker-compose logs app`
- Verifica que el archivo `.env` tenga todas las variables necesarias
- Verifica que la base de datos esté corriendo: `docker-compose ps db`

### La imagen no se actualiza
- Verifica que el tag de la versión sea correcto en docker-compose.yml
- Ejecuta manualmente: `docker-compose pull app`
- Limpia la caché: `docker system prune -af`

## 📚 Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Docker Build & Push Action](https://github.com/docker/build-push-action)
- [SSH Action for GitHub Actions](https://github.com/appleboy/ssh-action)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

## ✅ Checklist de Configuración Inicial

- [ ] Crear cuenta en Docker Hub
- [ ] Crear Access Token en Docker Hub
- [ ] Configurar los 5 secrets en GitHub
- [ ] Crear directorio `~/smartspend` en EC2
- [ ] Subir `docker-compose.yml` y `.env` a EC2
- [ ] Verificar conectividad SSH desde tu máquina local
- [ ] Verificar que Docker esté instalado en EC2
- [ ] Crear primera release de prueba
- [ ] Verificar que el deployment funcione correctamente

---

**¿Necesitas ayuda?** Abre un issue en el repositorio o contacta al equipo de desarrollo.
