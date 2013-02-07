/**
 * Copyright (c) 2012 - 2013 minglin. All rights reserved.

 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package comp.minijava.parser;

/*
import cn.edu.tsinghua.comp.lexer.TokenType;

public class NonTerminal extends Token{
	public Integer value;

	public NonTerminal(int nonTerminalValue){
		super(TokenType.NONTERMINAL);

		value = new Integer(nonTerminalValue);
	}

	public Integer getValue(){
		return value;
	}
}
*/

public interface NonTerminalType{
    public final static int Program = 0;
    public final static int MainClass = 1;
    public final static int ClassDeclOpt = 2;
    public final static int ClassDecl = 3;
    public final static int ClassDeclRest = 4;
    public final static int VarDeclOpt = 5;
    public final static int VarDecl = 6;
    public final static int MethodDeclOpt = 7;
    public final static int MethodDecl = 8;
    public final static int ParamList = 9;
    public final static int ParamListRest = 10;
    public final static int Type = 11;
    public final static int IntTypeRest = 12;

    public final static int StmtOpt = 13;
    public final static int Stmt = 14;
    public final static int AssgnStmt = 15;
    public final static int AssgnStmtRest = 16;
    public final static int IfStmt = 17;
    public final static int ElsePart = 18;
    public final static int WhileStmt = 19;
    public final static int PrintStmt = 20;
    public final static int ReturnStmt = 21;

    public final static int Expr = 22;
    public final static int ExprRest = 23;
    public final static int UnaryExpr = 24;
    public final static int PostfixExpr = 25;
    public final static int PostfixExprRest = 26;
    public final static int PostfixOp = 27;
    public final static int PostfixOpRest = 28;
    public final static int BasicExpr = 29;
    public final static int NewExpr = 30;
    public final static int NewExprRest = 31;
    public final static int ExprList = 32;
    public final static int ExprListRest = 33;
    public final static int BinOp = 34;

    // records the number of non-terminals in context free grammar for miniJava
    public final static int Number = 35;
}	
