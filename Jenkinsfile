pipeline {
    agent any //在任何可用的代理上执行流水线或阶段
    environment {//环境变量
        GIT_PROJECT_NAME = "${params.GATEWAY_PROJECT}"//项目名称;服务名称
        GIT_CREDENTIALS_ID = "github-user"//git/coding 凭证
        VERSION = sh(script: "echo `date '+%Y%m%d%H'`", returnStdout: true).trim()
        VERSION_ID = "${env.VERSION}-${env.BUILD_ID}"

        IMAGE_REGISTRY_CREDENTIALS_ID = "harbor-user" //镜像仓库凭证
        IMAGE_NAME = "harbor.ryds.cn/ryds/${env.GIT_PROJECT_NAME}:${env.VERSION_ID}";
        IMAGE_PUSH_ADDR = "https://harbor.ryds.cn/ryds/${env.GIT_PROJECT_NAME}"
        IAMGE_K8S_ADDR_DOMAIN = "${env.IMAGE_NAME}"
        IAMGE_K8S_ADDR_INTRANET_IP = "172.16.239.35/ryds/${env.GIT_PROJECT_NAME}:${env.VERSION_ID}"
    }

    tools {
             maven 'jenkins-tool-maven3.9.4'
             jdk 'jenkins-tool-jdk23.0.1'
             nodejs 'jenkins-tool-nodejs14.9.0'
    }

    stages {
        //构建初始化
        stage("Initilization") {
            steps {
                script {
                    echo "描述构建信息;构建id ${env.VERSION_ID}"
                    currentBuild.displayName = "${env.VERSION_ID}"
                    currentBuild.description = "发布${env.GIT_PROJECT_NAME} - ${env.VERSION_ID}"
                }
            }
        }
        //检出代码
//         stage("Check Out") {
//             steps {
//                 echo "========Check Out ${env.GIT_PROJECT_NAME} From Gitee========"
//                 git branch: "${env.GIT_PROJECT_BRANCH}", credentialsId: "${env.GIT_CREDENTIALS_ID}", url: "${env.GIT_PROJECT_ADDR}"
//             }
//         }
        //编译&打包
        stage("Compile&Package") {
            steps {
                echo "========Compile&Package ${env.GIT_PROJECT_NAME} ========"
                // 在有Jenkinsfile同一个目录下（项目的根目录下）
                sh "mvn -B -U -DskipTests clean install"
            }
        }

        //构建 docker image
        stage("Make Docker Image && Push") {
            steps {
                echo "========Deploy ${env.GIT_PROJECT_NAME}  Docker Image & Push ========"
 dir("${env.GIT_PROJECT_NAME}") {//进入项目工作目录
                    script {
                        def customImage = docker.build("${env.IMAGE_NAME}", ".")
                        docker.withRegistry("${env.IMAGE_PUSH_ADDR}", "${env.IMAGE_REGISTRY_CREDENTIALS_ID}") {
                            customImage.push()
                        }
                        }
                }
            }
        }
        stage("Prod Env: Push Kubernetes ") {
            steps {
                    sh """
                           echo "镜像名称：${env.IMAGE_NAME}"
                           sed  -i "s|%{project_name}%|"${env.GIT_PROJECT_NAME}"|g"  kubernetes.yaml
                           sed  -i "s|%{image_name}%|"${env.IAMGE_K8S_ADDR_INTRANET_IP}"|g"  kubernetes.yaml
                           sed  -i "s|%{active_profile}%|prod|g"  kubernetes.yaml
                           echo "执行kubectl apply"
                           kubectl apply -f kubernetes.yaml --kubeconfig=/root/.kube/config-prod
                       """
                }
        }
    }
    post {
        success {
            echo "清除工作空间"
            sh """
            cd  smant-gateway-core
            mvn -B -U -DskipTests clean install deploy
            """
        }
    }
}
