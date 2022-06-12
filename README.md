# Jmm Compiler

## Group elements

### Group 9B

| Name | Number | Self-evaluation | Contribution |
| --- | --- | --- | --- |
| **Bruno Mendes** | 201906166 | TBD | TBD |
| **David Preda** | 201904726 | TBD | TBD |
| **Fernando Rego** | 201905951 | TBD | TBD |
| **Miguel Amorim** | 201907756 | TBD | TBD |

#### Global self-evaluation of the project: 19

## Summary
Our compiler takes `jmm` code, a subset of the `Java` language, and outputs `ollir`, a in-house intermediate code representation, and `jasmin`, a `JVM` stack-based language which can be run directly by the `JRE`.

The frontend parses the `jmm` code to an abstract syntaxt tree, properly annotated for the later phases to depend on, and alerts for syntaxt errors, with respective line and column information to guide the end user.

The backend is througly implemented using the visitor pattern on top of the AST nodes. First, a symbol table, containing types, names and declarations, is generated for the code. Then, the semantic analysis depends on it to warn the user about a variety of semantic errors such as type mismatches, duplicate method declarations or invalid field access in static methods. Lastly, we generate `ollir` on top of the checked AST (which is then optimized), and, finally, `jasmin` based on it.

## Semantic analysis

The semantic analysis phase of the compiler is where the program ensure that all declarations and statements are semantically correct. This process uses the syntax tree (AST) to build a symbol table and use both to do all the necessary verifications.

### Symbol table generation

The symbol table is composed by a `JmmClassSignature` that stores the class information (class name, super class name, fields), and two sub-tables, the `ImportsTable` and the `MethodsTable`. The `ImportsTable` stores information about every import that the file contain and the `MethodsTable` saves all the content of each method of the class, the method signature and all the local variables.  

To generate the symbol table we visit all nodes of the syntax tree to obtain all the data needed for the table. Therefore four visitors were implemented:

| Visitor | Explanation |
| --- | --- |
| `ImportDeclarationVisitor` | Visit every node of the kind `ImportDeclaration` at the AST and build the `ImportsTable` |
| `ClassDeclarationVisitor` | Visit the `ClassDeclaration` node to get the main data from the class and visit all the nodes of the kind `VarDeclaration` to get the fields of the class. The retrieved information allow the build of the `JmmClassSignature` |
| `MethodDeclarationVisitor` | If exists, visit the `MainMethod` and every node of kind `RegularMethod` to obtain all the information of each method of the class, which are saved into the `MethodsTable` |
| `LocalVariablesVisitor` | This visitor adds to the method table the information of all local variables by visiting all nodes of kind `VarDeclaration` inside any method |

### Semantic tests

After building the symbol table and together with the syntax tree (AST), all tests and verifications to all declarations and statements in the code are performed. The list of every semantic rule is divided in the following four major classes:

> In the `TypeCheckVisitor` is where the most magic happens. It runs through all the nodes of the syntax tree visiting everyone with an visitor associated to each node kind making the necessary verifications. The implementation is assuming that every outer method call that belongs to the super class or to one of the imports declaration is correct and returns null leaving the handle task to the parent's visitor

- `ImportCheckVisitor`
  - Verifies if all types of all declared variables associated with an outer class are imported, with exception if it refeers to the superclass that is extended

- `MethodCallVisitor`
  - If the class does not extends another class and a function call is made by an object of the class, checks if the method exists.

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
  - Checks if the field of a class exists when a access to an attribute is made. In case it does not exist, the class must extend another class
  - Verifies that every expressions in conditions must return a boolean, either for `If` statement or `While` loop
  - Checks if the return type of every method is the same compared to the given return expression
  - Verifies that for every function call, the number of arguments is correct
  - Checks if the arguments type of every function call is correct
  - For every `VarDeclaration`, checks if the type is valid


## Code generation

With the semantic analysis concluded, the compiler now moves to the code generation phase. This stage can be sudivided into two major steps: the first one transforms the AST into Ollir, changing the initial instructions to three address operations, while the second one converts Ollir to Jasmin, which will enable the final conversion to bytecodes.

The following subsections cover in a more deeply manner the two code generation phases.

### Ollir generation

Ollir is an intermediate code representation based on a three adress operation fashion, which facilitates the Jasmin code generation, given it is a stack-based language.

The `OllirEmitter` class is responsible for generating `ollir` code given the `jmm` abstract syntax tree and the symbol table generated at the semantic analysis step. The latter is useful, for example, for translating all class imports and method headers, without the need for consulting the AST.

The major challenge of this step is extracting complex nested code expressions into temporary variables to possibilitate three-adress code. This is done passing to the inner expression visitor a `SubstituteVariable`, a class responsible for holding the name, value and type of the visited expression. Its visitor is then responsible to inject the necessary code before the control flow returns to the parent's visitor.

> A method call such as *a.bar(2+3)* has to become something (in pseudo-code) like *temp = 2+3; a.bar(temp)*.

While visiting any kind of expression, a temporary variable is created to hold its value. However, there is no need to always use the holder: an identifier can be used inplace.

> There is no need to translate `a = b` to `temp = b; a = temp;`. A simple registry allocation optimization in the `visitIdentifier` method changes the value of the temporary variable to the actual identifier name and does not inject any code. The `ollir` thus becomes simply `a = b`.

An interesting challenge is the deduction of external `invoke` method call types (the internal ones are easier, since they are described in the symbol table). Since we have no access to the imported classes, we must trust the type matches at this level: the assigned variable type is passed to the right hand side expression visitor through the `assignType` field of the `SubstituteVariable` class.

> A `jmm` code like `Imported obj; obj.bar(...)` becomes `invoke(...).V`, while `Imported obj; int a; a = obj.bar(...)` becomes `a.i32 :=.i32 invoke(...).i32`.

It is thus quite difficult to infer the types of complex, external nested method calls.

> Even though the teacher's tests do not provide such a complex example, the grammar accepts statements such as `ret = obj.foo().bar()`. There is no way of knowing which type `obj.foo()` is, so this is likely to produce an incorrect `ollir` translation.

In terms of code organization, we feel that the `OllirEmitter` class has become quite large; we could split its responsabilities in more files, which would raise state managment challenges, but greatly improve the legibility.

### Ollir post-optimization
@poker

### Jasmin generation

Jasmin is an assembler for the JVM. Therefore, in order for this compiler to generate the required bytecodes, the Ollir instructions must be translated to Jasmin instructions.

The `JmmBackend` class is responsible for handling the full translation of a `.ollir` file into a `.j` file. It starts by the class and super directives, defaulting to `Object` as a super class if none is given. The fields are also translated by this class.

> If no access modifier is provided, `JmmBacked` will default a class field to private, in order to maximize security.

The `MethodDefinitionGenerator` class takes a `Method` object and translates it into a Jasmin method definition. It is responsible for generating the method header, which includes its descriptor, and for calculating the `.limit stack` and `.limit locals` values (the latter with help from the `Instruction Translator` class).

> The `.limit stack` value is determined by calculating the maximum amount of contiguous loads. On the other hand, `.limit locals` value is determined by taking the maximum register value in use by the method's var table and adding one to it (to take 0 into account).

However, the individual instruction is delegated to the `Instruction Translator` class. This class is responsible translating every type of instruction and ensuring that the necessary loads and stores are performed. Furthermore, it is also its task to generate the necessary labels to handle `if` and `goto` instructions.

The instruction translation was optimized through the usage of more efficient Jasmin instructions. Accesses and storages for registers 5 and below use the respective `iload_` and `istore_` instructions. Furthermore, constants smaller than 6 use the `iconst_` family of instructions whenever they are pushed to the stack, while byte-sized numbers use `bipush` and short-sized numbers use `sipush`. Other, larger, numbers use `ldc`. The addition operations are also made more efficient when one of the operands is a literal value, through the usage of the `iinc` instruction. 

>Assuming that t1 is stored in register 2, both `t1 + 10` and `10 + t1` would be converted to the instruction `iinc 2 10`.

Finally, less than comparisons are optimized whenver 0 is on the right side, through the usage of the `iflt` instruction.

Even though these classes behave as expected, we feel that we could have created a proper class-based framework for the translation of instructions instead of a direct instruction-to-string translation. This would have enabled us to have a better codebase, but time constraints did not allow us to implement this. 
 
## Best features
In terms of the base language, we added support for the `||` ("or") operator and for local variable declarations after statements (the original grammer forced all declarations to appear at the top of the method body).

In terms of the code architecture, we are confident that our pipeline is simple and robust, capable of detecting edge case errors and generating code for very complex expressions, in correct order. We built a strong base which can be painlessly used to support some modern `Java` syntax sugars.

> While supporting other operators such as `>`, `!=`, or `==` would be a breeze, an addition like assignments in declarations would not be so difficult, too.

## Possible improvements
Besides the already mentioned large size of some important classes, and the lack of a bigger optimization pipeline, we feel that our work could be much improved if we added more features to the base `jmm` language, such as method overloading, string literals or class constructor customizations.
