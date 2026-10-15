
pipeline {
    agent any

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['SANDBOX', 'QA', 'UAT'],
            description: 'Environment to deploy'
        )
    }

    environment {
        IMAGE_NAME = 'addellyrajesh/employee-service'
    }

    stages {
        stage('Build and Test') {
            steps {
                bat 'mvn clean package'
            }
        }

        stage('Build Docker Image') {
            steps {
                bat 'docker build -t %IMAGE_NAME%:%BUILD_NUMBER% .'
                bat 'docker tag %IMAGE_NAME%:%BUILD_NUMBER% %IMAGE_NAME%:1.0'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-employee-management',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_TOKEN'
                    )
                ]) {
                    bat '''
                        @echo off
                        echo %DOCKER_TOKEN% | docker login -u %DOCKER_USER% --password-stdin
                        if errorlevel 1 exit /b 1

                        docker push %IMAGE_NAME%:%BUILD_NUMBER%
                        if errorlevel 1 exit /b 1

                        docker push %IMAGE_NAME%:1.0
                        if errorlevel 1 exit /b 1

                        docker logout
                    '''
                }
            }
        }

        stage('Approve UAT') {
            when {
                expression {
                    params.ENVIRONMENT == 'UAT'
                }
            }
            steps {
                input(
                    message: 'Approve deployment to UAT?',
                    ok: 'Deploy to UAT'
                )
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def envConfig = [
                        SANDBOX: [project: 'sandbox', file: '.env.sandbox'],
                        QA:      [project: 'qa',      file: '.env.qa'],
                        UAT:     [project: 'uat',     file: '.env.uat']
                    ]

                    def selected = envConfig[params.ENVIRONMENT]

                    bat """
                        set "APP_IMAGE=%IMAGE_NAME%:%BUILD_NUMBER%" && docker compose -p ${selected.project} --env-file ${selected.file} up -d
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                script {
                    def ports = [
                        SANDBOX: '8081',
                        QA: '8082',
                        UAT: '8083'
                    ]

                    def port = ports[params.ENVIRONMENT]

                    bat "curl.exe --retry 10 --retry-delay 3 --retry-connrefused -f http://localhost:${port}/api/db-check"
                }
            }
        }
    }
}
