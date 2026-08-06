#!/bin/sh
set -e

# Railway (y la mayoria de PaaS) le asignan el puerto publico via $PORT en runtime,
# no siempre 8080 - hay que ajustar el Connector de Tomcat a ese puerto antes de arrancar.
PORT="${PORT:-8080}"
sed -i "s/port=\"8080\"/port=\"${PORT}\"/" /usr/local/tomcat/conf/server.xml

exec catalina.sh run
