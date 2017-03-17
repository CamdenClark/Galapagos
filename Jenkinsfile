#!/usr/bin/env groovy

pipeline {

  agent any

  stages {
    stage('Start') {
      steps {
        library 'netlogo-shared'
        sendNotifications('NetLogo/Galapagos', 'STARTED')
      }
    }

    stage('Build and Test') {
      steps {
        library 'netlogo-shared'
        sbt 'scalastyle'
        sbt 'coffeelint'
        sbt 'test'
      }
    }

    stage('Deploy') {
      when {
        branch "master"
      }
      steps {
        library 'netlogo-shared'
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: 'ccl-aws-deploy', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
          sbt 'scrapePlay'
          sbt 'scrapeUpload'
        }
      }
    }
  }

  post {
    always {
      library 'netlogo-shared'
      sendNotifications('NetLogo/Galapagos', currentBuild.result)
    }
  }

}
