How to build all java sources to generate a fresh version of Platypus.js
-------------------------------
Clone a git repository: https://github.com/altsoft/PlatypusJS.git

Checkout branch `master` if needed.

Folders structure
-------------------------------
- application  -> All Platypus.js runtime with source files and target folders structure.

  - bin                  -> Empty folder. It is a place for Platypus.js components' *.jar files compiled by Platypus.js build. 
  - lib                  -> Libraries used by Platypus.js runtime.

    - own             -> Empty folder. It is a place for Platypus.js libraries' *.jar files compiled by Platypus.js build.

    - thirdparty      -> Library jar files used by Platypus.js runtime. They are not compiled by the build.

  - src                  -> Java source of all Platypus.js parts, except browser client.

    - group          -> Contains build.xml - Ant build scripts with subprojects for all Platypus.js runtime. Also it holds a NetBeans ant project and can be built with NetBeans IDE.

    - lib	    -> Source of Platypus.js libraries, compiled during the build.

    - components	    -> Source of Platypus.js components, compiled during the build.

- designer                -> Platypus IDE NetBeans RCP application.

- hudsonci                -> Hudson continious integration server configuration for building Platypus.js with installation packs.

- installer               -> NBI(NetBeans installer) project for Platypus.js.

- web-client              -> Sources and assets of Platypus.js browser client.

  - src                 

    - platypus       -> Java Gwt sources. Also it contains Eclipse project and Ant build script (build.xml).

    - pwc_external        -> Css, images and fonts assets. 

To build Platypus.js, you need Ant, Java8 and Gwt 2.7.0 for Platypus.js browser client.

1. Cd to root Platypus.js download directory.
2. Type `ant "-Dlibs.javaee-web-api-7.0.classpath=/home/your-home-dir/apache-tomcat-8.0.30/lib/servlet-api.jar:/home/your-home-dir/apache-tomcat-8.0.30/lib/websocket-api.jar" -f ./application/src/group/build.xml clean compile` on the command line.
This will build Platypus.js components and libraries and will put theirs *.jar files to `./application/bin` and `./application/lib/own` folders respectively.
Note, that `libs.javaee-web-api-7.0.classpath` property points to JavaEE 7 Web profile libraries `servlet-api.jar`, and `websocket-api.jar`. It is not necessary, that they will be from Tomcat server. They may by taken from anywhere else.
Moreover, these libraries may be combined into single file (for example `javaee-web-api-7.0.jar` from NetBeans).
3. Type `ant -f ./designer/build.xml clean build` on the command line.
This will build Platypus IDE and will put it to `./designer/build` folder. Note - you may also need to add this two parameters -Dnbplatform.default.harness.dir=/home/your-home-dir/your-netbeans-dir/harness/ -Dnbplatform.default.netbeans.dest.dir=/home/your-home-dir/your-netbeans-dir/
4. Type `ant -Ddestdir=../../../application/bin -Dgwt.sdk=/home/your-home-dir/gwt-2.7.0 -f ./web-client/src/platypus/build.xml clean build copy-dest` on the command line.
This will build Platypus.js browser client and will put it to `./application/bin/pwc` folder.

Now you have got a fresh version of Platypus.js without installation packs.

To run Platypus IDE, type `ant -f ./designer/run.xml run` on the command line.
Note, that such version of Platypus IDE uses `./designer/build/testuserdir` folder as temporary user's profile directory and `clean` task of build script will erase it each time.
