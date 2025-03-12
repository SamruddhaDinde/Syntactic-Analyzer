# Syntactic Analyzer - LL(1) Parser for Java Subset

## 📌 Overview
This project implements a **Syntactic Analyzer** as part of an **LL(1) parser** that verifies the syntactic correctness of a subset of the **Java programming language**. The parser operates based on **Pushdown Automata (PDA)** principles and uses a **Parsing Table** to determine valid syntax structures.

## 🚀 Features
- **Reads and tokenizes input strings**.
- **Constructs a parsing table** based on predefined grammar rules.
- **Implements a recursive predictive parser** using a **Parsing Table**.
- **Builds and visualizes a parse tree** for valid inputs.
- **Detects syntax errors** and raises appropriate exceptions.

## 📜 Theoretical Background
The **LL(1) parser** used in this project:
- **Processes input from left to right** (L) and generates a **leftmost derivation** (L).
- **Uses one lookahead token (1)** to determine parsing decisions.
- Utilizes **FIRST() and FOLLOW() sets** to construct an unambiguous parsing table.



This grammar has also already been left factored and had left-recursion removed.

    <<prog>> → public class <<ID>> { public static void main ( String[] args ) { <<los>> } } 
    <<los>> → <<stat>> <<los>> | ε
    <<stat>> → <<while>> | <<for>> | <<if>> | <<assign>> ; | <<decl>> ; | <<print>> ; | ;
    <<while>> → while ( <<rel expr>> <<bool expr>> ) { <<los>> } 
    <<for>> → for ( <<for start>> ; <<rel expr>> <<bool expr>> ; <<for arith>> ) { <<los>> } 
    <<for start>> → <<decl>> | <<assign>> | ε
    <<for arith>> → <<arith expr>> | ε
    <<if>> → if ( <<rel expr>> <<bool expr>> ) { <<los>> } <<else if>>
    <<else if>> → <<else?if>> { <<los>> } <<else if>> | ε
    <<else?if>> → else <<poss if>>
    <<poss if>> → if ( <<rel expr>> <<bool expr>> ) | ε
    <<assign>> → <<ID>> = <<expr>>
    <<decl>> → <<type>> <<ID>> <<poss assign>>
    <<poss assign>> → = <<expr>> | ε
    <<print>> → System.out.println ( <<print expr>> )
    <<type>> → int | boolean | char
    <<expr>> → <<rel expr>> <<bool expr>> | <<char expr>>
    <<char expr>> → ' <<char>> ' 
    <<bool expr>> → <<bool op>> <<rel expr>> <<bool expr>> | ε
    <<bool op>> → <<bool eq>> | <<bool log>>
    <<bool eq>> → == | != 
    <<bool log>> → && | ||
    <<rel expr>> → <<arith expr>> <<rel expr'>> | true | false
    <<rel expr'>> → <<rel op>> <<arith expr>> | ε
    <<rel op>> → < | <= | > | >=
    <<arith expr>> → <<term>> <<arith expr'>>
    <<arith expr'>> → + <<term>> <<arith expr'>> | - <<term>> <<arith expr'>> | ε
    <<term>> → <<factor>> <<term'>>
    <<term'>> → * <<factor>> <<term'>> | / <<factor>> <<term'>> | % <<factor>> <<term'>> | ε
    <<factor>> → ( <<arith expr>> ) | <<ID>> | <<num>>
    <<print expr>> → <<rel expr>> <<bool expr>> | " <<string lit>> "
    
## 📂 Project Structure


## ⚙️ Implementation Details
### 1️⃣ **Parsing Table Construction**
- **FIRST() Set**: Determines the initial valid tokens for a given grammar symbol.
- **FOLLOW() Set**: Determines the possible tokens following a grammar symbol.
- **Table Mapping**: The parsing table is stored as a **HashMap**, mapping **(Non-Terminal, Terminal) → Production Rule**.

### 2️⃣ **Recursive Descent Parsing**
- If the **current token** matches an expected **terminal**, it is added to the parse tree.
- If the **current symbol is a non-terminal**, the parser expands it using the **Parsing Table**.
- If an invalid token appears, a **SyntaxException** is raised.

### 3️⃣ **Pushdown Automata (PDA) Simulation**
- The **call stack** of the recursive parser mimics the **PDA stack**.
- The parser **pushes and pops symbols** as it processes the input tokens.

## ✅ Example Execution
### **Input Java Code (Snippet)**
```java
public class Example {
    public static void main(String[] args) {
    }
}

