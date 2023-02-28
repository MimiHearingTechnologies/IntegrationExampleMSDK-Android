# Integration

## Gradle Wrapper

Make sure you are using a minimum of Gradle version 7.5.1 in your project.

Check **gradle/wrapper/gradle-wrapper.properties**

```groovy
distributionUrl=https\://services.gradle.org/distributions/gradle-7.5.1-all.zip
```

If the version is below **7.5.1**, then execute the below command in terminal to upgrade your gradle wrapper.
> Note: You can replace `7.5.1` with the any other gradle version.

```bash
./gradlew wrapper --gradle-version=7.5.1 --distribution-type=all
```

## Credentials

Add credentials in project's **local.properties**

```txt
# ========================= Mimi =========================
# Mimi Portal Access Credentials
mimiMavenUser=PORTAL_USERNAME
mimiMavenPassword=PORTAL_PASSWORD

# Client Credentials
# NOTE: The quotes are needed around value string
mimiClientID="CLIENT_ID"
mimiClientSecret="CLIENT_SECRET"
# ========================================================
```

## Dependency

Add helper function to the end of root `build.gradle` file to get values from `local.properties` file:

```groovy
//region Get ENV vars
def getBuildProperty(String localProperty, String environmentVariable, String orDefault = "undefined") {
    return getLocalProperty(localProperty) ?: System.getenv(environmentVariable) ?: orDefault
}

def getLocalProperty(String key) {
    Properties localProperties = new Properties()
    if (file("local.properties").exists()) {
        localProperties.load(file("local.properties").newDataInputStream())
    }
    return localProperties[key]
}
//endregion
```

Next, add mimi maven repo inside your `repositories` block.

- If your `repositories` block is inside **project/build.gradle** 

```groovy
allprojects {
    // Define Build properties
    ext {
        mimiMavenUser = getBuildProperty("mimiMavenUser", "PORTAL_USERNAME")
        mimiMavenPassword = getBuildProperty("mimiMavenPassword", "PORTAL_PASSWORD")
    }

    repositories {
        google()
        mavenCentral()

        // Mimi artifacts repository
        maven {
            url "https://api.integrate.mimi.io/files/sdk/android"
            credentials {
                username = "${mimiMavenUser}"
                password = "${mimiMavenPassword}"
            }
            authentication {
                basic(BasicAuthentication)
            }
        }
    }
}
```

- If your `repositories` block is inside **project/settings.gradle**

```groovy
dependencyResolutionManagement {
    // Define Build properties
    ext {
        mimiMavenUser = getBuildProperty("mimiMavenUser", "PORTAL_USERNAME")
        mimiMavenPassword = getBuildProperty("mimiMavenPassword", "PORTAL_PASSWORD")
    }

    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // Mimi artifacts repository
        maven {
            url "https://api.integrate.mimi.io/files/sdk/android"
            credentials {
                username = "${mimiMavenUser}"
                password = "${mimiMavenPassword}"
            }
            authentication {
                basic(BasicAuthentication)
            }
        }
    }
}
```

Finally inside the root `build.gradle` file add `ext` properties like below:

```groovy
ext {
    mimiClientID = getBuildProperty("mimiClientID", "CLIENT_ID")
    mimiClientSecret = getBuildProperty("mimiClientSecret", "CLIENT_SECRET")
    // Define MSDK version here
    msdkVer = "7.4.0"
}
```

Inside **app/build.gradle**

- Add `buildConfigField` under `defaultConfig`:  

```groovy
android {
    ...

    defaultConfig {
        ...

        buildConfigField("String", "MY_CLIENT_ID", rootProject.ext.mimiClientID)
        buildConfigField("String", "MY_CLIENT_SECRET", rootProject.ext.mimiClientSecret)
    }
}
```

- Add mimi sdk under `dependencies` section inside your app's `build.gradle` file:  

```groovy
dependencies {
    // Other dependencies here

    // For lifecycleScope
    def lifecycle_version = "2.5.1"
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"

    // Mimi SDK
    implementation "io.mimi:sdk:$rootProject.ext.msdkVer"
}
```

> **Sync/Make your project now so that all dependencies are downloaded and BuildConfig fields are generated**

## Init MSDK

Start `MimiCore` inside your custom Application class's `onCreate()`. For example, let's say your custom Application class is called `MyApp`. Then

```kotlin
import io.mimi.sdk.core.MimiConfiguration
import io.mimi.sdk.core.MimiCore

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeMimiCore(this)
    }

    private fun initializeMimiCore(context: Context) {
        MimiCore.start(
                context = context,
                configuration = MimiConfiguration(),
                clientId = BuildConfig.MY_CLIENT_ID,
                clientSecret = BuildConfig.MY_CLIENT_SECRET
        )
    }
}
```

!!! note
    The MSDK doesn’t have full API support for logging control yet, but there’s still a way to enable logging using Reflection, this isn’t supported as a API, so we recommend only use it when debugging and removing afterwards. Make sure you don't run it in production/proguarded code.
    First setup the function:
    ```kotlin
    private fun enableMimiSDKLogs() {
        io.mimi.sdk.core.util.Log::class.java.getDeclaredField("enabled")
                .apply {
                    isAccessible = true
                    setBoolean(io.mimi.sdk.core.util.Log(), true)
                }
    }
    ```
    Next, call this function just before initializing the MSDK (MimiCore.start() )
    ```kotlin
    private fun initializeMimiCore(context: Context) {
        enableMimiSDKLogs()
        MimiCore.start(...)
    }
    ```

## Wire in Application class

> Make sure `MyApp` is added to `application` tag inside `AndroidManifest.xml`
>
> ```xml
> <application
>     android:name=".MyApp" ...>
>
> ```

Add required permission declarations inside **AndroidManifest.xml**. 
> Skip if you already have them declared

```xml
<uses-permission android:name="android.permission.INTERNET" />
```
