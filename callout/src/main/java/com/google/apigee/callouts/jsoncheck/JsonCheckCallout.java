// JsonCheckCallout.java
//
// A callout for Apigee that performs one or more checks on
// a JSON payload.
//
// Copyright 2019-2024 Google LLC.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//
// Example configuration
//
// <JavaCallout name='Java-JsonCheck'>
//   <Properties>
//     <!--
//        The source of the JSON to be checked.
//        If you want to check Message content, then you can use
//        request.content.  Defaults to message.content.
//     -->
//     <Property name='source'>name-of-variable-containing-json</Property>
//   </Properties>
//   <ClassName>com.google.apigee.callouts.jsoncheck.JsonCheckCallout</ClassName>
//   <ResourceURL>java://apigee-callout-json-check-20240213.jar</ResourceURL>
// </JavaCallout>
//
// ----------------------------------------------------------
//
// This software is licensed under the Apache Source license 2.0.
// See the accompanying LICENSE file.
//
//

package com.google.apigee.callouts.jsoncheck;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.callouts.CalloutBase;
import java.io.ByteArrayInputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

public class JsonCheckCallout extends CalloutBase implements Execution {
  private static final String varPrefix = "jc.";
  private static final JsonParserFactory factory = Json.createParserFactory(null);

  public JsonCheckCallout(Map properties) {
    super(properties);
  }

  public String getVarnamePrefix() {
    return varPrefix;
  }

  private String getSource() {
    String source = (String) this.properties.get("source");
    if (source == null || source.equals("")) {
      return "message";
    }
    return source; // should be the name of a message
  }

  public ExecutionResult execute(MessageContext msgCtxt, ExecutionContext exeCtxt) {
    ExecutionResult calloutResult = ExecutionResult.ABORT;
    Boolean isValid = false;
    boolean debug = getDebug();

    try {
      clearVariables(msgCtxt);
      Object untypedSource = msgCtxt.getVariable(getSource());
      String jsonString = null;
      if (untypedSource.getClass().getName().equals("java.lang.String")) {
        jsonString = (String) untypedSource;
      } else if (untypedSource instanceof Message) {
        Message source = (Message) untypedSource;
        jsonString = source.getContent();
      } else {
        throw new IllegalStateException("unsupported source");
      }

      JsonParser jsonParser = factory.createParser(new ByteArrayInputStream(jsonString.getBytes()));
      Deque<Set<String>> stack = new ArrayDeque<Set<String>>();
      Set<String> keynames = new HashSet<String>();
      while (jsonParser.hasNext()) {
        JsonParser.Event event = jsonParser.next();
        if (event.equals(javax.json.stream.JsonParser.Event.START_OBJECT)) {
          stack.push(keynames);
          keynames = new HashSet<String>();
        } else if (event.equals(javax.json.stream.JsonParser.Event.END_OBJECT)) {
          keynames = stack.pop();
        } else if (event.equals(javax.json.stream.JsonParser.Event.KEY_NAME)) {
          String name = jsonParser.getString();
          if (keynames.contains(name)) {
            throw new IllegalStateException("duplicate key name");
          }
          keynames.add(name);
        }
      }
      msgCtxt.setVariable(varName("result"), true);
    } catch (javax.json.stream.JsonParsingException jsone) {
      msgCtxt.setVariable(varName("exception"), jsone.toString());
      msgCtxt.setVariable(varName("error"), "invalid json");
      return ExecutionResult.ABORT;
    } catch (Exception e) {
      if (debug) {
        // e.printStackTrace();
        String stacktrace = getStackTraceAsString(e);
        msgCtxt.setVariable(varName("stacktrace"), stacktrace);
      }
      setExceptionVariables(e, msgCtxt);
      return ExecutionResult.ABORT;
    }

    return ExecutionResult.SUCCESS;
  }
}
