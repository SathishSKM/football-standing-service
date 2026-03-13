pipeline {
    agent any

    environment {
        IMAGE_NAME = "football-standing-service"

        DEV_CONTAINER = "football-standing-service-dev"
        PROD_CONTAINER = "football-standing-service-prod"

        DEV_PORT = "8080"
        PROD_PORT = "8090"
    }

    stages {

        stage('Build') {
            steps {
                git url: 'https://github.com/SathishSKM/football-standing-service.git', branch: 'main'
                bat "docker build -t %IMAGE_NAME% ."
            }
        }

        stage('Deploy_DEV') {
            steps {
                bat """
                    docker run -d --name %DEV_CONTAINER% -p %DEV_PORT%:8080 %IMAGE_NAME%
                """
            }
        }

        stage('Approval_PROD') {
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
            }
        }

        stage('Deploy_PROD') {
            steps {
                bat """
                    docker run -d --name %PROD_CONTAINER% -p %PROD_PORT%:8080 %IMAGE_NAME%
                """
            }
        }
    }
}