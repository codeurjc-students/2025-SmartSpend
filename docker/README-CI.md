# SmartSpend CI Environment

Este docker-compose-ci.yml está configurado para ejecutar pruebas de integración con Newman en un entorno de CI.

## Servicios incluidos:

1. **init-volume**: Inicializa volúmenes para los reportes
2. **app-ci**: Aplicación SmartSpend (Backend + Frontend)
3. **db-ci**: Base de datos MySQL para CI
4. **newman**: Ejecutor de tests de Postman

## Uso:

### Ejecutar todas las pruebas de CI:
```bash
docker-compose -f docker-compose-ci.yml up --build --abort-on-container-exit
```

### Ejecutar solo hasta que la aplicación esté lista (sin Newman):
```bash
docker-compose -f docker-compose-ci.yml up --build app-ci
```

### Ver solo los logs de Newman:
```bash
docker-compose -f docker-compose-ci.yml logs newman
```

### Limpiar entorno de CI:
```bash
docker-compose -f docker-compose-ci.yml down -v
docker system prune -f
```

## Reportes:

Los reportes de Newman se generan en:
- HTML: `report-volume:/workspace/reports/newman-report.html`
- JUnit XML: `report-volume:/workspace/reports/newman-report.xml`

Para acceder a los reportes desde el host:
```bash
docker run --rm -v smartspend-ci_report-volume:/data -v $(pwd):/backup alpine cp -r /data/reports /backup/
```

## Variables de entorno:

El entorno de CI está configurado con valores seguros para testing:
- Base de datos: `smartspend_ci`
- Usuario DB: `smartspend`
- Puerto aplicación: `8443` (mapeado desde 443 interno)
- JWT Secret: Clave específica para testing
- SSL verificación: Deshabilitada para self-signed certificates

## Troubleshooting:

### Si la aplicación no responde al healthcheck:
1. Verifica que el puerto 8443 esté disponible
2. Revisa logs: `docker-compose -f docker-compose-ci.yml logs app-ci`
3. Verifica que la base de datos esté inicializada correctamente

### Si Newman falla:
1. Verifica que la aplicación esté respondiendo: `curl -k https://localhost:8443/actuator/health`
2. Revisa los logs de Newman: `docker-compose -f docker-compose-ci.yml logs newman`
3. Verifica que la colección de Postman esté actualizada

### Si hay problemas de red:
1. Verifica la MTU configurada (1400)
2. Revisa conectividad entre servicios: `docker-compose -f docker-compose-ci.yml exec newman ping app-ci`