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
                withCredentials([
                    usernamePassword(
                        credentialsId: 'Postgres-employee-management',
                        usernameVariable: 'DB_USERNAME',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {
                    withEnv([
                        'DB_URL=jdbc:postgresql://localhost:5432/employeemanagement',
                        'SERVER_PORT=8080'
                    ]) {
                        bat 'mvn clean package'
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                bat '''
                    @echo off
                    echo Checking Docker availability...

                    docker version
                    if errorlevel 1 (
                        echo ERROR: Docker daemon is unavailable.
                        exit /b 1
                    )

                    echo Building Docker image...

                    docker build -t %IMAGE_NAME%:%BUILD_NUMBER% .
                    if errorlevel 1 exit /b 1

                    echo Tagging Docker image...

                    docker tag %IMAGE_NAME%:%BUILD_NUMBER% %IMAGE_NAME%:1.0
                    if errorlevel 1 exit /b 1
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'github-employee-management-docker',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_TOKEN'
                    )
                ]) {
                    bat '''
                        @echo off

                        echo Logging in to Docker Hub...

                        echo %DOCKER_TOKEN% | docker login --username %DOCKER_USER% --password-stdin
                        if errorlevel 1 exit /b 1

                        echo Pushing build-number image...

                        docker push %IMAGE_NAME%:%BUILD_NUMBER%
                        if errorlevel 1 (
                            docker logout
                            exit /b 1
                        )

                        echo Pushing version 1.0 image...

                        docker push %IMAGE_NAME%:1.0
                        if errorlevel 1 (
                            docker logout
                            exit /b 1
                        )

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
                        SANDBOX: [
                            project: 'sandbox',
                            file: '.env.sandbox'
                        ],
                        QA: [
                            project: 'qa',
                            file: '.env.qa'
                        ],
                        UAT: [
                            project: 'uat',
                            file: '.env.uat'
                        ]
                    ]

                    def selected = envConfig[params.ENVIRONMENT]

                    withCredentials([
                        usernamePassword(
                            credentialsId: 'Postgres-employee-management',
                            usernameVariable: 'DB_USERNAME',
                            passwordVariable: 'DB_PASSWORD'
                        )
                    ]) {
                        withEnv([
                            "APP_IMAGE=${env.IMAGE_NAME}:${env.BUILD_NUMBER}",
                            'POSTGRES_DB=employeemanagement'
                        ]) {
                            bat """
                                @echo off

                                echo Checking Docker availability...

                                docker version
                                if errorlevel 1 exit /b 1

                                set "POSTGRES_USER=%DB_USERNAME%"
                                set "POSTGRES_PASSWORD=%DB_PASSWORD%"

                                echo Deploying to ${params.ENVIRONMENT}...

                                docker compose --env-file "${selected.file}" -p ${selected.project} config --quiet
                                if errorlevel 1 exit /b 1

                                docker compose --env-file "${selected.file}" -p ${selected.project} up -d
                                if errorlevel 1 exit /b 1
                            """
                        }
                    }
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

                    bat """
                        @echo off

                        echo Waiting for Spring Boot to start...
                        timeout /t 10 /nobreak >nul

                        echo Checking application and database health...

                        curl.exe --retry 10 --retry-all-errors --retry-delay 3 --retry-connrefused -f http://127.0.0.1:${port}/api/db-check

                        if errorlevel 1 (
                            echo ERROR: Application health verification failed.
                            exit /b 1
                        )

                        echo Application verification successful.
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully for ${params.ENVIRONMENT}."
        }

        failure {
            echo "Pipeline failed for ${params.ENVIRONMENT}. Check the failed stage logs."
        }

        always {
            echo 'Pipeline execution finished.'
        }
    }
}