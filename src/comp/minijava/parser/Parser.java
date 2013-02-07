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

import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import comp.minijava.lexer.TokenType;
import comp.minijava.lexer.Token;
import comp.minijava.lexer.Lexer;

import comp.minijava.type.TypeEnum;
import comp.minijava.type.BaseType;
import comp.minijava.type.PrimitiveType;
import comp.minijava.type.ObjectType;
import comp.minijava.type.FuncType;

import comp.minijava.symbol.Scope;
import comp.minijava.symbol.Symbol;
import comp.minijava.symbol.VoidSymbol;
import comp.minijava.symbol.BooleanSymbol;
import comp.minijava.symbol.IntSymbol;
import comp.minijava.symbol.IntArraySymbol;
import comp.minijava.symbol.ObjectSymbol;
import comp.minijava.symbol.FuncSymbol;

import comp.minijava.miniclass.MiniClass;
import comp.minijava.miniclass.MiniClassManager;

import comp.minijava.asyntree.Cordinate;
import comp.minijava.asyntree.expr.ArrayLengthExpr;
import comp.minijava.asyntree.expr.ArraySubscriptExpr;
import comp.minijava.asyntree.expr.BinaryExpr;
import comp.minijava.asyntree.expr.BoolLiteralOperand;
import comp.minijava.asyntree.expr.CallExpr;
import comp.minijava.asyntree.expr.Expr;
import comp.minijava.asyntree.expr.ExprList;
import comp.minijava.asyntree.expr.IdentifierExpr;
import comp.minijava.asyntree.expr.IntegerLiteralOperand;
import comp.minijava.asyntree.expr.NewArrayExpr;
import comp.minijava.asyntree.expr.NewExpr;
import comp.minijava.asyntree.expr.Opcode;
import comp.minijava.asyntree.expr.ThisExpr;
import comp.minijava.asyntree.expr.UnaryExpr;
import comp.minijava.asyntree.stmt.AssgnStmt;
import comp.minijava.asyntree.stmt.IfStmt;
import comp.minijava.asyntree.stmt.PrintStmt;
import comp.minijava.asyntree.stmt.ReturnStmt;
import comp.minijava.asyntree.stmt.Stmt;
import comp.minijava.asyntree.stmt.WhileStmt;

public class Parser {
	private Lexer lexer;
	private Token token;
	
	/*
	 * We plan to use one symbol table for each class.
	 * currClassName here records the name of the class we are parsing.
	 * currMethodName here records the name of the method we are parsing.
	 * currClassName is assigned the name of a class when parsing rules of non-terminal MainClass and ClassDecl.
	 * And a Class is created at the same time and assigned to currClass.
	 * 
	 * scope tells us whether we are parsing a method or a instance variable.
	 */
	private String currClassName;
	private MiniClass currClass;
	private String currMethodName;
	private Scope scope;
	
	public Parser(String fileName){
		lexer = new Lexer(fileName);
	}

	// inline method position
	private final Cordinate position(){
		return new Cordinate(lexer.fileName, lexer.lineNo, lexer.lineOffset);
	}
	
	private void createClass(String className){
		currClassName = className;
		currClass = new MiniClass(currClassName);
		
		/*
		 *  scope is set to Scope.VARIABLE by default. 
		 *  when we enter a method, it is set to Scope.METHOD, and restores to Scope.VARIABLE when exists the method. 
		 */
		scope = Scope.VARIABLE;
	}
	
	private void enterMethod(FuncSymbol methodSymbol, ArrayList<Symbol> params){
		currClass.createMethodSymbol(methodSymbol, params);
		
		scope = Scope.METHOD;
	}

	private void exitMethod(){
		scope = Scope.VARIABLE;
	}

	private Symbol createIdSymbol(String idName, BaseType idType){
		Symbol idSymbol = null;
		if(idType instanceof PrimitiveType){
			PrimitiveType primitiveType = (PrimitiveType)idType;
			if(primitiveType.type == TypeEnum.VOID){
				idSymbol = new VoidSymbol(idName);
			}else if(primitiveType.type == TypeEnum.BOOLEAN){
				idSymbol = new BooleanSymbol(idName);
			}else if(primitiveType.type == TypeEnum.INT){
				idSymbol = new IntSymbol(idName);
			}else if(primitiveType.type == TypeEnum.INT_ARRAY){
				idSymbol = new IntArraySymbol(idName+"[");
			}
		}else if(idType instanceof ObjectType){
			ObjectType classType = (ObjectType)idType;
			idSymbol = new ObjectSymbol(idName, classType.className);
		}else{
			System.out.println("invalid data type!");
		}
		
		return idSymbol;
	}
	
	/*
	 *  0. Program -> MainClass ClassDeclOpt
	 */
	public void program(){
		try {
			token = lexer.nextToken();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int ruleIndex = CFG.getRule(NonTerminalType.Program, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.Program);
			return;
		}
		
		System.out.println("parsing main class starts...");
		mainClass();
		// add main class to MiniClassManager
		MiniClassManager.add(currClass);
		System.out.println("parsing main class finishes...");

		classDeclOpt();
	}
	
	/*
	 * 1. MainClass -> class id{ private void main(){ Stmt } }
	 */
	private void mainClass(){
		int ruleIndex = CFG.getRule(NonTerminalType.MainClass, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.MainClass);
			return;
		}

		eatToken(TokenType.CLASS);
		
		// create symbol table for class id
		currClassName = (String)eatToken(TokenType.IDENTIFIER);
		createClass(currClassName);
		eatToken(TokenType.LBRACE);
		
		eatToken(TokenType.PUBLIC);
		// get return type
		eatToken(TokenType.VOID);
		// get method name
		eatToken(TokenType.MAIN);
		currMethodName = "main";
		// get types of parameters
		eatToken(TokenType.LPAREN);
		eatToken(TokenType.RPAREN);
		// create symbol for method
		FuncType methodType = new FuncType(new PrimitiveType(TypeEnum.VOID), null);
		FuncSymbol methodSymbol = new FuncSymbol(currMethodName, methodType, null);

		eatToken(TokenType.LBRACE);	
		enterMethod(methodSymbol, null);
		Stmt methodBody = stmt();
		//methodSymbol.funcBody = methodBody;
		currClass.addMethodBody(currMethodName, methodBody);
		exitMethod();
		eatToken(TokenType.RBRACE);
		
		eatToken(TokenType.RBRACE);		
	}
	
	/*
	 * 2. ClassDeclOpt -> ClassDecl ClassDeclOpt
     *   		       -> EPSILON
	 */
	private void classDeclOpt(){
		if(null == token){
			// reach the end of stream for source file bin\cn\edu\tsinghua\comp\test\Factorial.ava
			return;
		}
		int ruleIndex = CFG.getRule(NonTerminalType.ClassDeclOpt, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ClassDeclOpt);
			return;
		}

		if(ruleIndex == 0){
			classDecl();
			// add class to MiniClassManager
			MiniClassManager.add(currClass);
			classDeclOpt();
		}
	}

	/*
	 * 3. ClassDecl -> class id ClassDeclRest
     */
	private void classDecl(){
		int ruleIndex = CFG.getRule(NonTerminalType.ClassDecl, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ClassDecl);
			return;
		}

		eatToken(TokenType.CLASS);		
		// create symbol table for class id
		currClassName = (String)eatToken(TokenType.IDENTIFIER);
		createClass(currClassName);
		
		classDeclRest();
	}
	
	/*
	 * 4. ClassDeclRest -> { VarDeclOpt MethodDeclOpt }
	 *                  -> extends id { VarDeclOpt MethodDeclOpt }
	 */
	private void classDeclRest(){
		int ruleIndex = CFG.getRule(NonTerminalType.ClassDeclRest, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ClassDeclRest);
			return;
		}

		switch(ruleIndex){
		case 0:
			eatToken(TokenType.LBRACE);
			
			varDeclOpt();
			methodDeclOpt();

			eatToken(TokenType.RBRACE);
			break;
		case 1:
			eatToken(TokenType.EXTENDS);

			// link symbolTable.parent to className's super class's symbol table
			String superClassName = (String)eatToken(TokenType.IDENTIFIER);
			currClass.setParent(MiniClassManager.find(superClassName));
			
			break;
		}
	}

	/*
	 * 5. VarDeclOpt -> VarDecl VarDeclOpt
     *				 -> EPSILON
     */
	private void varDeclOpt(){
		int ruleIndex = CFG.getRule(NonTerminalType.VarDeclOpt, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.VarDeclOpt);
			return;
		}

		if(ruleIndex == 0){
			varDecl();
			varDeclOpt();
		}
	}
	
	/*
	 * 6. VarDecl -> Type id;
	 */
	private void varDecl(){
		int ruleIndex = CFG.getRule(NonTerminalType.VarDecl, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.VarDecl);
			return;
		}

		// TODO: we should check whether variable id is a duplicate definition or not? 
		BaseType idType = type();
		String idName = (String)eatToken(TokenType.IDENTIFIER);
		Symbol idSymbol = createIdSymbol(idName, idType);
		if(scope == Scope.VARIABLE)
			currClass.addSymbolToFields(idSymbol);
		else
			currClass.addSymbolToMethods(currMethodName, idSymbol);
		
		eatToken(TokenType.SEMICOLON);
	}
	
	/* 
	 * 7. MethodDeclOpt -> MethodDecl MethodDeclOpt
     *     				-> EPSILON
	 */
	private void methodDeclOpt(){
		int ruleIndex = CFG.getRule(NonTerminalType.MethodDeclOpt, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.MethodDeclOpt);
			return;
		}
		
		if(ruleIndex == 0){
			methodDecl();
			methodDeclOpt();
		}
	}
	
	/*
	 * 8. MethodDecl -> private Type id(ParamList){ VarDeclOpt StmtOpt }
	 */
	private void methodDecl(){
		int ruleIndex = CFG.getRule(NonTerminalType.MethodDecl, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.MethodDecl);
			return;
		}

		eatToken(TokenType.PUBLIC);
		// get return type of the method
		BaseType retType = type();
		currMethodName = (String)eatToken(TokenType.IDENTIFIER);
		// get types of parameters
		eatToken(TokenType.LPAREN);
		ArrayList<Symbol> params = new ArrayList<Symbol>();
		paramList(params);
		BaseType[] paramsType = null;
		if(false == params.isEmpty()){
			paramsType = new BaseType[params.size()];
			ListIterator<Symbol> iter = params.listIterator();
			int i = 0;
			while(iter.hasNext()){
				Symbol param = iter.next();
				PrimitiveType paramType = (PrimitiveType)param.getType();
				paramsType[i++] = paramType;
			}
		}
		
		FuncSymbol methodSymbol = null;
		if(retType instanceof PrimitiveType){
			PrimitiveType  primitiveType = (PrimitiveType)retType;
			FuncType methodType = new FuncType(primitiveType, paramsType);
			methodSymbol = new FuncSymbol(currMethodName, methodType, params);
		}
		eatToken(TokenType.RPAREN);
	
		// parse method body
		eatToken(TokenType.LBRACE);
		
		enterMethod(methodSymbol, params);
		varDeclOpt();
		Stmt methodBody = stmtOpt();
		currClass.addMethodBody(currMethodName, methodBody);
		exitMethod();
		
		eatToken(TokenType.RBRACE);
	}
	
	/*
	 * 9. ParamList -> Type id ParamListRest
     *				-> EPSILON
     */
	private void paramList(ArrayList<Symbol> params){
		int ruleIndex = CFG.getRule(NonTerminalType.ParamList, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ParamList);
			return;
		}

		if(ruleIndex == 0){
			// add parameter symbols to method symbol table
			BaseType idType = type();
			String idName = (String)eatToken(TokenType.IDENTIFIER);
			// for an array with name "xxx", it is stored in symbol table as "xxx["
			if((idType instanceof PrimitiveType) &&(((PrimitiveType)idType).type == TypeEnum.INT_ARRAY))
				idName += "[";
			Symbol idSymbol = createIdSymbol(idName, idType);
			
			params.add(idSymbol);
			paramListRest(params);
		}
	}
		
	/*
	 * 10. ParamListRest -> ,Type id ParamListRest
     *				    -> EPSILON
     */
	private void paramListRest(ArrayList<Symbol> params){
		int ruleIndex = CFG.getRule(NonTerminalType.ParamListRest, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ParamListRest);
			return;
		}

		if(ruleIndex == 0){
			eatToken(TokenType.COMMA);
			
			BaseType idType = type();
			String idName = (String)eatToken(TokenType.IDENTIFIER);
			// for an array with name "xxx", it is stored in symbol table as "xxx["
			if((idType instanceof PrimitiveType) &&(((PrimitiveType)idType).type == TypeEnum.INT_ARRAY))
				idName += "[";
			Symbol idSymbol = createIdSymbol(idName, idType);
			
			params.add(idSymbol);
			paramListRest(params);
		}
	}
	
	/*
	 * 11. Type -> void
	 * 			-> boolean
     *		    -> int IntTypeRest
	 * 		    -> id  # user-defined class 
	 */
	private BaseType type(){
		int ruleIndex = CFG.getRule(NonTerminalType.Type, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.Type);
			return null;
		}
		
		BaseType idType = null;
		switch(ruleIndex){
		case 0:
			eatToken(TokenType.VOID);
			
			idType = new PrimitiveType(TypeEnum.VOID);
			break;
		case 1:
			eatToken(TokenType.BOOLEAN);
			
			idType = new PrimitiveType(TypeEnum.BOOLEAN);
			break;
		case 2:
			eatToken(TokenType.INT);
			
			idType = intTypeRest();
			break;
		case 3:
			String className = (String)eatToken(TokenType.IDENTIFIER);

			idType = new ObjectType(className);
			break;
		}
	
		return idType;
	}
	
	/*
	 * 12. IntTypeRest -> []
                       ->
	 */
	private BaseType intTypeRest(){		
		int ruleIndex = CFG.getRule(NonTerminalType.IntTypeRest, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.IntTypeRest);
			return null;
		}
		
		if(ruleIndex == 0){
			eatToken(TokenType.LSQBRACE);
			eatToken(TokenType.RSQBRACE);

			return new PrimitiveType(TypeEnum.INT_ARRAY);
		}else if(ruleIndex == 1)
			return new PrimitiveType(TypeEnum.INT);
		else
			return null;	
	}
	
	/*
	 * TODO: What kind of information about statement do we need, how to represent it? 
	 */
	
	/* 
	 * 13. StmtOpt -> Stmt StmtOpt
     *			   -> EPSILON
	 */
	private Stmt stmtOpt(){
		int ruleIndex = CFG.getRule(NonTerminalType.StmtOpt, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.StmtOpt);
			return null;
		}
		
		Stmt stmt = null;
		if(ruleIndex == 0){
			// TODO: create a statement list? 
			stmt = stmt();
			Stmt stmtRest = stmtOpt();
			if(null != stmtRest)
				stmt.next = stmtRest;
		}
		
		return stmt;
	}

	/*
	 * 14. Stmt -> {StmtOpt}
	 *		    -> AssgnStmt
	 *		    -> IfStmt
	 *		    -> WhileStmt
     *		    -> PrintStmt
	 *		    -> ReturnStmt
	 */
	private Stmt stmt(){
		int ruleIndex = CFG.getRule(NonTerminalType.Stmt, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.Stmt);
			return null;
		}

		Stmt stmt = null;
		switch(ruleIndex){
		case 0:
			eatToken(TokenType.LBRACE);
			stmt = stmtOpt();
			eatToken(TokenType.RBRACE);
			break;
		case 1:
			stmt = assgnStmt();
			break;
		case 2:
			stmt = ifStmt();
			break;
		case 3:
			stmt = whileStmt();
			break;
		case 4:
			stmt = printStmt();
			break;
		case 5:
			stmt = returnStmt();
			break;
		}	
		
		return stmt;
	}
	
	/*
	 * 15. AssgnStmt -> id AssgnStmtRest
	 */
	private AssgnStmt assgnStmt(){
		int ruleIndex = CFG.getRule(NonTerminalType.AssgnStmt, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.AssgnStmt);
			return null;
		}

		String idName = (String)eatToken(TokenType.IDENTIFIER);
		AssgnStmt stmt = assgnStmtRest(idName);
		return stmt;
	}
	
	/*
	 * 16. AssgnStmtRest -> = Expr;
     *	                 -> [Expr] = Expr;
	 */
	private AssgnStmt assgnStmtRest(String idName){
		int ruleIndex = CFG.getRule(NonTerminalType.AssgnStmtRest, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.AssgnStmtRest);
			return null;
		}

		AssgnStmt stmt = null;
		switch(ruleIndex){
		case 0:{
				eatToken(TokenType.ASSGN);
	
				IdentifierExpr id = new IdentifierExpr(position(), idName);
				Expr rvalue = expr();
				stmt = new AssgnStmt(id, rvalue);
		
				eatToken(TokenType.SEMICOLON);
			break;
			}
		case 1:{
				eatToken(TokenType.LSQBRACE);	
				Expr subscript = expr(); 
				eatToken(TokenType.RSQBRACE);
		
				eatToken(TokenType.ASSGN);
				
				IdentifierExpr id = new IdentifierExpr(position(), idName+"[");
				Expr rvalue = expr();
				stmt = new AssgnStmt(id, subscript, rvalue);
					
				eatToken(TokenType.SEMICOLON);
			}
			break;
		}
		
		return stmt;
	}
	
	/*
	 * 17. IfStmt -> if(Expr)Stmt ElsePart
	 */
	private IfStmt ifStmt(){
		int ruleIndex = CFG.getRule(NonTerminalType.IfStmt, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.IfStmt);
			return null;
		}

		eatToken(TokenType.IF);
		eatToken(TokenType.LPAREN);
		Expr condition = expr();
		eatToken(TokenType.RPAREN);
		
		System.out.println("ifStmt: if ...");
		Stmt trueStmt = stmt();
		IfStmt stmt = new IfStmt(condition, trueStmt);
		System.out.println("ifStmt: else ...");
		elsePart(stmt);
		return stmt;	
	}
	
	/*
	 * 18. ElsePart -> else Stmt
	 * 				->
	 */
	private void elsePart(IfStmt stmt){
		System.out.println("elsePart: token = " + token.getType());
		int ruleIndex = CFG.getRule(NonTerminalType.ElsePart, token);
		System.out.println("elsePart: ruleIndex = " + ruleIndex);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ElsePart);
			return;
		}

		if(ruleIndex == 0){
			eatToken(TokenType.ELSE);
			
			Stmt falseStmt = stmt();
			stmt.falseStmt = falseStmt;
		}
	}
	
	/*
	 * 19. WhileStmt -> while(Expr) Stmt
	 */
	private WhileStmt whileStmt(){
		int ruleIndex = CFG.getRule(NonTerminalType.WhileStmt, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.WhileStmt);
			return null;
		}

		eatToken(TokenType.WHILE);
		eatToken(TokenType.LPAREN);
		Expr condition = expr();
		eatToken(TokenType.RPAREN);
		
		return new WhileStmt(condition, stmt());
	}
	
	/*
	 * 20. PrintStmt -> print(Expr);
	 */
	private PrintStmt printStmt(){
		int ruleIndex = CFG.getRule(NonTerminalType.PrintStmt, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.PrintStmt);
			return null;
		}

		eatToken(TokenType.PRINT);
		eatToken(TokenType.LPAREN);
		Expr expr = expr();
		eatToken(TokenType.RPAREN);
		eatToken(TokenType.SEMICOLON);
		
		return new PrintStmt(expr);		
	}
	
	/*
	 * 21. ReturnStmt -> return Expr;
	 */
	private ReturnStmt returnStmt(){
		int ruleIndex = CFG.getRule(NonTerminalType.ReturnStmt, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ReturnStmt);
			return null;
		}

		eatToken(TokenType.RETURN);
		Expr expr = expr();
		ReturnStmt stmt = new ReturnStmt(expr);
		eatToken(TokenType.SEMICOLON);

		return stmt;
	}
	
	/*
	 * Attention: each parse routine for non-terminal below doesn't exactly map to one expression class.
	 * For example, routine "Unary" is responsible for parsing unary expression and postfix expression(including function call).
	 * But expression class "UnaryExpr" is only used to represent unary expression "!Expr", 
	 * it is not used to represent postfix expression which is not a unary expression.   
	 */
	
	/*
	 * 22. Expr -> UnaryExpr ExprRest
	 */
	private Expr expr(){
		int ruleIndex = CFG.getRule(NonTerminalType.Expr, token); 
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.Expr);
			return null;
		}
		
		Expr leftOperand = unaryExpr();
		BinaryExpr exprRest = exprRest(leftOperand);
		if(null == exprRest)
			return leftOperand;
		
		return exprRest;
	}
	
	/*
	 * 23. ExprRest -> BinOp UnaryExpr ExprRest
	 *              -> EPSILON
	 */	
	private BinaryExpr exprRest(Expr leftOperand){
		int ruleIndex = CFG.getRule(NonTerminalType.ExprRest, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ExprRest);
			return null;
		}

		BinaryExpr exprRestExpr = null;
		if(ruleIndex == 0){
			/*
			 * BinaryExpr, we need to guarantee the left associativity of binary operators here
			 * 			op
			 * 		/		\
			 * 	leftOperand	rightOperand
			 */
			Opcode op = binOp();
			Expr rightOperand = unaryExpr();
			BinaryExpr binaryExpr = new BinaryExpr(position(), op, leftOperand, rightOperand);
			exprRestExpr = exprRest(binaryExpr);
			if(null == exprRestExpr)
				return binaryExpr;
		}
		
		return exprRestExpr;
	}

	/*
	 * 24. UnaryExpr -> !UnaryExpr
	 *               -> PostfixExpr
	 */
	private Expr unaryExpr(){
		int ruleIndex = CFG.getRule(NonTerminalType.UnaryExpr, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.UnaryExpr);
			return null;
		}

		Expr unaryExpr = null;
		switch(ruleIndex){
		case 0:
			// UnaryExpr
			eatToken(TokenType.NOR);
			Expr operand = unaryExpr();
			unaryExpr = new UnaryExpr(position(), Opcode.NOR, operand);
			break;
		case 1:
			unaryExpr = postfixExpr();
			break;
		}
		
		return unaryExpr;
	}
	 	
	/*
	 * 25. PostfixExpr -> BasicExpr PostfixExprRest
	 */
	private Expr postfixExpr(){
		int ruleIndex = CFG.getRule(NonTerminalType.PostfixExpr, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.PostfixExpr);
			return null;
		}

		Expr basicExpr = basicExpr();
		Expr postfixExprRest = postfixExprRest(basicExpr);
		if(null == postfixExprRest)
			return basicExpr;
		
		return postfixExprRest;
	}
	
	/*
	 * 26. PostfixExprRest -> PostfixOp PostfixExprRest
  	 *                     ->
  	 */
	private Expr postfixExprRest(Expr expr){
		int ruleIndex = CFG.getRule(NonTerminalType.PostfixExprRest, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.PostfixExprRest);
			return null;
		}
		
		if(ruleIndex == 0){
			Expr postfixOp = postfixOp(expr);
			Expr postfixExprRest = postfixExprRest(postfixOp);
			if(null == postfixExprRest)
				return postfixOp;
			
			return postfixExprRest;
		}
		
		return null;
	}

	/*
	 * 27. PostfixOp -> .PostfixOpRest
     *               -> [Expr]
     */
	private Expr postfixOp(Expr expr){
		int ruleIndex = CFG.getRule(NonTerminalType.PostfixOp, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.PostfixOp);
			return null;
		}

		Expr postfixOp = null;
		switch(ruleIndex){
		case 0:
			eatToken(TokenType.DOT);
			postfixOp = postfixOpRest(expr);
			break;
		case 1:			
			// ArraySubscriptExpr
			eatToken(TokenType.LSQBRACE);
			Expr subscript = expr();
			eatToken(TokenType.RSQBRACE);			
			postfixOp = new ArraySubscriptExpr(position(), Opcode.ARRAY_SUBSCRIPT, expr, subscript);
			break;
		}
		
		return postfixOp;
	}

	/*
	 * 28. PostfixOpRest -> length
	 *                   -> id(ExpList)
	 */
	private Expr postfixOpRest(Expr expr){
		int ruleIndex = CFG.getRule(NonTerminalType.PostfixOpRest, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.PostfixOpRest);
			return null;
		}

		Expr postfixOpRest = null;
		switch(ruleIndex){
		case 0:
			// ArrayLengthExpr
			eatToken(TokenType.LENGTH);
			postfixOpRest = new ArrayLengthExpr(position(), Opcode.ARRAY_LENGTH, expr);
			break;
		case 1:
			// CallExpr
			String methodName = (String)eatToken(TokenType.IDENTIFIER);
			IdentifierExpr method = new IdentifierExpr(position(), methodName); 
			eatToken(TokenType.LPAREN);
			postfixOpRest = new CallExpr(position(), Opcode.CALL, expr, method);
			exprList((CallExpr)postfixOpRest);
			eatToken(TokenType.RPAREN);
			break;
		}
		
		return postfixOpRest;
	}

	/*
	 * 29. BasicExpr -> NewExpr
	 *               -> id
	 *               -> INTEGER_LITERAL
	 *               -> this # this expression
	 *               -> true
	 *               -> false
     *               -> (Expr)
	 */
	private Expr basicExpr(){
		int ruleIndex = CFG.getRule(NonTerminalType.BasicExpr, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.BasicExpr);
			return null;
		}
		
		Expr basicExpr = null;
		switch(ruleIndex){
		case 0:
			basicExpr = newExpr();
			break;
		case 1:
			// IdentifierExpr
			String idName = (String)eatToken(TokenType.IDENTIFIER);
			basicExpr = new IdentifierExpr(position(), idName);
			break;
		case 2:
			// IntegerLiteralOperand
			Integer integerLiteral = (Integer)eatToken(TokenType.INTEGER_LITERAL);	
			basicExpr = new IntegerLiteralOperand(position(), integerLiteral);
			break;
		case 3:
			// ThisExpr
			eatToken(TokenType.THIS);
			basicExpr = new ThisExpr(position(), currClassName);
			break;
		case 4:
			// BoolLiteralOperand
			eatToken(TokenType.TRUE);
			basicExpr = new BoolLiteralOperand(position(), true);
			break;
		case 5:
			// BoolLiteralOperand
			eatToken(TokenType.FALSE);
			basicExpr = new BoolLiteralOperand(position(), false);
			break;
		case 6:
			eatToken(TokenType.LPAREN);
			basicExpr = expr();
			eatToken(TokenType.RPAREN);			
			break;
		}
		
		return basicExpr;
	}

	/*
	 * 30. NewExpr -> new NewExprRest
	 */
	private Expr newExpr(){
		int ruleIndex = CFG.getRule(NonTerminalType.NewExpr, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.NewExpr);
			return null;
		}
		
		eatToken(TokenType.NEW);		
		return newExprRest();
	}

	/*
	 * 31. NewExprRest -> id()
	 *                 -> int[Expr]
	 */
	private Expr newExprRest(){
		int ruleIndex = CFG.getRule(NonTerminalType.NewExprRest, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.NewExprRest);
			return null;
		}

		Expr newExprRest = null;
		switch(ruleIndex){
		case 0:
			// NewExpr
			String idName = (String)eatToken(TokenType.IDENTIFIER);				
			IdentifierExpr id = new IdentifierExpr(position(), idName);
			newExprRest = new NewExpr(position(), Opcode.NEW, id);
				
			eatToken(TokenType.LPAREN);
			eatToken(TokenType.RPAREN);
			break;
		case 1:
			// NewArrayExpr
			eatToken(TokenType.INT);
			eatToken(TokenType.LSQBRACE);
			Expr subscript = expr();
			newExprRest = new NewArrayExpr(position(), Opcode.NEW_ARRAY, subscript);
			eatToken(TokenType.RSQBRACE);
		}
		
		return newExprRest;
	}

	/*
	 * 32. ExprList -> Expr ExprListRest
	 *				->
	 */
	private void exprList(CallExpr callExpr){
		int ruleIndex = CFG.getRule(NonTerminalType.ExprList, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ExprList);
			return;
		}

		if(ruleIndex == 0){
			Expr expr = expr();
			callExpr.parameters = new ExprList(expr);
			exprListRest(callExpr);
		}	
	}

	/*
	 * 33. ExprListRest -> ,Expr ExprListRest
	 * 					-> EPSILON
	 */
	private void exprListRest(CallExpr callExpr){
		int ruleIndex = CFG.getRule(NonTerminalType.ExprListRest, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.ExprListRest);
			return;
		}

		if(ruleIndex == 0){
			eatToken(TokenType.COMMA);
			
			Expr expr = expr();
			callExpr.parameters.add(expr);
			exprListRest(callExpr);
		}
	}

	/*
	 * 34. BinOp -> &&
	 *		     -> <
	 *		     -> +
	 *		     -> -
	 *		     -> *
	 */
	private Opcode binOp(){
		Opcode op = null;
		
		int ruleIndex = CFG.getRule(NonTerminalType.BinOp, token);
		if(-1 == ruleIndex){
			errorRecovery(NonTerminalType.BinOp);
			return null;
		}
	
		switch(ruleIndex){
		case 0:
			eatToken(TokenType.ANDAND);
			op = Opcode.ANDAND;
			break;
		case 1:
			eatToken(TokenType.LT);
			op = Opcode.LT;
			break;
		case 2:
			eatToken(TokenType.ADD);
			op = Opcode.ADD;
			break;
		case 3:
			eatToken(TokenType.SUB);
			op = Opcode.SUB;
			break;
		case 4:
			eatToken(TokenType.MUL);
			op = Opcode.MUL;
			break;
		}
		
		return op;
	}

	
	/*
	 * @inline method eatToken
	 * Each time we check whether current input token is the terminal as we expect.
	 * If not, it is an error during parsing a terminal, we output a message. Otherwise we get its value.
	 * Then iterate to the next input token and check it against the next terminal/non-terminal in current rule.
	 * 
	 * TODO: how to handle the case that calling eatToken to get a value(such as identifier name, integer literal) but the value is null during parsing???
	 */
	private final Object eatToken(int tokenType){
		if(null != token)
			System.out.println(token.getValue());
		
		Object tokenValue = null;
		if(token.getType() != tokenType)
			System.out.println("Error - " + lexer.lineNo + ":" + lexer.lineOffset + ", input token: " + token.getValue() + ", expected: " + tokenType);
		else
			tokenValue = token.getValue();

		try {
			token = lexer.nextToken();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tokenValue;
	}
	
	/*
	 *  @inline method errorRecovery
	 *  Here we adopt simple mechanism to recovery from error during parsing a non-terminal.
	 *  If current input token can't be derived by rules of non-terminal "nonTerminal", 
	 *  we just skip several input tokens until a token belongs to follow(nonTerminal) is met. 
	 */
	private final void errorRecovery(int nonTerminal){
		System.out.println("Error - " + lexer.lineNo + ":" + lexer.lineOffset + ", input token: " + token.getValue() + ", non-terminal: " + nonTerminal);
		
		do{
			try {
				token = lexer.nextToken();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}while(false == CFG.isFollowItem(nonTerminal, token));
	}	
}
