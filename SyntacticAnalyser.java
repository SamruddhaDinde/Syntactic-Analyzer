import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntacticAnalyser {

	static int index = 0;

	private static final Map<Pair<Symbol, Token.TokenType>, List<Symbol>> parsingTable = new HashMap<>();  
	//The hash map - will store the rules of the parsing table

	static {
		//1. <<prog>> → public class <<ID>> { public static void main ( String[] args ) { <<los>> } } 
		parsingTable.put(new Pair<>(TreeNode.Label.prog, Token.TokenType.PUBLIC), 
			Arrays.asList( //The first rule: Look for the rule corresponding Prog & PUBLIC
			Token.TokenType.PUBLIC, 
    		Token.TokenType.CLASS, 
    		Token.TokenType.ID, 
    		Token.TokenType.LBRACE, 
    		Token.TokenType.PUBLIC, 
    		Token.TokenType.STATIC, 
    		Token.TokenType.VOID, 
    		Token.TokenType.MAIN, 
    		Token.TokenType.LPAREN, 
    		Token.TokenType.STRINGARR, 
    		Token.TokenType.ARGS, 
    		Token.TokenType.RPAREN, 
    		Token.TokenType.LBRACE, 
    		TreeNode.Label.los, 
    		Token.TokenType.RBRACE, 
    		Token.TokenType.RBRACE
			//TreeNode.Label.terminal
			));   //not sure if all this needs to be tokenised??

		//2. <<los>> → <<stat>> <<los>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.SEMICOLON),
			Arrays.asList(
				TreeNode.Label.stat,
				TreeNode.Label.los));
		parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.TYPE),
			Arrays.asList(
				TreeNode.Label.stat,
				TreeNode.Label.los));
		parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.PRINT),
			Arrays.asList(
				TreeNode.Label.stat,
				TreeNode.Label.los));
		parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.WHILE),
			Arrays.asList(
				TreeNode.Label.stat,
				TreeNode.Label.los));
		parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.FOR),
			Arrays.asList(
				TreeNode.Label.stat,
				TreeNode.Label.los
				));
		parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.IF),
			Arrays.asList(
				TreeNode.Label.stat,
				TreeNode.Label.los
				));	
		parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.stat,
				TreeNode.Label.los
				));
		parsingTable.put(new Pair<>(TreeNode.Label.los, Token.TokenType.RBRACE),
			Arrays.asList(
				TreeNode.Label.epsilon));

		//3. <<stat>> → <<while>> | <<for>> | <<if>> | <<assign>> ; | <<decl>> ; | <<print>> ; | ;
		parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.WHILE),
			Arrays.asList(
				TreeNode.Label.whilestat
				));
		parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.FOR),
			Arrays.asList(
				TreeNode.Label.forstat
				));
		parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.IF),
			Arrays.asList(
				TreeNode.Label.ifstat
				));
		parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.assign,
				Token.TokenType.SEMICOLON
				));
		parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.TYPE),
			Arrays.asList(
				TreeNode.Label.decl,
				Token.TokenType.SEMICOLON
				));
		parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.PRINT),
			Arrays.asList(
				TreeNode.Label.print,
				Token.TokenType.SEMICOLON
				));
		parsingTable.put(new Pair<>(TreeNode.Label.stat, Token.TokenType.SEMICOLON),
			Arrays.asList(
				Token.TokenType.SEMICOLON
				));

		//4. <<while>> → while ( <<rel expr>> <<bool expr>> ) { <<los>> } 
		parsingTable.put(new Pair<>(TreeNode.Label.whilestat, Token.TokenType.WHILE),
			Arrays.asList(
				Token.TokenType.WHILE,
				Token.TokenType.LPAREN,
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr,
				Token.TokenType.RPAREN,
				Token.TokenType.LBRACE,
				TreeNode.Label.los,
				Token.TokenType.RBRACE
			));

		//5. <<for>> → for ( <<for start>> ; <<rel expr>> <<bool expr>> ; <<for arith>> ) { <<los>> } 
		parsingTable.put(new Pair<>(TreeNode.Label.forstat, Token.TokenType.FOR),
			Arrays.asList(
				Token.TokenType.FOR,
				Token.TokenType.LPAREN,
				TreeNode.Label.forstart,
				Token.TokenType.SEMICOLON,
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr,
				Token.TokenType.SEMICOLON,
				TreeNode.Label.forarith,
				Token.TokenType.RPAREN,
				Token.TokenType.LBRACE,
				TreeNode.Label.los,
				Token.TokenType.RBRACE
				));

		//6. <<for start>> → <<decl>> | <<assign>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.TYPE),
			Arrays.asList(
				TreeNode.Label.decl
				));
		parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.assign
			));
		parsingTable.put(new Pair<>(TreeNode.Label.forstart, Token.TokenType.SEMICOLON),
			Arrays.asList(
				TreeNode.Label.epsilon));

		//7. <<for arith>> → <<arith expr>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.LPAREN),
			Arrays.asList(
				TreeNode.Label.arithexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.arithexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.NUM),
			Arrays.asList(
				TreeNode.Label.arithexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.forarith, Token.TokenType.RPAREN),
			Arrays.asList(
				TreeNode.Label.epsilon));

		//8. <<if>> → if ( <<rel expr>> <<bool expr>> ) { <<los>> } <<else if>>
		parsingTable.put(new Pair<>(TreeNode.Label.ifstat, Token.TokenType.IF),
			Arrays.asList(
				Token.TokenType.IF,
				Token.TokenType.LPAREN,
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr,
				Token.TokenType.RPAREN,
				Token.TokenType.LBRACE,
				TreeNode.Label.los,
				Token.TokenType.RBRACE,
				TreeNode.Label.elseifstat
			));

		//9. <<else if>> → <<else?if>> { <<los>> } <<else if>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.ELSE),
			Arrays.asList(
				TreeNode.Label.elseorelseif,
				Token.TokenType.LBRACE,
				TreeNode.Label.los,
				Token.TokenType.RBRACE,
				TreeNode.Label.elseifstat
			));
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.RBRACE),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.SEMICOLON),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.TYPE),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.PRINT),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.WHILE),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.FOR),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.IF),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.elseifstat, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.epsilon));

		//10. <<else?if>> → else <<poss if>>
		parsingTable.put(new Pair<>(TreeNode.Label.elseorelseif, Token.TokenType.ELSE),
			Arrays.asList(
				Token.TokenType.ELSE,
				TreeNode.Label.possif
				));

		//11. <<poss if>> → if ( <<rel expr>> <<bool expr>> ) | ε
		parsingTable.put(new Pair<>(TreeNode.Label.possif, Token.TokenType.IF),
			Arrays.asList(
				Token.TokenType.IF,
				Token.TokenType.LPAREN,
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr,
				Token.TokenType.RPAREN
			));
		parsingTable.put(new Pair<>(TreeNode.Label.possif, Token.TokenType.LBRACE),
			Arrays.asList(
				TreeNode.Label.epsilon));

		//12 <<assign>> → <<ID>> = <<expr>>
		parsingTable.put(new Pair<>(TreeNode.Label.assign, Token.TokenType.ID),
			Arrays.asList(
				Token.TokenType.ID,
				Token.TokenType.ASSIGN,
				TreeNode.Label.expr
			));

		// 13 <<decl>> → <<type>> <<ID>> <<poss assign>>
		parsingTable.put(new Pair<>(TreeNode.Label.decl, Token.TokenType.TYPE),
			Arrays.asList(
				TreeNode.Label.type,
				Token.TokenType.ID,
				TreeNode.Label.possassign
				));

		//14 <<poss assign>> → = <<expr>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.possassign, Token.TokenType.ASSIGN),
			Arrays.asList(
				Token.TokenType.ASSIGN,
				TreeNode.Label.expr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.possassign, Token.TokenType.SEMICOLON),
			Arrays.asList(
				TreeNode.Label.epsilon));

		//15 <<print>> → System.out.println ( <<print expr>> )
		parsingTable.put(new Pair<>(TreeNode.Label.print, Token.TokenType.PRINT),
			Arrays.asList(
				Token.TokenType.PRINT,
				Token.TokenType.LPAREN,
				TreeNode.Label.printexpr,
				Token.TokenType.RPAREN
				));

		//16 <<type>> → int | boolean | char  ****
		parsingTable.put(new Pair<>(TreeNode.Label.type, Token.TokenType.TYPE),
			Arrays.asList(
				Token.TokenType.TYPE
			));

	    // 17 <<expr>> → <<rel expr>> <<bool expr>> | <<char expr>>
		parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.LPAREN),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.NUM),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.TRUE),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.FALSE),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.expr, Token.TokenType.SQUOTE),
			Arrays.asList(
				TreeNode.Label.charexpr
				));

		//18 <<char expr>> → ' <<char>> ' 
		parsingTable.put(new Pair<>(TreeNode.Label.charexpr, Token.TokenType.SQUOTE),
			Arrays.asList(
				Token.TokenType.SQUOTE,
				Token.TokenType.CHARLIT,    //IS THIS RIGHT?
				Token.TokenType.SQUOTE
				));

		//19.<<bool expr>> → <<bool op>> <<rel expr>> <<bool expr>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.EQUAL),
			Arrays.asList(
				TreeNode.Label.boolop,
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
			));
		parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.NEQUAL),
			Arrays.asList(
				TreeNode.Label.boolop,
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.AND),
			Arrays.asList(
				TreeNode.Label.boolop,
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.OR),
			Arrays.asList(
				TreeNode.Label.boolop,
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.RPAREN),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.boolexpr, Token.TokenType.SEMICOLON),
			Arrays.asList(
				TreeNode.Label.epsilon));

		//20 <<bool op>> → <<bool eq>> | <<bool log>>
		parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.EQUAL),
			Arrays.asList(
				TreeNode.Label.booleq
				));
		parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.NEQUAL),
			Arrays.asList(
				TreeNode.Label.booleq
				));
		parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.AND),
			Arrays.asList(
				TreeNode.Label.boollog
				));
		parsingTable.put(new Pair<>(TreeNode.Label.boolop, Token.TokenType.OR),
			Arrays.asList(
				TreeNode.Label.boollog
				));

		//21 <<bool eq>> → == | != 
		parsingTable.put(new Pair<>(TreeNode.Label.booleq, Token.TokenType.EQUAL),
			Arrays.asList(
				Token.TokenType.EQUAL
				));
		parsingTable.put(new Pair<>(TreeNode.Label.booleq, Token.TokenType.NEQUAL),
			Arrays.asList(
				Token.TokenType.NEQUAL
				));

		//22 <<bool log>> → && | ||
		parsingTable.put(new Pair<>(TreeNode.Label.boollog, Token.TokenType.AND),
			Arrays.asList(
				Token.TokenType.AND
				));
		parsingTable.put(new Pair<>(TreeNode.Label.boollog, Token.TokenType.OR),
			Arrays.asList(
				Token.TokenType.OR
			));

		//23 <<rel expr>> → <<arith expr>> <<rel expr'>> | true | false
		parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.LPAREN),
			Arrays.asList(
				TreeNode.Label.arithexpr,
				TreeNode.Label.relexprprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.arithexpr,
				TreeNode.Label.relexprprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.NUM),
			Arrays.asList(
				TreeNode.Label.arithexpr,
				TreeNode.Label.relexprprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.TRUE),
			Arrays.asList(
				Token.TokenType.TRUE
			));
		parsingTable.put(new Pair<>(TreeNode.Label.relexpr, Token.TokenType.FALSE),
			Arrays.asList(
				Token.TokenType.FALSE
				));

		//24 <<rel expr'>> → <<rel op>> <<arith expr>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.LT),
			Arrays.asList(
				TreeNode.Label.relop,
				TreeNode.Label.arithexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.LE),
			Arrays.asList(
				TreeNode.Label.relop,
				TreeNode.Label.arithexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.GT),
			Arrays.asList(
				TreeNode.Label.relop,
				TreeNode.Label.arithexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.GE),
			Arrays.asList(
				TreeNode.Label.relop,
				TreeNode.Label.arithexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.EQUAL),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.NEQUAL),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.RPAREN),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.AND),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.OR),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.relexprprime, Token.TokenType.SEMICOLON),
			Arrays.asList(
				TreeNode.Label.epsilon));

		// 25 <<rel op>> → < | <= | > | >=
		parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.LT),
			Arrays.asList(
				Token.TokenType.LT
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.LE),
			Arrays.asList(
				Token.TokenType.LE
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.GT),
			Arrays.asList(
				Token.TokenType.GT
				));
		parsingTable.put(new Pair<>(TreeNode.Label.relop, Token.TokenType.GE),
			Arrays.asList(
				Token.TokenType.GE
				));

		//26 <<arith expr>> → <<term>> <<arith expr'>>
		parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.LPAREN),
			Arrays.asList(
				TreeNode.Label.term,
				TreeNode.Label.arithexprprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.term,
				TreeNode.Label.arithexprprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexpr, Token.TokenType.NUM),
			Arrays.asList(
				TreeNode.Label.term,
				TreeNode.Label.arithexprprime
				));

		//27 <<arith expr'>> → + <<term>> <<arith expr'>> | - <<term>> <<arith expr'>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.PLUS),
			Arrays.asList(
				Token.TokenType.PLUS,
				TreeNode.Label.term,
				TreeNode.Label.arithexprprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.MINUS),
			Arrays.asList(
				Token.TokenType.MINUS,
				TreeNode.Label.term,
				TreeNode.Label.arithexprprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.EQUAL),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.NEQUAL),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.LT),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.LE),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.GT),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.GE),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.RPAREN),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.AND),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.OR),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.arithexprprime, Token.TokenType.SEMICOLON),
			Arrays.asList(
				TreeNode.Label.epsilon));

		//28. <<term>> → <<factor>> <<term'>>
		parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.LPAREN),
			Arrays.asList(
				TreeNode.Label.factor,
				TreeNode.Label.termprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.factor,
				TreeNode.Label.termprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.term, Token.TokenType.NUM),
			Arrays.asList(
				TreeNode.Label.factor,
				TreeNode.Label.termprime
				));

		// 29. <<term'>> → * <<factor>> <<term'>> | / <<factor>> <<term'>> | % <<factor>> <<term'>> | ε
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.TIMES),
			Arrays.asList(
				Token.TokenType.TIMES,
				TreeNode.Label.factor,
				TreeNode.Label.termprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.DIVIDE),
			Arrays.asList(
				Token.TokenType.DIVIDE,
				TreeNode.Label.factor,
				TreeNode.Label.termprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.MOD),
			Arrays.asList(
				Token.TokenType.MOD,
				TreeNode.Label.factor,
				TreeNode.Label.termprime
				));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.PLUS),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.MINUS),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.EQUAL),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.NEQUAL),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.LT),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.LE),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.GT),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.GE),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.RPAREN),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.AND),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.OR),
			Arrays.asList(
				TreeNode.Label.epsilon));
		parsingTable.put(new Pair<>(TreeNode.Label.termprime, Token.TokenType.SEMICOLON),
			Arrays.asList(
				TreeNode.Label.epsilon));

		// 30 <<factor>> → ( <<arith expr>> ) | <<ID>> | <<num>>
		parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.LPAREN),
			Arrays.asList(
				Token.TokenType.LPAREN,
				TreeNode.Label.arithexpr,
				Token.TokenType.RPAREN
				));
		parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.ID),
			Arrays.asList(
				Token.TokenType.ID
				));
		parsingTable.put(new Pair<>(TreeNode.Label.factor, Token.TokenType.NUM),
			Arrays.asList(
				Token.TokenType.NUM
				)); //IS THIS RIGHT?
		
		//31. <<print expr>> → <<rel expr>> <<bool expr>> | " <<string lit>> "
		parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.LPAREN),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.ID),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.NUM),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.TRUE),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
			));
		parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.FALSE),
			Arrays.asList(
				TreeNode.Label.relexpr,
				TreeNode.Label.boolexpr
				));
		parsingTable.put(new Pair<>(TreeNode.Label.printexpr, Token.TokenType.DQUOTE),
			Arrays.asList(
				Token.TokenType.DQUOTE,
				Token.TokenType.STRINGLIT,
				Token.TokenType.DQUOTE
				));





	}

	 public static ParseTree parse(List<Token> tokens) throws SyntaxException {
        if (tokens == null || tokens.isEmpty()) {
            throw new SyntaxException("List is empty");
        }

        TreeNode rootNode = new TreeNode(TreeNode.Label.prog, null);
        ParseTree parseTree = new ParseTree(rootNode);

        parseVariable(rootNode, tokens, index);

        return parseTree;
    }

    private static int parseVariable(TreeNode node, List<Token> tokens,int index) throws SyntaxException {
        TreeNode.Label variable = node.getLabel();
        if (index  < tokens.size()) {
        

        Token currentToken = tokens.get(index);
        List<Symbol> grammarRule = parsingTable.get(new Pair<>(variable, currentToken.getType()));

        if (grammarRule != null) {
        
        

        for (Symbol s : grammarRule) {
            if (s == TreeNode.Label.epsilon) {
                // This important line over here will handle the epsilon rule.
                node.addChild(new TreeNode(TreeNode.Label.epsilon, node));
            } else if (s instanceof Token.TokenType) {
                // if (index >= tokens.size()) {
                //     throw new SyntaxException("End of Input");
                // }
				if((((Token.TokenType) s) == (tokens.get(index).getType())) && index < tokens.size()){
					
					  node.addChild(new TreeNode(TreeNode.Label.terminal, tokens.get(index), node));
                		index++;
						continue;
				} 
				 throw new SyntaxException("End of Input or the token does not match");
                // Create terminal node with TreeNode.Label.terminal and the current token
               // node.addChild(new TreeNode(TreeNode.Label.terminal, tokens.get(index), node));
               // index++;
            } else if (s instanceof TreeNode.Label) {
                TreeNode childNode = new TreeNode((TreeNode.Label) s, node);
                node.addChild(childNode);
                index = parseVariable(childNode, tokens, index);
            }
        }
		} 
		else 
		{
			   throw new SyntaxException("No rule" + currentToken.getType());
		} 
		} else
		 {
			throw new SyntaxException("end of input.");
		}

        return index;
    }
}
	



// The following class may be helpful.

class Pair<A, B> {
	private final A a;
	private final B b;

	public Pair(A a, B b) {
		this.a = a;
		this.b = b;
	}

	public A fst() {
		return a;
	}

	public B snd() {
		return b;
	}

	@Override
	public int hashCode() {
		return 3 * a.hashCode() + 7 * b.hashCode();
	}

	@Override
	public String toString() {
		return "{" + a + ", " + b + "}";
	}

	@Override
	public boolean equals(Object o) {
		if ((o instanceof Pair<?, ?>)) {
			Pair<?, ?> other = (Pair<?, ?>) o;
			return other.fst().equals(a) && other.snd().equals(b);
		}

		return false;
	}

}
