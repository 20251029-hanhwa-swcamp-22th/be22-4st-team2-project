pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'ckato9173'
        IMAGE_TAG       = "${BUILD_NUMBER}"
        GITHUB_REPO     = 'https://github.com/20251029-hanhwa-swcamp-22th/be22-4st-team2-project.git'
        MANIFEST_REPO   = 'https://github.com/fdrn9999/team2-manifest.git'
        GITOPS_TMP_DIR  = "${WORKSPACE}/gitops-tmp/${BUILD_NUMBER}"
    }

    triggers {
        githubPush()
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

        // [ci skip] 커밋에 의한 재트리거 방지 (GitOps 무한루프 차단)
        stage('Skip CI Check') {
            steps {
                script {
                    def lastCommitMsg
                    if (isUnix()) {
                        lastCommitMsg = sh(script: 'git log -1 --pretty=%B', returnStdout: true).trim()
                    } else {
                        lastCommitMsg = bat(script: '@git log -1 --pretty=%%B', returnStdout: true).trim()
                    }
                    if (lastCommitMsg.contains('[ci skip]') || lastCommitMsg.contains('[skip ci]')) {
                        echo "CI skip detected in commit message: ${lastCommitMsg}"
                        currentBuild.result = 'NOT_BUILT'
                        error('Skipping build — triggered by CI commit')
                    }
                }
            }
        }

        stage('Backend Build & Test') {
            steps {
                script {
                    if (isUnix()) {
                        sh './gradlew clean build'
                    } else {
                        bat 'gradlew.bat clean build'
                    }
                }
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
                        script {
                            if (isUnix()) {
                                sh "docker build -t ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG} ."
                            } else {
                                bat "docker build -t ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG} ."
                            }
                        }
                    }
                }
                stage('Frontend Image') {
                    steps {
                        script {
                            if (isUnix()) {
                                sh "docker build -t ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG} ./frontend"
                            } else {
                                bat "docker build -t ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG} .\\frontend"
                            }
                        }
                    }
                }
            }
        }

        // Trivy 이미지 취약점 스캔 (non-blocking: HIGH/CRITICAL만 리포트)
        stage('Image Security Scan') {
            steps {
                script {
                    if (isUnix()) {
                        sh """
                            docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                                aquasec/trivy:latest image --severity HIGH,CRITICAL --exit-code 0 \
                                ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG} || true
                        """
                        sh """
                            docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                                aquasec/trivy:latest image --severity HIGH,CRITICAL --exit-code 0 \
                                ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG} || true
                        """
                    } else {
                        bat "docker run --rm -v //var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image --severity HIGH,CRITICAL --exit-code 0 ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG} || exit 0"
                        bat "docker run --rm -v //var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image --severity HIGH,CRITICAL --exit-code 0 ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG} || exit 0"
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
                    script {
                        if (isUnix()) {
                            sh 'echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin'
                            sh "docker push ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG}"
                            sh "docker push ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG}"
                        } else {
                            bat 'docker login -u %DOCKER_USER% -p %DOCKER_PASS%'
                            bat "docker push ${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG}"
                            bat "docker push ${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        // GitOps: 별도 매니페스트 레포(team2-manifest)의 이미지 태그를 업데이트하고
        // main 브랜치에 commit & push하여 ArgoCD가 자동 Sync하도록 위임
        stage('Update GitOps Manifest') {
            steps {
                withCredentials([string(
                    credentialsId: 'github-token',
                    variable: 'GITHUB_TOKEN'
                )]) {
                    script {
                        if (isUnix()) {
                            sh """
                                rm -rf "${GITOPS_TMP_DIR}"
                                git clone https://${GITHUB_TOKEN}@github.com/fdrn9999/team2-manifest.git "${GITOPS_TMP_DIR}"
                            """

                            sh """
                                sed -i 's|${DOCKER_REGISTRY}/salesboost-backend:[^ ]*|${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG}|g' "${GITOPS_TMP_DIR}/deployments/backend.yaml"
                                sed -i 's|${DOCKER_REGISTRY}/salesboost-frontend:[^ ]*|${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG}|g' "${GITOPS_TMP_DIR}/deployments/frontend.yaml"
                            """

                            // 동시 파이프라인 실행 시 git push 충돌 방지를 위한 재시도 로직
                            sh """
                                cd "${GITOPS_TMP_DIR}" && \
                                git config --local user.email "jenkins@salesboost.ci" && \
                                git config --local user.name "Jenkins CI" && \
                                git add deployments/backend.yaml deployments/frontend.yaml && \
                                if git diff --cached --quiet; then \
                                    echo "No manifest changes detected, skipping commit"; \
                                else \
                                    git commit -m "ci: update image tags to ${IMAGE_TAG}" && \
                                    for i in 1 2 3; do \
                                        git pull --rebase origin main && \
                                        git push origin main && break || \
                                        echo "Push failed (attempt \$i/3), retrying in 5s..." && \
                                        sleep 5; \
                                    done; \
                                fi
                            """
                        } else {
                            bat "if exist \"${GITOPS_TMP_DIR}\" rmdir /s /q \"${GITOPS_TMP_DIR}\""

                            // 매니페스트 레포 clone
                            // credential.helper= : Windows GCM이 URL 토큰을 무시하는 것을 방지
                            bat "git -c credential.helper= clone https://%GITHUB_TOKEN%@github.com/fdrn9999/team2-manifest.git \"${GITOPS_TMP_DIR}\""

                            // Windows에서는 PowerShell로 sed 대체
                            powershell """
                                \$backend = '${GITOPS_TMP_DIR}/deployments/backend.yaml'
                                \$frontend = '${GITOPS_TMP_DIR}/deployments/frontend.yaml'
                                (Get-Content \$backend) -replace '${DOCKER_REGISTRY}/salesboost-backend:[^ ]*', '${DOCKER_REGISTRY}/salesboost-backend:${IMAGE_TAG}' | Set-Content \$backend
                                (Get-Content \$frontend) -replace '${DOCKER_REGISTRY}/salesboost-frontend:[^ ]*', '${DOCKER_REGISTRY}/salesboost-frontend:${IMAGE_TAG}' | Set-Content \$frontend
                            """

                            dir("${GITOPS_TMP_DIR}") {
                                bat 'git config --local user.email "jenkins@salesboost.ci"'
                                bat 'git config --local user.name "Jenkins CI"'
                                bat 'git add deployments/backend.yaml deployments/frontend.yaml'
                                def hasChanges = bat(script: '@git diff --cached --quiet', returnStatus: true)
                                if (hasChanges != 0) {
                                    bat "git commit -m \"ci: update image tags to ${IMAGE_TAG}\""
                                    bat "git -c credential.helper= pull --rebase origin main"
                                    bat "git -c credential.helper= push origin main"
                                } else {
                                    echo 'No manifest changes detected, skipping commit'
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline SUCCESS - image tag: ${IMAGE_TAG} pushed to team2-manifest. ArgoCD will sync to cluster."
            script {
                if (isUnix()) {
                    sh '''
                        if command -v argocd &> /dev/null; then
                            echo "Checking ArgoCD sync status..."
                            argocd app wait salesboost --timeout 180 --health || \
                                echo "WARNING: ArgoCD health check timed out. Manual verification required."
                        else
                            echo "argocd CLI not found. Please verify deployment manually."
                        fi
                    '''
                } else {
                    bat(script: '''
                        where argocd >nul 2>nul && (
                            echo Checking ArgoCD sync status...
                            argocd app wait salesboost --timeout 180 --health || echo WARNING: ArgoCD health check timed out. Manual verification required.
                        ) || (
                            echo argocd CLI not found. Please verify deployment manually.
                        )
                        exit /b 0
                    ''', returnStatus: true)
                }
            }
        }
        failure {
            echo "Pipeline FAILED at build ${IMAGE_TAG}"
        }
        always {
            script {
                if (isUnix()) {
                    sh 'docker logout || true'
                    sh "rm -rf \"${GITOPS_TMP_DIR}\""
                } else {
                    bat 'docker logout 2>nul || exit 0'
                    bat "if exist \"${GITOPS_TMP_DIR}\" rmdir /s /q \"${GITOPS_TMP_DIR}\""
                }
            }
        }
    }
}
