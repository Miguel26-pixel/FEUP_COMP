/**
 * Copyright 2021 SPeCS.
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

import java.util.function.BiFunction;

/**
 * Represents all visitors of JmmNodes.
 * 
 * @author JBispo
 *
 * @param <D>
 * @param <R>
 */
public interface JmmVisitor<D, R> {

    R visit(JmmNode jmmNode, D data);

    default R visit(JmmNode jmmNode) {
        return visit(jmmNode, null);
    }

    /**
     * Adds a visit for the node with the given kind name.
     * 
     * @param kind
     * @param method
     */
    void addVisit(String kind, BiFunction<JmmNode, D, R> method);

    /**
     * Overload that accepts any object as kind, and calls .toString() to determine the kind of node.
     * 
     * @param kind
     * @param method
     */
    default void addVisit(Object kind, BiFunction<JmmNode, D, R> method) {
        addVisit(kind.toString(), method);
    }

    void setDefaultVisit(BiFunction<JmmNode, D, R> method);
}
