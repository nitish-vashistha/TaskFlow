#!/bin/bash
# TaskFlow Backend Runner
export JAVA_HOME=${JAVA_HOME:-$HOME/tools/jdk-17.0.2.jdk/Contents/Home}
export PATH="$JAVA_HOME/bin:$HOME/tools/apache-maven-3.9.8/bin:$PATH"

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR/backend" || exit 1

echo "Starting TaskFlow Spring Boot Backend (Java 17)..."
echo "API will be available at: http://localhost:8080"
echo "Swagger Docs: http://localhost:8080/swagger-ui.html"
echo "H2 Console: http://localhost:8080/h2-console"
echo "----------------------------------------------------"

./mvnw spring-boot:run
