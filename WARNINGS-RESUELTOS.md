# ⚠️ Warnings Resueltos — Cerrajería API (Spring Boot)

> Documento de referencia que explica los warnings que aparecen comúnmente al crear un proyecto
> Spring Boot desde cero, por qué ocurren y cómo se solucionan correctamente.

---

## 1. PostgreSQL Dialect — `HHH90000025`

### Warning original
```
HHH90000025: PostgreSQLDialect does not need to be specified explicitly
using 'hibernate.dialect' (remove the property setting and it will be selected by default)
```

### ¿Por qué ocurre?
Desde **Hibernate 6+** (incluido en Spring Boot 3.x), Hibernate detecta automáticamente
el dialecto SQL a partir de la conexión JDBC. Antes (Hibernate 5 / Spring Boot 2.x) era
**obligatorio** especificarlo manualmente, y muchos tutoriales antiguos aún lo incluyen.

Cuando lo declaras explícitamente, Hibernate te avisa que es redundante y que la
auto-detección es más confiable (si cambias de PostgreSQL 15 a 16, el dialecto se
ajusta solo).

### Solución
Eliminar la línea del `application.properties`:

```diff
- spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

Hibernate selecciona `PostgreSQLDialect` automáticamente al conectarse a la base de datos.

---

## 2. Open-In-View — `spring.jpa.open-in-view`

### Warning original
```
spring.jpa.open-in-view is enabled by default. Therefore, database queries may be
performed during view rendering. Explicitly configure spring.jpa.open-in-view to
disable this warning.
```

### ¿Por qué ocurre?
**Open Session in View (OSIV)** es un patrón que mantiene la sesión de Hibernate abierta
durante toda la petición HTTP, incluso durante la serialización JSON de la respuesta.

**Ventaja aparente:** Las relaciones `@ManyToOne(fetch = LAZY)` se cargan automáticamente
cuando Jackson las serializa, sin necesidad de hacer `fetch join` o `@Transactional`.

**El problema real:**
- Las queries SQL se ejecutan **fuera del servicio**, en la capa del controlador o
  incluso durante la serialización, lo que hace impredecible el rendimiento.
- Puede causar el famoso **N+1 problem** sin que te des cuenta (una query por cada
  entidad relacionada).
- En APIs REST (como la nuestra) no tiene sentido — los DTOs se construyen en el
  servicio, no se serializan entidades directamente.

Spring Boot te avisa porque lo activa **por defecto** y quiere que tomes una decisión
explícita.

### Solución
Desactivarlo explícitamente en `application.properties`:

```properties
spring.jpa.open-in-view=false
```

> **Nota:** Si después de desactivarlo obtienes `LazyInitializationException`, significa
> que estabas dependiendo de OSIV sin saberlo. La solución correcta es usar `JOIN FETCH`
> en tus queries o cargar los datos necesarios dentro del `@Service`.

---

## 3. PageImpl Serialization — `PagedModel`

### Warning original
```
Serializing PageImpl instances as-is is not supported, meaning that there is no
guarantee about the stability of the resulting JSON structure!
For a stable JSON structure, please use Spring Data's PagedModel
(globally via @EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO))
```

### ¿Por qué ocurre?
Cuando un `@RestController` retorna un `Page<T>` directamente, Jackson serializa el
objeto `PageImpl` interno de Spring Data. El problema es que `PageImpl` es una clase
**interna de implementación** — su estructura JSON puede cambiar entre versiones de
Spring sin previo aviso.

Por ejemplo, entre Spring Boot 3.2 y 3.3, los campos del JSON cambiaron de nombre,
rompiendo frontends que dependían de la estructura anterior.

### Solución
Agregar la anotación `@EnableSpringDataWebSupport` en la clase principal:

```java
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class CerrajeriaApiApplication { ... }
```

Esto envuelve automáticamente las respuestas `Page<T>` en un `PagedModel` con estructura
estable y documentada:

```jsonc
// ANTES (inestable — puede cambiar entre versiones)
{
  "content": [...],
  "number": 0,          // campo plano
  "totalPages": 5,      // campo plano
  "last": true,         // campo plano
  "totalElements": 30
}

// DESPUÉS con VIA_DTO (estructura estable y garantizada)
{
  "content": [...],
  "page": {
    "size": 6,
    "number": 0,
    "totalElements": 30,
    "totalPages": 5
  }
}
```

> **⚠️ Importante:** Al activar `VIA_DTO`, los metadatos de paginación se mueven dentro
> de un objeto `page`. Cualquier frontend que lea `data.number` o `data.last` directamente
> debe actualizarse a `data.page.number` y calcular `data.page.number + 1 < data.page.totalPages`.

---

## 4. AuthenticationProvider — `InitializeUserDetailsBeanManagerConfigurer`

### Warning original
```
Global AuthenticationManager configured with an AuthenticationProvider bean.
UserDetailsService beans will not be used by Spring Security for automatically
configuring username/password login. Consider removing the AuthenticationProvider bean.
```

### ¿Por qué ocurre?
Spring Security tiene dos formas de configurar la autenticación:

1. **Automática:** Detecta un bean `UserDetailsService` y crea internamente un
   `DaoAuthenticationProvider` con el `PasswordEncoder` disponible.

2. **Manual:** Tú defines un bean `AuthenticationProvider` explícitamente (como hacemos
   nosotros con `DaoAuthenticationProvider`).

Cuando usas la opción 2, Spring detecta que **también** existe un `UserDetailsService`
como bean (nuestro `UserService`) y te avisa que lo va a **ignorar** en favor de tu
`AuthenticationProvider` manual.

Es un warning **informativo**, no un error. Spring quiere asegurarse de que es
intencional y no un accidente de configuración.

### ¿Cuándo SÍ es un problema?
Si defines un `AuthenticationProvider` pero olvidas pasarle el `UserDetailsService`,
tu autenticación no funcionará y este warning sería la pista.

### Solución
Como en nuestro caso **es intencional** (necesitamos el `AuthenticationProvider` manual
para inyectar nuestro `PasswordEncoder` sin crear dependencias circulares), suprimimos
el warning elevando su nivel de log:

```properties
logging.level.org.springframework.security.config.annotation.authentication.configuration.InitializeUserDetailsBeanManagerConfigurer=ERROR
```

> **Alternativa:** Podrías eliminar el bean `AuthenticationProvider` del `SecurityConfig`
> y dejar que Spring lo configure automáticamente. Pero en proyectos con configuración
> personalizada de seguridad (JWT, múltiples providers, etc.), es preferible mantener
> el control explícito.

---

## 5. Constraint Skip — `SQL Warning Code: 0`

### Warning original
```
SQL Warning Code: 0, SQLState: 00000
constraint "ukqa4d4dyerwkhp0iihkno5k45n" of relation "ck_promotion_like" does not exist, skipping
```

### ¿Por qué ocurre?
Hibernate intenta ejecutar `ALTER TABLE ... DROP CONSTRAINT ukqa4d4dyerwkhp0iihkno5k45n`
antes de recrear la constraint `UNIQUE` en la tabla `ck_promotion_like`. Si la constraint
no existe (porque la tabla fue recreada o es la primera vez), PostgreSQL emite este aviso.

Es un warning de **PostgreSQL** (no de Hibernate) y ocurre únicamente durante el
arranque con `ddl-auto=create` o `update`. No afecta la funcionalidad.

### Solución
No requiere acción. Desaparece automáticamente una vez que:
- La tabla existe y tiene la constraint creada.
- Se usa `ddl-auto=update` (no `create`) en producción.

> **En producción** se recomienda usar `ddl-auto=none` o `validate` y manejar las
> migraciones con **Flyway** o **Liquibase**, lo que elimina por completo este tipo
> de warnings.

---

## Resumen

| # | Warning | Causa raíz | Solución |
|---|---------|-----------|----------|
| 1 | `HHH90000025` PostgreSQL Dialect | Dialecto declarado manualmente (innecesario desde Hibernate 6) | Eliminar `hibernate.dialect` del properties |
| 2 | `open-in-view` | OSIV activo por defecto, anti-patrón en APIs REST | `spring.jpa.open-in-view=false` |
| 3 | `PageImpl serialization` | Serialización de clase interna inestable | `@EnableSpringDataWebSupport(VIA_DTO)` |
| 4 | `AuthenticationProvider` | Spring detecta `UserDetailsService` ignorado | Subir nivel de log a ERROR (es intencional) |
| 5 | `constraint does not exist` | DDL intenta eliminar constraint inexistente | Normal con `ddl-auto=create`, usar `update` o migraciones |

---

*Documento generado durante el desarrollo del proyecto Cerrajería AutoKeys.*
*Última actualización: Febrero 2026.*
