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
                script {
                    def props = [:]
                    def envFile = readFile(file: '.env')

                    envFile.eachLine { line ->
                        if (line.trim() && !line.trim().startsWith('#')) {
                            def idx = line.indexOf('=')
                            if (idx > 0) {
                                def key = line.substring(0, idx).trim()
                                def value = line.substring(idx + 1).trim()
                                props[key] = value
                            }
                        }
                    }

                    withEnv([
                        "DB_URL=${props.get('DB_URL', 'jdbc:postgresql://localhost:5432/employeemanagement')}",
                        "DB_USERNAME=${props.get('DB_USERNAME', 'postgres')}",
                        "DB_PASSWORD=${props.get('DB_PASSWORD', 'root')}",
                        "SERVER_PORT=${props.get('SERVER_PORT', '8080')}"
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
                    docker version >nul 2>&1
                    if errorlevel 1 (
                        echo Docker daemon unavailable on this Jenkins agent. Skipping Docker build.
                        exit /b 0
                    )

                    docker build -t %IMAGE_NAME%:%BUILD_NUMBER% .
                    docker tag %IMAGE_NAME%:%BUILD_NUMBER% %IMAGE_NAME%:1.0
                '''
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
                        docker version >nul 2>&1
                        if errorlevel 1 (
                            echo Docker daemon unavailable. Skipping Docker Hub push.
                            exit /b 0
                        )

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
                        QA:      [project: 'qa', file: '.env.qa'],
                        UAT:     [project: 'uat', file: '.env.uat']
                    ]

                    def selected = envConfig[params.ENVIRONMENT]

                    bat """
                        @echo off
                        docker version >nul 2>&1
                        if errorlevel 1 (
                            echo Docker daemon unavailable. Skipping deployment.
                            exit /b 0
                        )

                        set "APP_IMAGE=%IMAGE_NAME%:%BUILD_NUMBER%"
                        docker compose --env-file "${selected.file}" -p ${selected.project} up -d
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