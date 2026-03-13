
pipeline {
    agent any

    environment {
        IMAGE_NAME = "football-standing-service"
        CONTAINER_NAME = "football-standing-service"
        APP_PORT = "8080"
    }

    stages {

        stage('Build') {
            steps {
                git url: 'https://github.com/SathishSKM/football-standing-service.git', branch: 'main'
                bat "docker build -t ${IMAGE_NAME} ."
            }
        }
        stage('Destroy') {
            steps {
                bat """
                    docker stop ${CONTAINER_NAME} || exit 0
                    docker rm ${CONTAINER_NAME} || exit 0
                """
            }
        }
        stage('Deploy') {
            steps {
                bat "docker run -d --name ${CONTAINER_NAME} -p ${APP_PORT}:${APP_PORT} ${IMAGE_NAME}"
            }
        }
    }
}