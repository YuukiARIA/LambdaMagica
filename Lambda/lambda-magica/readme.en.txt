

                  Lambda * Magica      ver 3.80

         Untyped Lambda Calculus Interpreter for Education

        Copyright (C) 2011-2012 Yuuki.S All Rights Reserved.


1. Introduction

  An interpreter of untyped lambda calculus.

1.1 Run

  - Windows
      Execute run.bat or lm.jar directly.

  - Linux/Mac
      Execute run.sh or lm.jar directly.

1.2 Run as CUI (old) mode

  To run the interpreter as CUI (old) mode, type following command:

    $ java -cp lm.jar lambda.Main


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

  Undefined macros are not expanded.
  In the case that all redexes in an expression are undefined macros, the
  expression is redarded as normal form.

  Basic macros commonly used in untyped lambda calculus are defined in
  prelude.lm.txt, attached in the package.
  Type ':l prelude' to load this.

2.1 η-reduction

  Let M is an lambda term, following transformation is called η-reduction:

    \x.Mx  -->η  M

  based on extensionality, where variable x is not in free
  variables of M.
  For example, following lambda term:

    \x.(\ab.aab)x

  and

    \ab.aab

  are extensionally equivalent -- both are reduced same result by applying to
  any lambda terms.
  Therefore, following reduction is allowed by the η-reduction:

    \x.(\ab.aab)x  -->η  \ab.aab

  On the other hand, reducing following lambda term:

    \x.(\ab.abx)x

  to

    \ab.abx

  is not allowed -- the term \ab.abx contains free variable x.

  In GUI interpreter, check 'enable eta-reduction' in order to display
  η-redexes and select one of them in the Redex view.


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

  [NOTE] Although these are available in CUI mode, some of these became
  disabled in GUI mode.
  In current version, only :? :f :l :c :pwd :q are available in the input field.
  Other settings are configured by checkboxes instead of commands.


4. Versions

- ver 3.80 (Descember 6, 2012)
  fix wrong redex finding
  add setting of the number of continuation steps in auto-mode
  improve graph panel and optimize animation algorithm
  integrate Lambda-state graph into GUI mode
  fix deadlock on startup

- ver 3.72 (December 4, 2012)
  fix auto-mode problem
  fix wrong eta-reduction such as \f.fx --> f
  change transition presentation according to the kind of reduction

- ver 3.71 (November 29, 2012)
  add step-backward
  add LaTeX code translator
  fix to change row height of macro table depending on font size

- ver 3.70 (November 26, 2012)
  add eta-reduction
  fix several small bugs
  refactoring

- ver 3.60 (November 23, 2012)
  maintained GUI mode, set GUI mode as default lauch mode
  implemented font resizing
  implemented line editor holding history
  handle StackOverflowError (when calculation diverged) in auto-mode

- ver 3.50 (November 21, 2012)
  implemented GUI mode.
  implemented redex viewer/selector, auto-mode
  revised readme

- ver 3.00 (November 15, 2012)
  maintained, fixed, and improved considerably.

- ver 2.40 (November 3, 2011)
  fix a problem that :l command does not load files.
  fix a problem that reduction stops remaining macros in certain cases.
  add some system commands.

- ver 2.35
  The first
