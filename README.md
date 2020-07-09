# `PPLG` Purpose Parametric Log Generator
![gsim](https://bitbucket.org/proslabteam/PPLG/raw/f51fb2a83bd8787bb162b3131def27c2ed044854/src/main/webapp/images/arch.png)

This tool provides a novel log generation methodology that can be parametric on the input process model language and on the mining purpose, to produce artificial event logs. The methodology is meant to ensure the possibility of simulating any kind of process model through the implementation of several modeling language semantics (e.g., BPMN, Petri net, EPC, WF-net), and also the possibility to decide characteristics of the output event log according to the requirements of a mining procedure.


## Builds

Project build are availabe [here](https://bitbucket.org/proslabteam/guidedsimulator-builds).

## Building the project

### System requirements

* Java SE 13.x or later
* Nodejs 9.x or later
* npm 6.x or later
* Maven

### Local dependences

`mvn install:install-file -Dfile=lib/rapidprom-4.0.0.jar -DgroupId=org.rapidprom -DartifactId=rapidprom -Dversion=4.0.0 -Dpackaging=jar`

`mvn install:install-file -Dfile=lib/camunda-bpmn-model-7.11.0.jar -DgroupId=org.camunda.bpm.model -DartifactId=camunda-bpmn-model -Dversion=7.11.0MIDA -Dpackaging=jar`

`mvn install:install-file -Dfile=lib/camunda-xml-model-7.11.0.jar -DgroupId=org.camunda.bpm.model -DartifactId=camunda-xml-model -Dversion=7.11.0MIDA -Dpackaging=jar`

`mvn install:install-file -Dfile=lib/ProM-Framework.jar -DgroupId=org.processmining.framework -DartifactId=processmining-framework -Dversion=0.0.1 -Dpackaging=jar`

### Packaging

`mvn clean package -Pproduction`

### Run

`java -jar target/guidedsimulator-0.1.jar`

open [http://localhost:4300](http://localhost:4300)

### Logs location 

`logs/gsim.log`

### Custom configuration

To customise the final build (e.g. the port number) change the settings in

`/src/main/resources/application.properties`

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)
