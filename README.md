# Jmm Compiler

## Group elements

### Group 9B

| Name              | Number    | Self-evaluation | Contribution | Performed work                                                                            |
|-------------------|-----------|-----------------|--------------|-------------------------------------------------------------------------------------------|
| **Bruno Mendes**  | 201906166 | 19              | 28%          | Grammar, symbol table generation, `ollir` generation, constant propagation optimization   |
| **David Preda**   | 201904726 | 19              | 28%          | Grammar, symbol table generation, `jasmin` generation, instruction selection optimization |
| **Fernando Rego** | 201905951 | 19              | 28%          | Grammar, symbol table generation, semantic analysis                                       |
| **Miguel Amorim** | 201907756 | 16              | 16%          | Grammar, register allocation optimization                                                 |

#### Global self-evaluation of the project: 19

## Summary
Our compiler takes `jmm` code, a subset of the `Java` language, and outputs `ollir`, an in-house intermediate code representation, and `jasmin`, a `JVM` stack-based language which can be run directly by the `JRE`.

The frontend parses the `jmm` code to an abstract syntax tree, properly annotated for the later phases to depend on, and alerts for syntax errors, with respective line and column information to guide the end user, and `ollir` and `jasmin` reserved names properly escaped to avoid confusion in the upcoming stages.

The backend is thoroughly implemented using the visitor pattern on top of the AST nodes.
First, a symbol table, containing types, names and declarations, is generated for the code.
Then, the semantic analysis depends on it to warn the user about a variety of semantic errors such as type mismatches, duplicate method declarations or invalid field access in static methods.
After this, we perform constant propagation optimization, generating a cleaner AST without pointless var declarations and assignments for the `ollir` generation.
As soon as the `ollir` is generated, register allocation is optimized.
Finally, `jasmin` code, with carefully selected instructions, is generated for direct use in the `JVM`.

## Semantic analysis

The semantic analysis phase of the compiler is where the program ensure that all declarations and statements are semantically correct. This process uses the syntax tree (AST) to build a symbol table and use both to do all the necessary verifications.

### Symbol table generation

The symbol table is composed by a `JmmClassSignature` that stores the class information (class name, super class name, fields), and two sub-tables, the `ImportsTable` and the `MethodsTable`. The `ImportsTable` stores information about every import that the file contain and the `MethodsTable` saves all the content of each method of the class, the method signature and all the local variables.  

To generate the symbol table we visit all nodes of the syntax tree to obtain all the data needed for the table. Therefore, four visitors were implemented:

| Visitor                    | Explanation                                                                                                                                                                                                                     |
|----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `ImportDeclarationVisitor` | Visit every node of the kind `ImportDeclaration` at the AST and build the `ImportsTable`                                                                                                                                        |
| `ClassDeclarationVisitor`  | Visit the `ClassDeclaration` node to get the main data from the class and visit all the nodes of the kind `VarDeclaration` to get the fields of the class. The retrieved information allow the build of the `JmmClassSignature` |
| `MethodDeclarationVisitor` | If exists, visit the `MainMethod` and every node of kind `RegularMethod` to obtain all the information of each method of the class, which are saved into the `MethodsTable`                                                     |
| `LocalVariablesVisitor`    | This visitor adds to the method table the information of all local variables by visiting all nodes of kind `VarDeclaration` inside any method                                                                                   |

As an addition, at this level, we ensure that there are no duplicate method definitions (since overloading is not supported), and that the file name matches the class name (if `inputFile` is provided in the configuration), as required by the Java specification for public classes.

### Semantic tests

After building the symbol table and together with the syntax tree (AST), all tests and verifications to all declarations and statements in the code are performed. The list of every semantic rule is divided in the following four major classes:

- `ImportCheckVisitor`
  - Verifies if all types of all declared variables associated with an outer class are imported, with exception if it refeers to the superclass that is extended

- `MethodCallVisitor`
  - If the class does not extend another class and a function call is made by an object of the class, checks if the method exists.

- `ArrayAccessVisitor`
  - Checks if every array access is done over an array 

- `TypeCheckVisitor`
  - Checks if there is no `ThisLiteral` in the `MainMethod` because the main method is static
  - Checks if in the `MainMethod` there is no access to the class fields
  - Checks if every `Identifier` associated to a method or a variable exists. In case it does not exist, it is checked if it corresponds to any import declaration or to the class extended
  - Checks if the argument in every `UnaryOp` (not operation) is of type boolean
  - Verifies if the types of the two elements in every `BinaryOp` is compatible with the operation:
    - `assign`: type of the assignee must be compatible with the assigned
    - `and`, `or`: both elements must be of the type `boolean`
    - `add`, `sub`, `mul`, `div`, `lt`: both elements must be of the type `int`
  - Checks if every array access index is an expression of type `int`
  - Checks if the field of a class exists when access to an attribute is made. In case it does not exist, the class must extend another class
  - Verifies that all expressions in conditions must return a boolean, either for `If` statement or `While` loop
  - Checks if the return type of every method is the same compared to the given return expression
  - Verifies that for every function call, the number of arguments is correct
  - Checks if the arguments type of every function call is correct
  - For every `VarDeclaration`, checks if the type is valid

> The `TypeCheckVisitor` is where most of the magic happens. It runs through all the nodes of the syntax tree visiting everyone with an visitor associated to each node kind making the necessary verifications. The implementation is assuming that every outer method call that belongs to the super class or to one of the imports' declaration is correct and returns null leaving the handle task to the parent's visitor.

## Code generation

With the semantic analysis concluded, the compiler now moves to the code generation phase. This stage can be subdivided into two major steps: the first one transforms the AST into `ollir`, changing the initial instructions to three address operations, while the second one converts `ollir` to `jasmin`, which will enable the final conversion to bytecodes. Optimizations happen at this stage, too.

The following subsections cover in a more deeply manner the code generation phases and optimizations.

### Optimizations at the AST level

Before the `ollir` generation is started, we perform constant propagation at the semantically correct AST level.
While visiting method local variables, the right-hand sides of constant assigns are saved, so that they may replace the identifier references later in the code.
As soon as the original variable is reassigned to a non-constant value, the variable is no longer flagged for optimization and later usages are not changed.

> While this seems trivial to do in a preorder visit of the tree, loops present a higher challenge, since the condition is evaluated before the body, and the seemingly constant loop control variable is actually not constant if the loop body is analysed. This is worked around by visiting, in case of loops, the body before the exit condition.

Before exiting the method visit (and before clearing flagged constants, to avoid name clashes in later methods), if a variable is known to have been constant throughout all the method body, its declaration (and assignments) are removed from the AST, further improving the `ollir` and thus the `jasmin` codes.

### `Ollir` generation

`ollir` is an intermediate code representation based on a three address operation fashion, which facilitates the `jasmin` code generation, given it is a stack-based language.

The `OllirEmitter` class is responsible for generating `ollir` code given the `jmm` abstract syntax tree and the symbol table generated at the semantic analysis step. The latter is useful, for example, for translating all class imports and method headers, without the need for consulting the AST.

The major challenge of this step is extracting complex nested code expressions into temporary variables to allow three-address code. This is done passing to the inner expression visitor a `SubstituteVariable`, a class responsible for holding the name, value and type of the visited expression. Its visitor is then responsible to inject the necessary code before the control flow returns to the parent's visitor.

> A method call such as *a.bar(2+3)* has to become something (in pseudo-code) like *temp = 2+3; a.bar(temp)*.

While visiting any kind of expression, a temporary variable is created to hold its value. However, there is no need to always use the holder: an identifier can be used in place.

> There is no need to translate `a = b` to `temp = b; a = temp;`. A simple registry allocation optimization in the `visitIdentifier` method changes the value of the temporary variable to the actual identifier name and does not inject any code. The `ollir` thus becomes simply `a = b`.

Since the methods' local variables' liveliness cannot interfere, the variable temporary counter, used for its name, is reset for each method.
The name is also escaped if it exists in the symbol table, to prevent name clashes.

> The second compiled method can reuse the `t0` name for its first temporary variable, or `t0_`, if that method's local variables include a `t0`.

An interesting challenge is the deduction of external `invoke` method call types (the internal ones are easier, since they are described in the symbol table). Since we have no access to the imported classes, we must trust the type matches at this level: the assigned variable type is passed to the right hand side expression visitor through the `assignType` field of the `SubstituteVariable` class.

> A `jmm` code like `Imported obj; obj.bar(...)` becomes `invoke(...).V`, while `Imported obj; int a; a = obj.bar(...)` becomes `a.i32 :=.i32 invoke(...).i32`.

It is thus quite difficult to infer the types of complex, external nested method calls.

> Even though the teachers' tests do not provide such a complex example for external types, the grammar accepts statements such as `ret = obj.foo().bar()`. There is no way of knowing which type `obj.foo()` is, so this is likely to produce an incorrect `ollir` translation.

In terms of code organization, we feel that the `OllirEmitter` class has become quite large; we could have split its responsibilities in more files, which would raise state management challenges, but greatly improve the legibility.

### Optimizations at the `ollir` level

#### Register Allocation

After generating the OllirResult, if the "-r" option is used, the *register allocation optimization* is performed to each method.

First, the algorithm starts by using our implementation of the algorithm *liveliness analysis* which uses *dataflow analysis* to determine the lifetime of local variables. After that, it constructs the *Interference Graph*, a HashMap where it's saved all variables and for each variable, a set of variables representing all the others variables that have an intersecting lifetimes.
Unfortunately, there is some error we couldn't fix, so the algorithm is not at the 100%.

Then, uses *Graph Coloring* to allocate registers and builds a new varTable for the method, updating each variable's virtual register.

If the specified number of registers isn't enough to store all the variables, the program aborts and reports an error, showing the minimum number of registers needed.

If the specified number of registers is zero, then the program uses the number of registers needed to finish the allocation.

Unfortunately, the algorithm does not work correctly all the times. There are some issues regarding the calculation of the life-time of the variables, on set of out variables and in variables.

### `Jasmin` generation

`jasmin` is an assembler for the JVM. Therefore, in order for this compiler to generate the required bytecodes, the `ollir` instructions must be translated to `jasmin` instructions.

The `JmmBackend` class is responsible for handling the full translation of a `.ollir` file into a `.j` file. It starts by the class and super directives, defaulting to `Object` as a super class if none is given. The fields are also translated by this class.

> If no access modifier is provided, `JmmBacked` will default a class field to private, in order to maximize security.

The `MethodDefinitionGenerator` class takes a `Method` object and translates it into a `jasmin` method definition. It is responsible for generating the method header, which includes its descriptor, and for calculating the `.limit stack` and `.limit locals` values (the latter with help from the `Instruction Translator` class).

> The `.limit stack` value is determined by calculating the maximum amount of contiguous loads. On the other hand, `.limit locals` value is determined by taking the maximum register value in use by the method's var table and adding one to it (to take 0 into account).

However, the individual instruction is delegated to the `InstructionTranslator` class. This class is responsible translating every type of instruction and ensuring that the necessary loads and stores are performed. Furthermore, it is also its task to generate the necessary labels to handle `if` and `goto` instructions.

The instruction translation was optimized through the usage of more efficient `jasmin` instructions. Accesses and storages for registers 5 and below use the respective `iload_` and `istore_` instructions. Furthermore, constants smaller than 6 use the `iconst_` family of instructions whenever they are pushed to the stack, while byte-sized numbers use `bipush` and short-sized numbers use `sipush`. Other, larger, numbers use `ldc`. The addition operations are also made more efficient when one of the operands is a literal value, through the usage of the `iinc` instruction. 

>Assuming that t1 is stored in register 2, both `t1 + 10` and `10 + t1` would be converted to the instruction `iinc 2 10`.

Finally, less than comparisons are optimized whenever 0 is on the right side, through the usage of the `iflt` instruction.

Even though these classes behave as expected, we feel that we could have created a proper class-based framework for the translation of instructions instead of a direct instruction-to-string translation. This would have enabled us to have a better codebase, but time constraints did not allow us to implement this. 

## Best features
In terms of the base language, we added support for the `||` ("or") operator and for local variable declarations after statements (the original grammar forced all declarations to appear at the top of the method body).

At the semantic level, we added two extra checks, detailed above, related with method duplication detection and class name mismatch.

In terms of the code architecture, we are confident that our pipeline is simple and robust, capable of detecting edge case errors and generating code for very complex expressions, in correct order. We built a strong base which can be painlessly used to support some modern `Java` syntax sugars.

> While supporting other operators such as `>`, `!=`, or `==` would be a breeze, an addition like assignments in declarations would not be so difficult, too.
 
While we implemented the required register allocation and constant propagation optimizations, we would like to point out the bigger feature set of the latter, given that it removes some dead code, as explained above.

As a whole, we were able to run in the JVM all `jmm` code samples, including complex ones such as *TicTacToe* or *Life*.
It was interesting to see that *Life.jmm* contained a field named *field*, which initially raised confusion at the Jasmin level, and proved the need for our AST disambiguation traversal.
Also, the `WhileAndIf` sample provided contained a public class named `WhileAndIF`, which is not acceptable for the JVM, a small detail we also put effort to look into.

## Possible improvements
Beside the already mentioned large size of some important classes, and the lack of a bigger optimization pipeline, we feel that our work could be much improved if we added more features to the base `jmm` language, such as method overloading, string literals or class constructor customizations.

## Compile and Running

To compile and install the program, run ``gradle installDist``. This will compile your classes and create a launcher script in the folder ``./build/install/comp2022-9b/bin``. For convenience, there are two script files, one for Windows (``comp2022-9b.bat``) and another for Linux (``comp2022-9b``), in the root folder, that call tihs launcher script.

After compilation, a series of tests will be automatically executed. The build will stop if any test fails. Whenever you want to ignore the tests and build the program anyway, you can call Gradle with the flag ``-x test``.

## Test

To test the program, run ``gradle test``. This will execute the build, and run the JUnit tests in the ``test`` folder. If you want to see output printed during the tests, use the flag ``-i`` (i.e., ``gradle test -i``).
You can also see a test report by opening ``./build/reports/tests/test/index.html``.
