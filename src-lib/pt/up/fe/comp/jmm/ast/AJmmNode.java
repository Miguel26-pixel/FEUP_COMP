/**
 * Copyright 2022 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

package pt.up.fe.comp.jmm.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.up.fe.specs.util.SpecsCheck;

public abstract class AJmmNode implements JmmNode {

    private final Map<String, String> attributes = new HashMap<>();

    protected Map<String, String> getAttributesMap() {
        return attributes;
    }

    @Override
    public String getKind() {
        return this.getClass().getSimpleName();
    }

    @Override
    public List<String> getAttributes() {
        return new ArrayList<>(attributes.keySet());
    }

    @Override
    public void put(String attribute, String value) {
        attributes.put(attribute, value);
    }

    @Override
    public String get(String attribute) {
        var value = this.attributes.get(attribute);

        SpecsCheck.checkNotNull(value, () -> "Node " + toString() + " does not contain attribute '" + attribute + "'");

        return value;
    }

}
