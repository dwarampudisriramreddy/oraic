import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.tasks.JavaExec
import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinxSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    js {
        browser()
        binaries.executable()
    }

    // wasmJs removed - SQLDelight doesn't support it
    // @OptIn(ExperimentalWasmDsl::class)
    // wasmJs {
    //     browser()
    //     binaries.executable()
    // }

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.android)
            // AndroidSVG for SVG to bitmap conversion - lightweight and Android-specific
            implementation("com.caverock:androidsvg-aar:1.4")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(compose.materialIconsExtended)
        }
        androidMain.dependencies {
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.sqlite)
            // SQLite JDBC driver - required for JdbcSqliteDriver
            implementation("org.xerial:sqlite-jdbc:3.44.1.0")
            // Apache Batik for SVG rendering
            implementation("org.apache.xmlgraphics:batik-swing:1.17")
            implementation("org.apache.xmlgraphics:batik-svg-dom:1.17")
            implementation("org.apache.xmlgraphics:batik-dom:1.17")
            implementation("org.apache.xmlgraphics:batik-bridge:1.17")
            implementation("org.apache.xmlgraphics:batik-awt-util:1.17")
            implementation("org.apache.xmlgraphics:batik-codec:1.17")
            implementation("org.apache.xmlgraphics:batik-transcoder:1.17")
            // iText for PDF generation
            implementation("com.itextpdf:itextpdf:5.5.13.3")
        }
        
        iosMain.dependencies {
            implementation(libs.sqldelight.native)
        }
    }
}

android {
    namespace = "com.ram.orai.oraic"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.orai.oraixcs"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "license/**"
            pickFirsts += "license/LICENSE.dom-documentation.txt"
            pickFirsts += "license/NOTICE"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.ram.orai.oraic.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ORAIC"
            packageVersion = "1.0.0"
            description = "ORAIC - Dental Case Management System"
            vendor = "Ram Orai"
            
            windows {
                menuGroup = "ORAIC"
                upgradeUuid = "18159995-d967-4cd2-8885-77BFA97CFA9F"
                menu = true
                // Desktop shortcut should be created automatically by jpackage
            }
            
            // Ensure all dependencies are included in the distribution
            includeAllModules = true
        }
    }
}

sqldelight {
    databases {
        create("OraicDatabase") {
            packageName.set("com.ram.orai.oraic.database")
            generateAsync.set(false)
        }
    }
}

