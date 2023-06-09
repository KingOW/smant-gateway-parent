pipeline {
    agent any //在任何可用的代理上执行流水线或阶段
    environment {//环境变量
        GIT_PROJECT_ADDR = "https://github.com/KingOW/smant-gateway-parent.git"//gitee项目地址
        GIT_PROJECT_NAME = "${params.PROJECT_NAME}"//项目名称;服务名称
        GIT_PROJECT_BRANCH = "${params.GIT_BRANCH}"
        GIT_CREDENTIALS_ID = "87283845-bfca-4acb-950a-b3c855d526af"//git 凭证
        PROJECT_JAR_NAME = "${params.PROJECT_NAME}.jar"
        //IMAGE_REGISTRY = "139.224.1.8:8443"
        IMAGE_REGISTRY = "172.20.246.206:8443"
        //172.20.246.206
        IMAGE_REGISTRY_PROTOCOL = "https"
        IMAGE_REGISTRY_CREDENTIALS_ID = "30e7f955-480a-4a7c-8125-91f570e6796d" //镜像仓库凭证
        IMAGE_ADDR = "${env.IMAGE_REGISTRY}/smant"; //镜像仓库地址
        IMAGE_NAME = "${env.GIT_PROJECT_NAME}"  // 镜像名一般和项目名相同
        VERSION = sh(script: "echo `date '+%Y%m%d%H%M%S'`", returnStdout: true).trim()
        IMAGE_VERSION_ID = "${env.GIT_PROJECT_BRANCH}-${env.VERSION}"
        PROJECT_WORKSPACE = "/smant/workspace/${env.GIT_PROJECT_NAME}/${env.GIT_PROJECT_BRANCH}"
        //REMOTE_SERVER="106.14.68.104"
//        REMOTE_SERVER="172.20.246.204"
//        REMOTE_USER="root"
//        REMOTE_WORKSPACE="/ylsk_worker/workspace/${env.GIT_PROJECT_NAME}/${params.GIT_BRANCH}"
//        REMOTE_SECRET_PEM="/ylsk_worker/EGS3_Test.pem"
    }

    tools {
        maven 'jenkins-maven3.8.1_2'
        jdk 'jdk17'
    }

    stages {
        //构建初始化
        stage(Initilization) {
            steps {
                script {
                    echo "描述构建信息;构建id ${env.BUILD_ID}"
                    currentBuild.displayName = "${env.IMAGE_VERSION_ID} - ${env.BUILD_ID}"
                    currentBuild.description = "发布${env.GIT_PROJECT_NAME} - ${env.IMAGE_VERSION_ID} - ${env.BUILD_ID}"
                    EXIST_PROJECT_WORKSPACE = fileExists "${env.PROJECT_WORKSPACE}"
                    if (!EXIST_PROJECT_WORKSPACE) {
                        sh "mkdir  -p  ${env.PROJECT_WORKSPACE}"
                    }
                }
            }
        }
        //检出代码
        stage("Check Out") {
            steps {
                echo "========Check Out ${env.GIT_PROJECT_NAME} From Git========"
                git branch: "${params.GIT_BRANCH}", credentialsId: "${env.GIT_CREDENTIALS_ID}", url: "${env.GIT_PROJECT_ADDR}"
            }
        }
        //编译&打包
        stage("Compile&Package") {
            steps {
                dir("${env.GIT_PROJECT_NAME}") {//进入项目工作目录
                    echo "========Compile&Package ${env.GIT_PROJECT_NAME} ========"
                    // 在有Jenkinsfile同一个目录下（项目的根目录下）
                    sh "mvn -B -U -DskipTests clean compile package -P ${params.RUN_ENV}"
                }
            }
        }

        stage("K8S-Deploy") {
            when { environment name: 'GIT_PROJECT_BRANCH', value: 'main' }
            steps {
                echo "========Deploy ${env.GIT_PROJECT_NAME}  Docker Image & Push ========"
                script {
                    //target/lib
                    sh "cp -r Dockerfile kubernetes.yaml ${env.GIT_PROJECT_NAME}/target/lib   ${env.GIT_PROJECT_NAME}/target/${env.PROJECT_JAR_NAME}  ${env.PROJECT_WORKSPACE}"
                }
                dir("${env.PROJECT_WORKSPACE}") {//进入项目工作目录

                    sh """
                         sed -i "s|@{project_run_jar}@|"${env.PROJECT_JAR_NAME}"|g"  Dockerfile
                       """
                    script {
                        def customImage = docker.build("${env.IMAGE_ADDR}/${env.GIT_PROJECT_NAME}:${env.IMAGE_VERSION_ID}", ".")
                        docker.withRegistry("${env.IMAGE_REGISTRY_PROTOCOL}://${env.IMAGE_REGISTRY}/${env.GIT_PROJECT_NAME}", "${env.IMAGE_REGISTRY_CREDENTIALS_ID}") {
                            customImage.push()
                        }
                    }
                    sh """
                            echo "镜像名称：${env.IMAGE_ADDR}/${env.GIT_PROJECT_NAME}:${env.IMAGE_VERSION_ID}"
                            sed  -i "s|%{image_name}%|"${env.IMAGE_ADDR}/${env.GIT_PROJECT_NAME}:${env.IMAGE_VERSION_ID}"|g"  kubernetes.yaml
                        """

                    sh """
                           echo "执行kubectl apply"
                           kubectl apply -f kubernetes.yaml
                       """
                    sh """
                    docker rmi -f `docker images | grep "${env.GIT_PROJECT_NAME}" | awk '{print \$3}'`
                   """
                }
            }

        }
//        stage("Test-Deploy") {
//            when { environment name: 'GIT_PROJECT_BRANCH', value: 'test' }
//            steps {
//                echo "========Deploy ${env.GIT_PROJECT_NAME}  ========"
//                script {
//                    sh "cp -r run.sh Dockerfile kubernetes.yaml target/lib target/config  target/${env.PROJECT_JAR_NAME}  ${env.PROJECT_WORKSPACE}"
//                }
//                dir("${env.PROJECT_WORKSPACE}") {//进入项目工作目录
//                    sh """
//                            scp -i ${env.REMOTE_SECRET_PEM} -r lib/ config/  ${env.PROJECT_JAR_NAME}   ${env.REMOTE_USER}@${env.REMOTE_SERVER}:${env.REMOTE_WORKSPACE}/tmp
//                            scp -i ${env.REMOTE_SECRET_PEM}  run.sh  ${env.REMOTE_USER}@${env.REMOTE_SERVER}:${env.REMOTE_WORKSPACE}
//                            ssh -i ${env.REMOTE_SECRET_PEM}  ${env.REMOTE_USER}@${env.REMOTE_SERVER} "cd ${env.REMOTE_WORKSPACE};nohup sh run.sh &"
//                     """
//                }
//
//            }
//        }
//        stage("Dev-Deploy") {
//            when { environment name: 'GIT_PROJECT_BRANCH', value: 'dev' }
//            steps {
//                echo "3) ========Build ${env.GIT_PROJECT_NAME}  Docker Image ========"
//                echo "3.1) Push jar,config etc to target workspace "
//            }
//        }
    }
    post{
        success{
            echo "清除工作空间"
            sh """
                rm -r "${env.PROJECT_WORKSPACE}"/"${env.PROJECT_JAR_NAME}"
                rm -r "${env.PROJECT_WORKSPACE}"/lib
            """
        }
    }
}
//              rm -r "${env.PROJECT_WORKSPACE}"/config