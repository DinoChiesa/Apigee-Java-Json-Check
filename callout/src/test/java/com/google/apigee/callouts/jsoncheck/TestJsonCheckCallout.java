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

package com.google.apigee.callouts.jsoncheck;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import mockit.Mock;
import mockit.MockUp;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestJsonCheckCallout {
  private static final String TEST_DATA_DIR = "src/test/resources/test-data";

  MessageContext msgCtxt;
  String messageContent;
  Message message;
  ExecutionContext exeCtxt;

  @BeforeMethod()
  public void beforeMethod() {

    msgCtxt =
        new MockUp<MessageContext>() {
          private Map<String, Object> variables;

          public void $init() {
            variables = new HashMap<String, Object>();
          }

          @Mock()
          @SuppressWarnings("unchecked")
          public <T> T getVariable(final String name) {
            if (variables == null) {
              variables = new HashMap<String, Object>();
            }
            T value = (T) variables.get(name);
            System.out.printf(
                "getVariable(%s) ==> %s\n", name, (value != null) ? value.toString() : "null");
            return value;
          }

          @Mock()
          @SuppressWarnings("unchecked")
          public boolean setVariable(final String name, final Object value) {
            if (variables == null) {
              variables = new HashMap<String, Object>();
            }
            System.out.printf("setVariable(%s, %s)\n", name, value.toString());
            variables.put(name, value);
            return true;
          }

          @Mock()
          public boolean removeVariable(final String name) {
            if (variables == null) {
              variables = new HashMap<String, Object>();
            }
            if (variables.containsKey(name)) {
              variables.remove(name);
            }
            return true;
          }

          @Mock()
          public Message getMessage() {
            return message;
          }
        }.getMockInstance();

    exeCtxt = new MockUp<ExecutionContext>() {}.getMockInstance();

    message =
        new MockUp<Message>() {
          @Mock()
          public String getContent() {
            System.out.printf("message.getContent()=> %s\n", messageContent);
            return messageContent;
          }
          // @Mock()
          // public InputStream getContentAsStream() {
          //   // new ByteArrayInputStream(messageContent.getBytes(StandardCharsets.UTF_8));
          //   return messageContentStream;
          // }
        }.getMockInstance();
  }

  @DataProvider(name = "batch1")
  public static Object[][] getDataForBatch1() throws IOException, IllegalStateException {
    return TestReader.getTests(TEST_DATA_DIR);
  }

  @Test
  public void testDataProviders() throws IOException {
    Assert.assertTrue(getDataForBatch1().length > 0);
  }

  @Test(dataProvider = "batch1")
  public void test2_Configs(TestCase tc) throws Exception {
    if (tc.getDescription() != null)
      System.out.printf("  %10s - %s\n", tc.getTestName(), tc.getDescription());
    else System.out.printf("  %10s\n", tc.getTestName());

    messageContent = tc.getSourceAsString();
    msgCtxt.setVariable("message", message);

    JsonCheckCallout callout = new JsonCheckCallout(tc.getProperties());

    // execute and retrieve output
    ExecutionResult actualResult = callout.execute(msgCtxt, exeCtxt);

    String s = (String) (tc.getExpected().get("success"));
    ExecutionResult expectedResult =
        (s != null && s.toLowerCase().equals("true"))
            ? ExecutionResult.SUCCESS
            : ExecutionResult.ABORT;
    // check result and output
    if (expectedResult == actualResult) {
      if (expectedResult == ExecutionResult.SUCCESS) {
        Assert.assertEquals(actualResult, expectedResult, tc.getTestName() + " output");
      } else {
        String expectedError = (String) (tc.getExpected().get("error"));
        Assert.assertNotNull(expectedError, "broken test: no expected error specified");
        String actualError = msgCtxt.getVariable("jc.error");
        Assert.assertEquals(actualError, expectedError, tc.getTestName() + " error");
      }
    } else {
      Assert.assertEquals(actualResult, expectedResult, "result not as expected");
    }
    System.out.println("=========================================================");
  }
}
