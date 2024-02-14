# Java callout for JSON Check

This directory contains the Java source code required to compile a Java callout
for Apigee that does JSON checks. Currently there's one check: it checks for duplicate key names.

The source can be a string or a "Message".


## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.


## Using this policy

To use the policy you must have an API Proxy configured with the JAR included
here, as well as all of its dependencies, in the resources/jaava directory.

You do not need to build the source code in order to use the policy in Apigee,
or to download all the dependencies. The pre-built JAR and the dependencies are
include in this repo.

But if you _want_ to build the policy from source code, you can do so.
The instructions to do so are at the bottom of this README.


To use the jar and dependencies included in this repo:

* copy all of the jar files available in [the
  repo](bundle/apiproxy/resources/java/), to your apiproxy/resources/java
  directory. You can do this offline in your filesystem, or you can do it
  graphically using the Proxy Editor in the Apigee Edge Admin UI.

To use the jar and the downloaded dependencies, _first_ build the project (see
instructions below), then after you build it:

* copy the jar file, available in target/apigee-callout-json-check-20240213.jar , if
  you have built the jar, or in [the
  repo](bundle/apiproxy/resources/java/apigee-callout-json-check-20240213.jar) if
  you have not, to the apiproxy/resources/java directory for YOUR proxy. Also copy all the
  required dependencies. (See below) You can do this offline, or using the
  graphical Proxy Editor in the Apigee Admin Portal.


Then, in either case:

1. include a Java callout policy in your
   apiproxy/resources/policies directory. It should look
   like this:
   ```xml
   <JavaCallout name="Java-JSON-Check">
     <Properties>
       <Property name='source'>message.content</Property>
     </Properties>
     <ClassName>com.google.apigee.callouts.jsoncheck.JsonCheckCallout</ClassName>
     <ResourceURL>java://apigee-callout-json-check-20240213.jar</ResourceURL>
   </JavaCallout>
   ```

5. use the Apigee UI, or a command-line tool like [apigeecli](https://github.com/apigee/apigeecli) or similar to
   import the proxy into an Apigee organization, and then deploy the proxy .

6. use a client to generate and send http requests to invoke the proxy, like this:
   ```
   curl -i -X POST -H content-type:application/json $apigee/jsoncheck/t1 -d @content.json
   ```
   If the content is valid JSON and has no duplicate property names, the proxy will return OK.
   If there are duplicate property names, the proxy will return a fault.


## Usage Notes

There is one callout class, com.google.apigee.callouts.jsoncheck.JsonCheckCallout ,
which parses a string as JSON. It will throw a failt if the JSON is invalid or if it includes
a duplicate property name.


You can configure the callout with a Property element in the policy
configuration.

| Property             | Description                                                                                                      |
|----------------------|------------------------------------------------------------------------------------------------------------------|
| source               | optional. name of a string variable that contains json, or name of a Message that has a json payload. Defaults to `message`, which means to parse the `message.content` as JSON.     |


If the check fails, the callout will set the variable `jc.error` to a string explaining the problem.  For example, `duplicate key name`. 


## Building

Building from source requires Java 1.8, and Maven.

1. unpack (if you can read this, you've already done that).

2. Before building _the first time_, configure the build on your machine by loading the Apigee jars into your local cache:
  ```
  ./buildsetup.sh
  ```

3. Build with maven.
  ```
  mvn clean package
  ```
  This will build the jar and also run all the tests.


Pull requests are welcomed!


## Build Dependencies

- Apigee Edge expressions v1.0
- Apigee Edge message-flow v1.0
- glassfish javax.json library v1.1.4


## License

This material is Copyright (c) 2019-2024, Google LLC.  and is licensed under
the [Apache 2.0 License](LICENSE). This includes the Java code as well
as the API Proxy configuration.


## Support

This callout is open-source software, and is not a supported part of Apigee.
If you need assistance, you can try inquiring on
[The Apigee Community Site](https://www.googlecloudcommunity.com/gc/Apigee/bd-p/cloud-apigee).
There is no service-level guarantee for responses to inquiries regarding this callout.


## Bugs

* The tests are thin.
