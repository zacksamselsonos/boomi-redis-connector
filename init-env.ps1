Set-Location (Split-Path $MyInvocation.MyCommand.Path)
mvn install:install-file "-Dfile=./lib/connector-sdk-test-util-2.3.0.jar" "-DgroupId=com.boomi.connsdk" "-DartifactId=connector-sdk-test-util" "-Dversion=2.3.0" "-Dpackaging=jar"
mvn install:install-file "-Dfile=./lib/connector-sdk-api-2.3.0.jar" "-DgroupId=com.boomi.connsdk" "-DartifactId=connector-sdk-api" "-Dversion=2.3.0" "-Dpackaging=jar"
mvn install:install-file "-Dfile=./lib/connector-sdk-util-2.3.0.jar" "-DgroupId=com.boomi.connsdk" "-DartifactId=connector-sdk-util" "-Dversion=2.3.0" "-Dpackaging=jar"
mvn install:install-file "-Dfile=./lib/common-sdk-1.1.7.jar" "-DgroupId=com.boomi" "-DartifactId=common-sdk" "-Dversion=1.1.7" "-Dpackaging=jar"