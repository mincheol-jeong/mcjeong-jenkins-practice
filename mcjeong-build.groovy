import java.text.SimpleDateFormat
def NEW_DATE = "null"
pipeline {
    agent {label 'mcjeong'}

    environment {
        JWF = '/mnt/jenkins-work'
        BRF = '/mnt/jenkins-work/build'
        SF = '/mnt/jenkins-work/service'
        SBINF = '/mnt/jenkins-work/sbin'
    }

    stages {
       stage('Version Change') {
           steps {
               
               script {
                   sh('rm -rf ${BRF}/*')
                   def versionInfo = readFile(file: '/mnt/jenkins-work/versionInfo.txt')
                   println(versionInfo)
                   def (codeName, version, date) = versionInfo.split('-')
                   println(codeName+version+date)
                   def (a,b,c) = version.split('\\.')
                   int intC = c as int
                   println(intC)
                   def dateFormat = new SimpleDateFormat('yyMMdd')
                   def today = new Date()
                   def newDate = dateFormat.format(today)
                   def newVersionInfo = codeName + '-' + a + '.' + b + '.' + intC.toString() + '-' + newDate
                   println('newVersionInfo : '+newVersionInfo)
                   writeFile file: '/mnt/jenkins-work/versionInfo.txt', text: newVersionInfo

                   sh('cat ${JWF}/versionInfo.txt')
                   sh('rm -rf ${JWF}/'+newDate)
                   sh('mkdir ${JWF}/'+newDate)
                   NEW_DATE=newDate
               }
           }
       }

       stage('Cockpit Build') {
           steps{

               build 'mcjeong-cockpit-test'
           }
       }
        
        stage('Cockpit-plugin Build') {
           steps{

               build 'mcjeong-cockpit-plugin-test'
           }
       }

        stage('Glue Build') {
            steps{

                build 'mcjeong-glue-test'
            }
       }


        stage('Mold Build') {
            steps{

                build 'mcjeong-mold-test'
            }
        }

        stage('Netdive Build') {
            steps{

                build 'mcjeong-netdive-test'
            }
        }

        stage('Wall Build') {
            steps{

                build 'mcjeong-wall-test'
            }
        }

       stage('Build result file Move to version folder') {
           steps{

                  sh("""cp -r ${BRF}/* ${JWF}/${NEW_DATE}/""")
           }
       }

        stage('Ablestack Template Create') {
            steps{

                build 'mcjeong-template-test'
            }
        }
        
        stage('Ablestack ISO Create') {
            steps{

                build 'mcjeong-kickstart-test'
            }
        }
        
        stage('Ablestack ISO Copy') {
            steps{
                sh("""cp -r ${JWF}/ISO/* ${JWF}/${NEW_DATE}/""")
                sh("""rm -rf ${JWF}/ISO/*""")
            }
        }
    }
}