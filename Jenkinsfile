pipeline {
  agent {
    docker {
      image 'rhel6'
    }
    
  }
  stages {
    stage('Prepare') {
      steps {
        git(url: 'https://github.com/galeb/galeb-libraries.git', branch: 'develop')
      }
    }
    stage('Build') {
      steps {
        sh 'export JAVA_HOME=/opt/java18/ ; mvn clean install -DskipTests'
      }
    }
    stage('Tests') {
      steps {
        sh 'export JAVA_HOME=/opt/java18/ ; mvn test'
      }
    }
    stage('Jacoco') {
      steps {
        sh 'export JAVA_HOME=/opt/java18/ ; mvn jacoco:prepare-agent jacoco:prepare-agent-integration'
      }
    }
    stage('Sonar') {
      steps {
        sh 'export JAVA_HOME=/opt/java18/ ; mvn sonar:sonar'
      }
    }
  }
}