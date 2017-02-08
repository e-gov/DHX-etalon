![Riigi Infosüsteemi Ameti](https://avatars3.githubusercontent.com/u/7447915 "Riigi Infosüsteemi Amet") ![](img/EL_Regionaalarengu_Fond_horisontaalne.jpg)

# DHX etalonteostus / DHX reference implementation

## ET

Demonstraator (töötav mudel, näidisimplementatsioon) dokumendivahetusprotokolli [DHX](https://github.com/e-gov/DHX) headuse tõestamiseks.

#### Viited

* [DHX protokolli etalonteostuse avaleht](https://dhxdemo.eesti.ee/)
* [DHX protokolli etalonteostuse kasutusjuhend ](https://github.com/e-gov/DHX-etalon/blob/master/files/kasutusjuhend.md)
* [DHX protokolli etalonteostuse testilood ](https://github.com/e-gov/DHX-etalon/blob/master/files/testlood.md)
* [DHX protokolli etalonteostuse spetsifikatsioon ](https://github.com/e-gov/DHX-etalon/blob/master/files/spekk.md)

####Projketi konfigureerimine
* Selleks et projekti ehitada ja käivitada on esiteks vaja üle vaadata ja vajadusel muuta failid **/src/main/resources/conf/#profile-name#** kaustas(kus #profile-name on maven profiili nimi mida kasutate projekti ehitamiseks ja käivitamiseks).
* Erilist tähelepanu vajab konfiguratsiooni fail mis asub siin:
**/src/main/resources/conf/#profile-name#/ws/application.properties**.
Järgmised application.properties faili parameetrid on keskkonnast sõltuvad ja suure tõnäosusega vajavad muutmist: 

  *soap.security-server=http://10.0.13.198*
  
  *soap.xroad-instance=ee-dev*
  
  *soap.member-class=GOV*
  
  *soap.user-id=38605150320*
  
  *soap.member-code=40000001*


####Projekti ehitamine
Projektis on 3 eeldefineeritud maven profiili pom.xml failis(põhierinevused profiilide vahel on konfiguratsiooni failide kausta asukoht ja tomcat serveri parameetrid):

* **development**(default) - Profiil arendamiseks. Tomcati parameetrid on puudu.     
  Maveni käsud selle profiili kasutamiseks(käivitada juur kataloogis):     
  * rakenduse ehitamine(war fail on väljund):
  `mvn clean package`
  * rakenduse käivitamine(rakendus käivitatakse embedded(sisse ehitatud) tomcat serveris):
 `spring-boot:run -pl dhx-adapter-client`
 
* **etalon1** - Profiil etalon1 keskkona jaoks.     
  Maveni käsud selle profiili kasutamiseks(käivitada juur kataloogis):     
    
  * rakenduse ehitamine(war fail on väljund):
  `mvn clean package "-Denv=etalon1"`
  
  * rakenduse käivitamine(rakendus käivitatakse embedded(sisse ehitatud) tomcat serveris):   
  `tomcat7:undeploy  tomcat7:deploy "-Denv=etalon1"`
  
* **etalon2** - Profile for dhs-maket2. Tomcat options are provided, meant to run with external tomcat.      
  Maveni käsud selle profiili kasutamiseks(käivitada juur kataloogis):     

  * rakenduse ehitamine(war fail on väljund):
  `mvn clean package "-Denv=etalon2"`
    
  * rakenduse käivitamine(rakendus käivitatakse embedded(sisse ehitatud) tomcat serveris):
  `tomcat7:undeploy  tomcat7:deploy "-Denv=etalon2"`

## EN

A reference implementation of [DHX document exchange protocol](https://github.com/e-gov/DHX). Reference implementation is used to verify integrity and feasibility of the DHX protocol and to demonstrate protocol operation. Code can also be useful for implementation

####Configuring the project
* In order to build and run DHX application all files located in **/src/main/resources/conf/#profile-name#** folder need to be reviewed and changed if needed(where #profile-name# is the name of the profile you are willing to use to build and run application).
* Special attention needs to be turned to configuration file that is located here: **/src/main/resources/conf/#profile-name#/ws/application.properties**.
Following parameters of the application.properties file are environment dependent and most likely need to be changed to build and run application: 

  *soap.security-server=http://10.0.13.198*
  
  *soap.xroad-instance=ee-dev*
  
  *soap.member-class=GOV*
  
  *soap.user-id=38605150320*
  
  *soap.member-code=40000001*
  


####Building the project
There are 3 predefined profiles provided in projects pom.xml(main differences between profiles are folder where configuration files are stored and tomcat options):

* **development**(default) - Profile for development. No tomcat options are provided.      
  Maven commands to use with that profile(run from projects root folder):
  - build application(war file is an output):
  `mvn clean package`
  
  - run application(application will be deployed in embedded tomcat server):
 `spring-boot:run -pl dhx-adapter-client`
 
* **etalon1** - Profile for etalon1 environment.     
  Maven commands to use with that profile(run from projects root folder):
    
  * build application(war file is an output):
  `mvn clean package "-Denv=etalon1"`
  
  * run application(packaged war file will be deployed to preconfigured tomcat server):
    
  `tomcat7:undeploy  tomcat7:deploy "-Denv=etalon1"`
  
* **etalon2** - Profile for dhs-maket2. Tomcat options are provided, meant to run with external tomcat.      
  Maven commands to use with that profile(run from projects root folder):

  * build application(war file is an output):
  `mvn clean package`
    
  * run application(packaged war file will be deployed to configured tomcat server):
  `tomcat7:undeploy  tomcat7:deploy "-Denv=etalon2"`


