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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class TestCase implements Comparable {

  private String _parentDir;
  private String _testName;
  private String _description;
  private String _sourcefile; // name of file containing JSON hash
  private HashMap<String, String> _properties; // JSON hash
  private HashMap<String, Object> _expected; // JSON hash
  private HashMap<String, String> _context; // JSON hash

  // getters
  public String getTestName() {
    return _testName;
  }

  public String getDescription() {
    return _description;
  }

  public String getSourcefile() {
    return _sourcefile;
  }

  public HashMap<String, String> getProperties() {
    return _properties;
  }

  public HashMap<String, String> getContext() {
    return _context;
  }

  public HashMap<String, Object> getExpected() {
    return _expected;
  }

  public void init(String parentDir, String filename) {
    setTestName(filename.substring(0, filename.length() - 5));
    _parentDir = parentDir;
  }

  private static String readFileAsUtf8String(String parent, String fname) throws IOException {
    List<String> linelist = Files.readAllLines(Paths.get(parent, fname), StandardCharsets.UTF_8);
    String fileContent = String.join("\n", linelist).trim();
    return fileContent;
  }

  public String getSourceAsString() throws Exception {
    try {
      return readFileAsUtf8String(_parentDir, _sourcefile);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  // setters
  public void setTestName(String n) {
    _testName = n;
  }

  public void setDescription(String d) {
    _description = d;
  }

  public void setSourcefile(String filename) {
    _sourcefile = filename;
  }

  public void setExpected(HashMap<String, Object> hash) {
    _expected = hash;
  }

  public void setContext(HashMap<String, String> hash) {
    _context = hash;
  }

  public void setProperties(HashMap<String, String> hash) {
    _properties = hash;
  }

  @Override
  public int compareTo(Object tc) {
    return getTestName().compareTo(((TestCase) tc).getTestName());
  }
}
