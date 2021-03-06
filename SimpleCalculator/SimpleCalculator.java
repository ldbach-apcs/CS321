/*
        CS321 - Simple Calculator.
		Sample Code.
 */

import java.util.*;
import java.io.*;

/* -------------- Abstract Syntax Tree ------------------- */

// <program>	-> <exprlist> .
final class Program {
    private ExprList el;
    
    Program(ExprList el) {
        this.el = el;
    }
    
    void interpret() {
        el.interpret();
        System.out.print(".");
    }
}

// <exprlist>	-> <expr> <exprlistrest>
final class ExprList {
    private Expr e;
    private ExprListRest elr;
    
    ExprList(Expr e, ExprListRest elr) {
        this.e = e;
        this.elr = elr;
    }
    
    void interpret() {
        System.out.print(e.interpret());
        elr.interpret();
    }
}

// <exprlistrest>	-> empty
abstract class ExprListRest {
    abstract void interpret();
}
final class EmptyExprListRest extends ExprListRest {
    EmptyExprListRest() {
    }
    
    void interpret() {
        // do nothing
    }
}

final class NextExprListRest extends ExprListRest {
	ExprListRest elr;
	Expr e;
	NextExprListRest(Expr e, ExprListRest elr) {
		this.e = e;
		this.elr = elr;
	}

	void interpret() {
		System.out.print("; ");
		System.out.print(e.interpret());
		elr.interpret();
	}
}

// <expr>	-> <term> <exprrest>
final class Expr {
    private Term t;
    private ExprRest er;
    
    Expr(Term t, ExprRest er) {
        this.t = t;
        this.er = er;
    }
    
    int interpret() {
        int v = t.interpret();
        return er.interpret(v);
    }
}

// <exprrest>	-> + <term> <exprrest>
//				-> - <term> <exprrest>
//				-> empty
abstract class ExprRest {
    abstract int interpret(int v);
}

final class PlusExprRest extends ExprRest {
    private Term t;
    private ExprRest er;
    
    PlusExprRest(Term t, ExprRest er) {
        this.t = t;
        this.er = er;
    }
    
    int interpret(int v) {
        int val = v + t.interpret();
        return er.interpret(val);
    }
}

final class MinusExprRest extends ExprRest {
    private Term t;
    private ExprRest er;
    
    MinusExprRest(Term t, ExprRest er) {
        this.t = t;
        this.er = er;
    }
    
    int interpret(int v) {
        int val = v - t.interpret();
        return er.interpret(val);
    }
}

final class EmptyExprRest extends ExprRest {
    EmptyExprRest() {
    }
    
    int interpret(int v) {
        // return the final value of the expression
        return v;
    }
}

// <term>	-> * <factor> <termrest>
//			-> / <factor> <termrest>
//			-> empty
final class Term {
    private Factor f;
    private TermRest tr;
    
    Term(Factor f, TermRest tr) {
        this.f = f;
        this.tr = tr;
    }
    
    int interpret() {
        int v = f.interpret();
        return tr.interpret(v);
    }
}

final class MultiTermRest extends TermRest{
	private Factor f;
	private TermRest tr;
	
	MultiTermRest(Factor f, TermRest tr) {
		this.f = f;
		this.tr = tr;
	}
	
	int interpret(int v) {
		int val = v * f.interpret();
		return tr.interpret(val);
	}
}

final class DivTermRest extends TermRest{
	private Factor f;
	private TermRest tr;
	
	DivTermRest(Factor f, TermRest tr) {
		this.f = f;
		this.tr = tr;
	}
	
	int interpret(int v) {
		int val = v / f.interpret();
		return tr.interpret(val);
	}
}

// <termrrest>	-> empty
abstract class TermRest {
    abstract int interpret(int v);
}
final class EmptyTermRest extends TermRest {
    EmptyTermRest() {
    }
    
    int interpret(int v) {
        // return the final value of the term
        return v;
    }
}

// <factor>	-> num
abstract class Factor {
    abstract int interpret();
}
final class NumFactor extends Factor {
    private int num;
    
    NumFactor(int num) {
        this.num = num;
    }
    
    int interpret() {
        return num;
    }
}

// <factor> -> num ^ <expr>
final class ExpoFactor extends Factor {
	private int prevNum;
	private Factor factor;
	
	ExpoFactor(int prevNum, Factor factor) {
		this.prevNum = prevNum;
		this.factor = factor;
	}
	
	int interpret() {
		return (int) Math.pow(prevNum, factor.interpret());
	}
}

/* -------------------------- Predict Top Down Parser -------------------------- */
class Parser {
    // Lexer
    private Lexer lexer;
    
    // Current token
    private Token tok;
    
    Parser(Lexer lexer) {
        this.lexer = lexer;
    }
    
    // Accept the current token iff it is of the expected kind.
    // throws ParseError if token is not of expected kind, or if lexing error occurs.
    private void match(Token tok) throws SimpleCalculatorError {
        if (this.tok == tok)
            this.tok = lexer.lex();
        else
            throw new ParseError("Expected: " + tok + ". Found: " + this.tok);
    }
    
    // Parse Program
    Program parse() throws SimpleCalculatorError {
        tok = lexer.lex();
        ExprList el = parseExprList();
        match(Token.STOP);
        
        return new Program(el);
    }
    
    // Parse ExprList
    private ExprList parseExprList() throws SimpleCalculatorError {
        Expr e = parseExpr();
        ExprListRest elr = parseExprListRest();
        
        return new ExprList(e, elr);
    }
    // Parse ExprListRest
    private ExprListRest parseExprListRest() throws SimpleCalculatorError {
		if (tok == Token.BREAK) {
			match(Token.BREAK);
			Expr e = parseExpr();
			//return new EmptyExprListRest();
			return new NextExprListRest(e, parseExprListRest());
		}
        else return new EmptyExprListRest();
    }
    
    // Parse Expr
    private Expr parseExpr() throws SimpleCalculatorError {
        Term t = parseTerm();
        ExprRest er = parseExprRest();
        
        return new Expr(t, er);
    }
    // Parse ExprRest
    private ExprRest parseExprRest() throws SimpleCalculatorError {
        if(tok == Token.PLUS) {
            match(Token.PLUS);
            Term t = parseTerm();
            ExprRest er = parseExprRest();
            return new PlusExprRest(t, er);
        }  else if (tok == Token.MINUS) {
			match(Token.MINUS);
			Term t = parseTerm();
			ExprRest er = parseExprRest();
			return new MinusExprRest(t, er);
		}
		
		else return new EmptyExprRest();
    }
    
    // Parse Term
    private Term parseTerm() throws SimpleCalculatorError {
        Factor f = parseFactor();
        TermRest tr = parseTermRest();
        
        return new Term(f, tr);
    }
    // Parse TermRest
    private TermRest parseTermRest() throws SimpleCalculatorError {
		if (tok == Token.MULTI) {
			match(Token.MULTI);
			Factor f = parseFactor();
			TermRest tr = parseTermRest();
			return new MultiTermRest(f, tr);
		} else if (tok == Token.DIV) {
			match(Token.DIV);
			Factor f = parseFactor();
			TermRest tr = parseTermRest();
			return new DivTermRest(f, tr);
		}
        else return new EmptyTermRest();
    }
    
    // Parse Factor
    private Factor parseFactor() throws SimpleCalculatorError {
        if(tok == Token.NUM) {
            int num = (Integer)lexer.attribute;
            match(Token.NUM); 
            if (tok == Token.EXPO) {
    			match(Token.EXPO);
    			return new ExpoFactor(num, parseFactor());
    		} else return new NumFactor(num);
        } if (tok == Token.OPEN) {
			match(Token.OPEN);
			int num = parseExpr().interpret();
			match(Token.CLOSE);
			if (tok == Token.EXPO) {
    			match(Token.EXPO);
    			return new ExpoFactor(num, parseFactor());
    		} else return new NumFactor(num);
		} if (tok == Token.ABS) {
			match(Token.ABS);
			int num = Math.abs(parseExpr().interpret());
			match(Token.ABS);
			if (tok == Token.EXPO) {
    			match(Token.EXPO);
    			return new ExpoFactor(num, parseFactor());
    		} else return new NumFactor(num);
		}
        else throw new ParseError("Token " + tok + " is invalid here");
    }
}

/* -------------------------- Token --------------------------------- */
enum Token {
    NUM("integer"),    // integer
    PLUS("+"),   // +
    STOP("."),
	MINUS("-"),
	DIV("/"),
	MULTI("*"),
	OPEN("("),
	CLOSE(")"),
	BREAK(";"),
	ABS("|"),
	EXPO("^"),
    EOF("eof");	// end of input
    
    private String name;
    
    Token(String name) {
        this.name = name;
    }
    public String toString() {
        return name;
    }
}

/* --------------------------- Lexer ---------------------------------- */
class Lexer {
    // Attribute for most recently returned token (or null if none)
    Object attribute = null;
    
    // Source from which we're reading
    private PushbackReader pbrd;
    
    Lexer(Reader rd) {
        pbrd = new PushbackReader(rd);
    }
    
    // Return next token, reading from input source as needed.
    Token lex() throws LexicalError {
        try {
            attribute = null;
            while (true) {
                char c = (char) pbrd.read();
                // end of input
                if (c == (char) (-1))
                    return Token.EOF;
                
                // omit white space
                if (Character.isWhitespace(c) || c == '\r' || c == '\n')
                    continue;
                else if (Character.isDigit(c)) {
                    StringBuffer lexeme = new StringBuffer();
                    lexeme.append(c);
                    c = (char) pbrd.read();
                    while (Character.isDigit(c)) {
                        lexeme.append(c);
                        c = (char) pbrd.read();
                    }
                    pbrd.unread(c);
                    attribute = Integer.valueOf(lexeme.toString());
                    return Token.NUM;
                } else {
                    if(c == '+')
                        return Token.PLUS;
					if (c == '-')
						return Token.MINUS;
                    if(c == '.')
                        return Token.STOP;
					if (c == '*')
						return Token.MULTI;
					if (c == '/')
						return Token.DIV;
					if (c == '(')
						return Token.OPEN;
					if (c == ')')
						return Token.CLOSE;
                    if (c == ';')
						return Token.BREAK;
					if (c == '|')
						return Token.ABS;
					if (c == '^')
						return Token.EXPO;
						
                    throw new LexicalError("Invalid character \'"  + c + "\'");
                }
            }
        } catch (IOException exn) {
            throw new LexicalError("I/O error");
        }
    }
}

// Exception describing parse errors
abstract class SimpleCalculatorError extends Exception {
    SimpleCalculatorError(String message) {
        super(message);
    }
}
final class LexicalError extends SimpleCalculatorError {
    LexicalError(String message) {
        super(message);
    }
}
final class ParseError extends SimpleCalculatorError {
    ParseError(String message) {
        super(message);
    }
}

/* 	------------------------  Driver ------------------------------
        Main driver for reading, parsing, and interpreting simple calculator program.
 */
class SimpleCalculator {
    public static void main(String argv[]) {
        try {
            Reader rd = new FileReader(argv[0]);
            Lexer lexer = new Lexer(rd);
            Parser parser = new Parser(lexer);
            Program program = parser.parse();
            program.interpret();
        } catch (FileNotFoundException exn) {
            System.err.println("File not found: " + argv[0]);
        } catch (SimpleCalculatorError exn) {
            if(exn instanceof LexicalError)
                System.err.println("Lexical error: " + exn.getMessage());
            else
                System.err.println("Parse error: " + exn.getMessage());
        }
    }
}