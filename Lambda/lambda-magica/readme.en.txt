

                  Lambda * Magica      ver 3.50

         Untyped Lambda Calculus Interpreter for Education


1. Introduction

  An interpreter of untyped lambda calculus.

1.1 Run

  - Windows
      Execute run.bat or lm.jar directly.

  - Linux/Mac
      Execute run.sh or lm.jar directly.

1.2 GUI mode (experimental)

  To run the interpreter as GUI mode, type following command:

    $ java -cp lm.jar lambda.gui.Main

  Though the GUI mode is still under developed, more interactive features are
  available.
    - Step by step reduction
    - Interactive redex selection
    - Macro definition table            and more features in the future...


2. Basics

    M ::= \x.M    - λ-abstraction
        | M M     - application
        | x       - variable
        | <m>     - macro (extended syntax)

  A variable name consists of only one alphabet character (A-Z, a-z).
  So that, you need not to insert any spaces as delimiters between variables.

  When you bound a new variable using a name that already bounded, the new
  name hides the old name (shadowing).
  Therefore an expression \x.(\x.x) doesn't represent \y.(\x.y) but \x.(\y.y).

  To input 'lambda' symbol, use backslash (\) instead of λ.
  It is not a problem that some fonts display backslash as japanese yen symbol.

  It is able to abbreviate abstractions such as \x.\y.xy to \xy.xy.

  Application is left associative.
  This means that an expression 'xyz' represents '(xy)z.'

  To define a macro, input following expression in the interpreter.

    id = \x.x

  After this, you can use <id> instead of (\x.x) in expressions.
  For example, an expression (\y.yy)<id> is computed as follow:

    (\y.yy)<id> --> <id><id>
                --> (\x.x)<id>
                --> <id>
                --> (\x.x)

  Basic macros commonly used in untyped lambda calculus are defined in
  prelude.lm.txt, attached in the package.
  Type ':l prelude' to load this.


3. System Commands

  Built-in commands.

    :?            - show command help
    :f <expr>     - expand all macros in the expression, and show the result
                    reduction.
    :l <name>     - load macro definitions from a file
    :l            - load all *.lm.txt files in current directory
    :s <n>        - set a number of continuation steps
    :s            - show the number of current continuation steps
    :t (on|off)   - set trace mode
    :t            - show current setting of trace mode
    :m            - list names and expressions of all defined macros
    :clear        - clear all macro definitions
    :pwd          - print working directory
    :short        - toggle abbreviation mode
    :conv         - toggle data abstraction mode
    :q            - quit interpreter


4. Versions

- ver 3.00 (November 15, 2012)
  maintained, fixed, and improved considerably.

- ver 2.40 (November 3, 2011)
  fix a problem that :l command does not load files.
  fix a problem that reduction stops remaining macros in certain cases.
  add some system commands.

- ver 2.35
  The first
