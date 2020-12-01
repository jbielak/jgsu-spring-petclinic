pipeline {
    agent any

    triggers { pollSCM('* * * * *') }
    stages {
        stage('Checkout') {
            steps {
                // Get some code from a GitHub repository
                git url: 'https://github.com/jbielak/jgsu-spring-petclinic.git', branch: 'main'
            }
        }
        stage('Build') {
            steps {
                sh './mvnw clean package'
                //sh 'false' // true
            }

            post {
                // If Maven was able to run the tests, even if some of the test
                // failed, record the test results and archive the jar file.
                always  {
                    junit '**/target/surefire-reports/TEST-*.xml'
                    archiveArtifacts 'target/*.jar'
                }
                changed {
                    emailext subject: "Job \'${JOB_NAME}\' (${BUILD_NUMBER}) ${currentBuild.result}",
                        body: "Please go to ${BUILD_URL} and verify the build",
                        attachLog: true,
                        to: 'test@jenkins',
                        compressLog: true,
                        recipientProviders: [upstreamDevelopers(), requestor()]
                }
            }
        }
    }
}
