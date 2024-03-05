<img src="docs/img/esg-logo.jpg" style="height:150;">

This project contains the public code for the [Easy Smart Grid](https://www.easysg.de/) technology that can integrate more (fluctuating) renewable energy. It creates a market platform where energy producers and consumers can exchange energy and flexibility. Easy Smart Grid promises a simple, robust and affordable solution to activate all Demand Side Management potential (i.e. low cost storage) in electric grids.


----------

# Gradle

This project uses the Gradle build automation tool, containing fully functional Gradle build files (``build.gradle``). The build process also allows an effortless Eclipse IDE integration.  
A provided Gradle wrapper (gradlew) in the root of the project, or an installed Gradle on the system, may be used:

Install Gradle (optionally, if not using the wrapper):

- Download the latest Gradle from the website: [https://gradle.org/](https://gradle.org/)
- Add bin directory of Gradle to the user/system PATH:
    - in Unix systems: add ``export PATH=$PATH:/home/<user>/<path>/<gradle-version>/bin`` to ``~/.bashrc``
    - in Windows follow these [instructions](https://msdn.microsoft.com/en-us/library/office/ee537574%28v=office.14%29.aspx)
- Gradle will automatically download the project dependencies from Maven Central. Therefore if you’re behind a proxy you should set the proxy options in the gradle.properties file as explained [here](http://www.gradle.org/docs/current/userguide/build_environment.html).

Install a Java JDK >= 8:

- This project uses at least OpenJDK 8 to compile. If several JDKs are installed on a system, the ``org.gradle.java.home`` property in the gradle.properties file may be set.

Create Eclipse project files using Gradle:

- Eclipse project files can be generated via the command ``gradle eclipse`` or ``./gradlew eclipse``
- After generating the Eclipse project files, they can be imported into a Eclipse workspace
- It is important to add the ``GRADLE_USER_HOME`` variable in Eclipse: Window->Preferences->Java->Build Path->Classpath Variable. Set it to the path of the ``~/.gradle`` folder in your home directory (e.g. ``/home/<user_name>/.gradle/`` (Unix) or ``C:\Users\<user_name>\.gradle\`` (Windows))


----------

# Contact

This project is maintained by:

![ISC logo](docs/img/isc-logo.png)

- **[ISC Konstanz](http://isc-konstanz.de/)** (International Solar Energy Research Center)
- **Adrian Minde**: adrian.minde@isc-konstanz.de
