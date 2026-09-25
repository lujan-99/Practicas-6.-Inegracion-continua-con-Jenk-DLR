Practica 6 - Jenkins en Docker
==============================

Opcion A (recomendada) - con docker compose, desde esta carpeta:

    docker compose up -d
    docker exec jenkins-com450 cat /var/jenkins_home/secrets/initialAdminPassword

Opcion B - con docker run, desde cualquier carpeta:

    docker pull jenkins/jenkins:lts-jdk17
    docker volume create jenkins_home
    docker run -d --name jenkins-com450 -p 8090:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home --restart unless-stopped jenkins/jenkins:lts-jdk17
    docker exec jenkins-com450 cat /var/jenkins_home/secrets/initialAdminPassword

Luego abre http://localhost:8090 y pega la contrasena.

Detener sin perder nada:   docker stop jenkins-com450
Volver a arrancar:         docker start jenkins-com450
Borrar TODO (cuidado):     docker rm -f jenkins-com450 && docker volume rm jenkins_home
