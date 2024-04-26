# Smartpush
Follow these steps to deploy a new version.

## How to deploy a new version

### Prerequisites
1. The command line gpg tool — GPG Suite for macOS or Gpg4win for Windows
2. The keys to the next step

### Update version number
#### Into `gcm-smartpush-lib/build.gradle` update the `versionName`

### Setup the values into the `local.properties`
```
signing.keyId=KEY_ID
signing.password=PASSWORD
signing.key=KEY_64
ossrhUsername=sonatype_USERNAME
ossrhPassword=sonatype_PASSWORD
sonatypeStagingProfileId=sonatype_profileID
```

### Run the deploy command
```
./gradlew gcm-smartpush-lib:publishReleasePublicationToSonatypeRepository closeAndReleaseSonatypeStagingRepository
```

#### It will be uploaded here
https://s01.oss.sonatype.org/#stagingRepositories

### Check the deploy here
https://repo1.maven.org/maven2/br/com/getmo/smartpush

### To use
```
implementation 'br.com.getmo:smartpush:{versionName}'
```
