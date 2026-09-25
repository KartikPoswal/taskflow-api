pipeline {
    agent any

    environment {
        APP_NAME     = 'taskflow-api'
        DOCKER_IMAGE = 'kartikposwal/taskflow-api'
        TAG          = "v${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                sh 'git rev-parse --short HEAD'
            }
        }

        stage('Build') {
            steps {
                echo '=== Stage 1: Build ==='
                sh 'mvn -B clean package -DskipTests'
                sh "docker build -t ${DOCKER_IMAGE}:${TAG} -t ${DOCKER_IMAGE}:latest ."
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Test') {
            steps {
                echo '=== Stage 2: Test ==='
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec',
                           classPattern: 'target/classes',
                           sourcePattern: 'src/main/java',
                           exclusionPattern: '**/*Test*.class'
                }
            }
        }

        
	stage('Code Quality') {
		steps {
                echo '=== Stage 3: Code Quality ==='
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:3.10.0.2594:sonar -Dsonar.projectKey=taskflow-api -Dsonar.host.url=$SONAR_HOST_URL -Dsonar.login=$SONAR_AUTH_TOKEN'
                }
            }
        }
        stage('Security') {
            steps {
                echo '=== Stage 4: Security ==='
                sh 'mvn -B org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=9 || true'
            }
        }

        stage('Deploy') {
            steps {
                echo '=== Stage 5: Deploy to Staging ==='
                sh 'docker compose down || true'
                sh "TAG=${TAG} ENV=staging docker compose up -d"
                sh 'sleep 100'
                sh 'curl -fsS http://localhost:8085/actuator/health | grep UP'
            }
        }

        stage('Release') {
            when { branch 'main' }
            steps {
                echo '=== Stage 6: Release ==='
                sh "docker tag ${DOCKER_IMAGE}:${TAG} ${DOCKER_IMAGE}:latest || true"
            }
        }

        stage('Monitoring') {
            steps {
                echo '=== Stage 7: Monitoring ==='
                sh 'docker compose ps'
                sh 'curl -fsS http://localhost:9090/-/healthy'
                echo "Grafana: http://localhost:3000 | Prometheus: http://localhost:9090"
            }
        }
    }

    post {
        success { echo "Pipeline ${env.BUILD_NUMBER} succeeded — ${TAG} released." }
        failure { echo "Pipeline ${env.BUILD_NUMBER} failed. Check logs." }
    }
}