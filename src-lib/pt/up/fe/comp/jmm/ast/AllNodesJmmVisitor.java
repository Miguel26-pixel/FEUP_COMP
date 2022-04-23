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

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A visitor that always visits all nodes.
 * 
 * @author Joao Bispo
 *
 * @param <D>
 * @param <R>
 */
public abstract class AllNodesJmmVisitor<D, R> extends AJmmVisitor<D, R> {

    private BiFunction<R, List<R>, R> reduce;

    /**
     * 
     * @param reduce
     *            a reduce function, which returns a result based on the result of the current node and the results of
     *            its children
     */
    public AllNodesJmmVisitor() {
        this.reduce = null;
    }

    /**
     * Sets the reduction function based only on how to merge two results, without information about their origin.
     * 
     * @param reduce
     */
    public void setReduceSimple(BiFunction<R, R, R> reduce) {
        setReduce(buildReduce(reduce));
    }

    /**
     * Merges all children results, and finally, the node result.
     * 
     * @param simpleReduce
     * @return
     */
    private BiFunction<R, List<R>, R> buildReduce(BiFunction<R, R, R> simpleReduce) {

        return (nodeResult, childrenResults) -> {
            // If no children, simply return node result
            if (childrenResults.isEmpty()) {
                return nodeResult;
            }

            // Merge each children results
            R currentResult = childrenResults.get(0);

            for (int i = 1; i < childrenResults.size(); i++) {
                currentResult = simpleReduce.apply(currentResult, childrenResults.get(i));
            }

            // Merge with node result
            return simpleReduce.apply(currentResult, nodeResult);
        };

    }

    /**
     * Sets the reduction function, which returns a result based on the result of the current node and the results of
     * its children.
     * 
     * @param reduce
     */
    public void setReduce(BiFunction<R, List<R>, R> reduce) {
        this.reduce = reduce;
    }

    /**
     * 
     * @return the reduce function currently set, which returns a result based on the result of the current node and the
     *         results of its children
     */
    protected BiFunction<R, List<R>, R> getReduce() {
        return reduce;
    }

    /**
     * Sets the default return value when a node is visited that has no function associated.
     * 
     * @param defaultValue
     */
    public void setDefaultValue(Supplier<R> defaultValue) {
        // When setting a default value, the default visit simple returns the default value
        setDefaultVisit((node, data) -> defaultValue.get());
    }

}
