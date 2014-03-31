rsp-services-api
================

Java API to access rsp-services implementation for C-SPARQL Engine

Online Resources
------------

You can find a usage example of the API in the java client example project at https://github.com/streamreasoning/rsp-services-client-example

Javadoc
------------

http://streamreasoning.org/documentation/javadoc/rsp-services-csparql-api/

Maven
------------

Add new maven repository to pom file

<repositories>
	....
	<repository>
		<id>streamreasoning_repository</id>
		<name>streamreasoning repository</name>
		<url>http://streamreasoning.org/maven/</url>
		<layout>default</layout>
	</repository>
	...
</repositories>

Dependecy
<dependencies>
	...
	<dependency>
		<groupId>polimi.deib</groupId>
		<artifactId>rsp-services-api</artifactId>
		<version>latest_version_available</version>
	</dependency>
	...
</dependencies>
