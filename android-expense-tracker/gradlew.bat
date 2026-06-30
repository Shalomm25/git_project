@rem Gradle startup script for Windows. If gradle-wrapper.jar is missing,
@rem run `gradle wrapper --gradle-version 8.10.2` once to regenerate it.
@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%CLASSPATH%" (
    echo gradle-wrapper.jar not found. Run "gradle wrapper --gradle-version 8.10.2" once to generate it. 1>&2
    exit /b 1
)

if defined JAVA_HOME (set JAVA_EXE=%JAVA_HOME%\bin\java.exe) else (set JAVA_EXE=java.exe)

"%JAVA_EXE%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
