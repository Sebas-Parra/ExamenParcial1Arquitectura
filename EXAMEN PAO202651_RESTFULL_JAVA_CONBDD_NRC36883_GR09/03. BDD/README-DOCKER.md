# Bases de datos con Docker

Desde la carpeta raíz del proyecto, inicia Docker Desktop y ejecuta:

```powershell
docker compose up -d
docker compose ps
```

MariaDB escucha en `localhost:3306`. Al crear el volumen por primera vez, Compose carga `COMERCIALIZADORA.sql` y `BANQUITO.sql`, y configura usuarios para que Payara, ejecutándose en Windows, pueda conectarse.

Las conexiones que esperan los servidores ya coinciden con esta configuración:

| Base | Usuario | Clave |
|---|---|---|
| `comercializadora` | `comercializadora` | `comercializadora` |
| `banquito` | `banquito` | `banquito` |

Para ver el inicio de MariaDB:

```powershell
docker compose logs -f db
```

Para detenerlo sin borrar las bases:

```powershell
docker compose down
```

El volumen conserva los datos y los scripts de inicialización solo se ejecutan al crearlo por primera vez. `docker compose down -v` elimina el volumen y todas las bases guardadas; al iniciar después, se vuelven a cargar los datos de ejemplo. No lo uses si quieres conservar los datos actuales.

El puerto `3306` debe estar libre en Windows. Puedes cambiar la contraseña de `root` definiendo `MARIADB_ROOT_PASSWORD` antes de ejecutar Compose; los usuarios que usan Payara mantienen las credenciales indicadas arriba.
