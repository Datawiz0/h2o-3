def call(customEnv, buildConfig, timeoutValue, timeoutUnit, block) {

  def AWS_CREDENTIALS_ID = 'AWS S3 Credentials'

  def registry = 'docker.h2o.ai'
  def image = "${registry}/opsh2oai/h2o-3-runtime:${buildConfig.DOCKER_IMAGE_VERSION_TAG}"
  withCredentials([usernamePassword(credentialsId: registry, usernameVariable: 'REGISTRY_USERNAME', passwordVariable: 'REGISTRY_PASSWORD')]) {
      sh "docker login -u $REGISTRY_USERNAME -p $REGISTRY_PASSWORD ${registry}"
      sh "docker pull ${image}"
  }
  withEnv(customEnv) {
    timeout(time: timeoutValue, unit: timeoutUnit) {
      docker.withRegistry("https://${registry}") {
        sh "mkdir -p gradle-user-home"
        docker.image(image).inside('-v /home/0xdiag/smalldata:/home/0xdiag/smalldata -v /home/0xdiag/bigdata:/home/0xdiag/bigdata -v \${WORKSPACE}/gradle-user-home/:\${WORKSPACE}/gradle-user-home') {
          withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: AWS_CREDENTIALS_ID, secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
            sh 'id'
            sh 'printenv'
            block()
          }
        }
      }
    }
  }
}

return this
