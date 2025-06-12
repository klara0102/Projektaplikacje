// Plik build na najwyższym poziomie, w którym możesz dodać opcje konfiguracyjne wspólne dla wszystkich podprojektów/modułów.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
   // classpath("com.google.gms:google-services:4.4.1")
    id("com.google.gms.google-services") version "4.4.1" apply false
}