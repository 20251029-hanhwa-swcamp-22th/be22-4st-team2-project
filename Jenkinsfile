pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'ckato9173'
        IMAGE_TAG = "${BUILD_NUMBER}"
        GITHUB_REPO = 'https://github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git'
        KUBECONFIG = 'C:\\Users\\playdata2\\.kube\\config'
    }

    triggers {
        // 5분마다 GitHub에 변경사항 확인 → 변경 있으면 자동 빌드
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                withCredentials([string(
                    credentialsId: 'github-token',
                    variable: 'GITHUB_TOKEN'
                )]) {
                    checkout scmGit(
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[
                            url: "https://${GITHUB_TOKEN}@github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git"
                        ]]
                    )
                }
            }
        }

        stage('Backend Test') {
            steps {
                bat 'gradlew.bat clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Docker Build') {
            parallel {
                stage('Backend Image') {
                    steps {
                        bat "docker build -t %DOCKER_REGISTRY%/salesboost-backend:%IMAGE_TAG% -t %DOCKER_REGISTRY%/salesboost-backend:latest ."
                    }
                }
                stage('Frontend Image') {
                    steps {
                        bat "docker build -t %DOCKER_REGISTRY%/salesboost-frontend:%IMAGE_TAG% -t %DOCKER_REGISTRY%/salesboost-frontend:latest ./frontend"
                    }
                }
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat 'echo %DOCKER_PASS%| docker login -u %DOCKER_USER% --password-stdin'
                    bat "docker push %DOCKER_REGISTRY%/salesboost-backend:%IMAGE_TAG%"
                    bat "docker push %DOCKER_REGISTRY%/salesboost-backend:latest"
                    bat "docker push %DOCKER_REGISTRY%/salesboost-frontend:%IMAGE_TAG%"
                    bat "docker push %DOCKER_REGISTRY%/salesboost-frontend:latest"
                }
            }
        }

        stage('Deploy to K8s') {
            steps {
                bat 'kubectl apply -f infra/k8s/common.yaml'
                bat 'kubectl apply -f infra/k8s/deployments/backend.yaml'
                bat 'kubectl apply -f infra/k8s/deployments/frontend.yaml'
                bat 'kubectl apply -f infra/k8s/ingress.yaml'
                bat 'kubectl rollout restart deployment salesboost-backend'
                bat 'kubectl rollout restart deployment salesboost-frontend'
            }
        }

        stage('Verify') {
            steps {
                bat 'kubectl rollout status deployment salesboost-backend --timeout=180s'
                bat 'kubectl rollout status deployment salesboost-frontend --timeout=60s'
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS - image tag: ${IMAGE_TAG}"
        }
        failure {
            echo 'Pipeline FAILED'
        }
        always {
            bat 'docker logout || exit 0'
        }
    }
}
