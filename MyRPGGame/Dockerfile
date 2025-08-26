# Usar uma imagem base com Java 21
FROM openjdk:21-jdk-slim

# Instalar Gradle
RUN apt-get update && apt-get install -y wget unzip \
    && wget https://services.gradle.org/distributions/gradle-8.10-bin.zip \
    && unzip gradle-8.10-bin.zip -d /opt/ \
    && ln -s /opt/gradle-8.10/bin/gradle /usr/bin/gradle \
    && rm gradle-8.10-bin.zip

# Dependências do JavaFX
RUN apt-get update && apt-get install -y libgl1-mesa-glx libgtk-3-0

# Definir workdir na raiz do projeto
WORKDIR /app

# Copiar o projeto inteiro
COPY . .

# Expor a porta (se precisar)
EXPOSE 8080

# Rodar a aplicação a partir do módulo desktop
CMD ["gradle", ":desktop:run", "--no-daemon"]
