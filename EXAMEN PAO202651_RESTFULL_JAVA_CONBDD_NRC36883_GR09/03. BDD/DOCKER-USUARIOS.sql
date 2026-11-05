-- Permite que los servidores Payara ejecutados fuera de Docker se conecten
-- a las bases del contenedor por el puerto publicado en localhost:3306.
CREATE USER IF NOT EXISTS 'comercializadora'@'%' IDENTIFIED BY 'comercializadora';
GRANT ALL PRIVILEGES ON comercializadora.* TO 'comercializadora'@'%';

CREATE USER IF NOT EXISTS 'banquito'@'%' IDENTIFIED BY 'banquito';
GRANT ALL PRIVILEGES ON banquito.* TO 'banquito'@'%';
