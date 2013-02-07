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

import java.util.HashSet;
import java.util.ArrayList;
import java.util.ListIterator;
import comp.minijava.lexer.TokenType;
import comp.minijava.lexer.Token;

public class CFG implements TokenType, NonTerminalType{	
	private static ArrayList<int[]>[] ruleSet;
	//public static Boolean[] nullableNonTerminals;
	private static boolean[] nullableNonTerminals;
	
	private static ArrayList<Integer>[] firstSets;
	private static boolean[][] followSets;

	/*
	 *  Given non-terminal 'NT', input token 'Token', predictTable[NT][Token-PUBLIC] = j, j is the index into ruleSet[NT].
	 *  That is, ruleSet[NT].get(j) is the rule for derivation.
	 */
	private static int[][] predictTable;

	static{
		// setup rules for context free grammar
		setupRules();

		// setup predict table
		setupPredictTable();
	}

	public static void setupPredictTable(){
		// calculate nullable non-terminals
		/*
		nullableNonTerminals = new Boolean[Number];
		for(int i = 0; i < Number; ++i)
			nullableNonTerminals[i] = null;
		for(int i = 0; i < Number; ++i){
			if(nullableNonTerminals[i] == null)
				calcNullableNonTerminal(i);
		}
		*/
		nullableNonTerminals = new boolean[Number];
		for(int i = 0; i < Number; ++i)
			nullableNonTerminals[i] = false;
		calcNullableNonTerminal();
		
		// setup predict table
		int rowSize = Number;
		int columnSize = EPSILON-PUBLIC;
		predictTable = new int[rowSize][columnSize];
		for(int i = 0; i < rowSize; ++i)
			for(int j = 0; j < columnSize; ++j)
				predictTable[i][j] = -1;
		
		// calculate first/follows set for all non-terminals
		calcFirstFollowSets();
				
		for(int i = 0; i < Number; ++i){
			int ruleNumber = ruleSet[i].size();
			for(int j = 0; j < ruleNumber; ++j){
				boolean isNullable = false;
				int[] rule = ruleSet[i].get(j);
				/*
				 * if X -> .
				 * we should put this rule in predictTable[X][T], for each T in follow(T)???
				 */
				if(EPSILON == rule[0])
					isNullable = true;
				else{
					int ruleLength = rule.length;
					int k;
					for(k = 0; k < ruleLength; ++k){
						if(!((rule[k] <= BinOp) && (true == nullableNonTerminals[rule[k]])))
							break;
					}
					if(k == ruleLength)
						isNullable = true;
				}
				if(true == isNullable){
					for(int col = 0; col < columnSize; ++col)
						if(true == followSets[i][col]){
							int ruleIndex = predictTable[i][col];
							/*
							 * Duplicate rules may exist in predictTable[i][col] since first(i) and follow(i) contain token col+PUBLIC.
							 * It's probably the grammar is ambiguous, but I don't know how to correct it.
							 * For the moment, I just take rule indexed first(i) priority over the one indexed by follow(i).
							 * TODO: fix the issue that duplicate rules may exist one single item in predict table.
							 */
							if(-1 != ruleIndex){
								if(ruleIndex == j)
									System.out.println("Duplicate in predictTable[" + i + "][" + col + "]: "
											+ ruleIndex + " by both first and follow set");
								else
									System.out.println("Conflict in predictTable[" + i + "][" + col + "]: "
											+ ruleIndex + " by first set, " + j + " by follow set");
							}else
								predictTable[i][col] = j;
						}
				}
			}
		}
	}
	
	/*
	 * This non-recursive implementation iterate ruleSet for several times until no more nullable non-terminals can be found.
	 * As to CFG for miniJava, this implementation converges very fast, actually only two iterations are needed.
	 */
	public static void calcNullableNonTerminal(){
		boolean changed = true;
		while(changed){
			changed = false;
			
			for(int i = 0; i < Number; ++i){
				if(true == nullableNonTerminals[i])
					continue;

				int ruleNumber = ruleSet[i].size();
				for(int j = 0; j < ruleNumber; ++j){
					int[] rule = ruleSet[i].get(j);
					if(EPSILON == rule[0]){
						nullableNonTerminals[i] = true;
						
						changed = true;
						break;
					}
					int ruleLength = rule.length;
					int k;
					for(k = 0; k < ruleLength; ++k){
						if(!((rule[k] <= BinOp) && (true == nullableNonTerminals[rule[k]])))
							break;
					}
					if(k == ruleLength){
						nullableNonTerminals[i] = true;
						
						changed = true;
						break;
					}
				}
				
			}
			//System.out.println("changed = " + changed);
		}
	}
	
	/*
	 * This recursive implementation will cause StackFlowError excpetion.
	 * 
	 * Algorithm:
	 * Check each rule of nonTerminal.
	 * if current rule is an EPSILON rule, then nonTerminal is a nullable non-terminal, we don't need to check subsequent rules.
	 * if current rule is not an EPSILON rule, check the item in right-hand side of current rule one by one:
	 * a) if current item is a terminal, then we don't need to check the subsequent items, check the next rule directly.
	 * b) if current item is a non-terminal, and it is a non-nullable non-terminal, then we don't need to check the subsequent items, check the next rule directly.
	 *    Otherwise check the next item.
	 * If each item is neither terminal nor non-nullable non-terminal, then nonTerminal is a nullable non-terminal, we don't need to check subsequent rules.
	 * Otherwise check the next rule, and so on, until all the rules will be checked.   
	 */
	/*
	public static void calcNullableNonTerminal(int nonTerminal){
		if(null != nullableNonTerminals[nonTerminal])
			return;
		
		Boolean isNullable = false;
		ArrayList<int[]> rules = new ArrayList<int[]>(ruleSet[nonTerminal]);
		int ruleNumber = rules.size();
		for(int i = 0; i < ruleNumber; ++i){
			int[] rule = rules.get(i);
			if(EPSILON == rule[0]){
				isNullable = true;
				break;
			}
			int ruleLength = rule.length;
			int j;
			for(j = 0; j < ruleLength; ++j){
				int xx = rule[j];
				if(xx <= BinOp){
					if(null == nullableNonTerminals[xx])
						calcNullableNonTerminal(xx);
					if(false == nullableNonTerminals[xx])
						break;
				}else
					break;
			}
			if(j == ruleLength){
				isNullable = true;
				break;
			}
		}
		
		nullableNonTerminals[nonTerminal] = isNullable;
	}
	*/
	
	@SuppressWarnings("unchecked")
	public static void calcFirstFollowSets(){
		firstSets = (ArrayList<Integer>[]) new ArrayList[Number];
		for(int i = 0; i < Number; ++i)
			firstSets[i] = new ArrayList<Integer>();
		for(int j = 0; j < Number; ++j)
			if(true == firstSets[j].isEmpty())
				calcFirstSet(j);

		// Q: how to determine the size fo an object in Java?
		
		int rowSize = Number;
		int columnSize = EPSILON-PUBLIC;
		followSets = new boolean[rowSize][columnSize];
		for(int i = 0; i < rowSize; ++i)
			for(int j = 0; j < columnSize; ++j)
				followSets[i][j] = false;                                  
		calcFollowSets();	
		
		for(int j = 0; j < columnSize; ++j)
			if(followSets[VarDeclOpt][j] == true)
				System.out.print(j + ":true ");
			System.out.println();
	}
	
	/*
	 * Calculate first set for non-terminal 'nonTerminal'.
	 * For 'nonTerminal', check the first terminal/non-terminal, namely xx, in the right hand side of its each rule.
	 * a) If xx is a terminal, add it to the first set of non-terminal 'nonTerminal'.
	 * b) If xx is a non-terminal, add xx's first set to the first set of non-terminal 'nonTerminal'.
	 *    and if the first set of xx contains EPSILON, add xx's follow set to the first set of non-terminal 'nonTerminal' as well.
	 */
	public static ArrayList<Integer> calcFirstSet(int nonTerminal){
		if(false == firstSets[nonTerminal].isEmpty())
			return firstSets[nonTerminal];
		
		/*
		 * Remember we don't allow a non-terminal with several rules whose right-hand sides begin with the same terminal/nonterminal.
		 * Since such grammar is ambiguous. 
		 */
		int ruleNumber = ruleSet[nonTerminal].size();
		for(int i = 0; i < ruleNumber; ++i){
			/*
			 * If the first item in right-hand side of rule is a non-terminal, and it is nullable.
			 * Then check the item following this non-terminal, and so on, until the item is either a terminal or a non-nullable non-terminal, or the last item is reached.
			 */
			int[] rule = ruleSet[nonTerminal].get(i);
			if(EPSILON == rule[0])
				continue;
			
			int ruleLength = rule.length;
			for(int j = 0; j < ruleLength; ++j){
				int xx = rule[j];
				if(xx <= BinOp){
					//if(true == firstNonTerminalProcessed.add(xx)){					
						ArrayList<Integer> xxFirstSet = new ArrayList<Integer>(calcFirstSet(xx));
						ListIterator<Integer> xxFirstIter = xxFirstSet.listIterator();
						while(xxFirstIter.hasNext()){
							int token = xxFirstIter.next();
							// add token in non-terminal xx's fisrt set to nonTerminal's first set 
							firstSets[nonTerminal].add(token);
							
							// for non-terminal 'nonTerminal', input token 'token', use ith rule of 'nonTerminal' to match.
							predictTable[nonTerminal][token-PUBLIC] = i;
						}						
					//}
					
					if(false == nullableNonTerminals[xx])
						break;
				}else{
					//if(true == firstTerminalProcessed.add(xx)){
						// add token to nonTerminal's first set 
						firstSets[nonTerminal].add(xx);
						
						// for non-terminal 'nonTerminal', input token 'xx', use ith rule of 'nonTerminal' to match.
						predictTable[nonTerminal][xx-PUBLIC] = i;
					//}
					
					break;
				}
			}
		}
		
		return firstSets[nonTerminal];
	}
	
	@SuppressWarnings("unchecked")
	public static void calcFollowSets(){
		HashSet<Integer>[] firstNonTerminalProcessed = (HashSet<Integer>[]) new HashSet[Number];
		for(int i = 0; i < Number; ++i)
			firstNonTerminalProcessed[i] = new HashSet<Integer>();
		                 
		boolean changed = true;
		while(changed){
			changed = false;
			
			for(int i = 0; i < Number; ++i){
				/*
				 * check rule j of non-terminal i.
				 * check every item at right-hand side of rule j from left to right.
				 * As to current item rule[k], if it is a terminal, just ignore it.  
				 * Otherwise check the item following it, item rule[k+1]:
				 * a) if item rule[k+1] is a terminal, add it to item rule[k]'s follow set.
				 * b) If item rule[k+1] is a non-terminal, add its first set to item rule[k]'s follow set.
				 *    And if item rule[k+1] is a nullable non-terminal, check the item following item rule[k+1], item rule[k+2]. And so on, until the last item is checked. 
				 *    If all the items following item rule[k] are nullable non-terminals, add non-terminal i's follow set to item rule[k]'s follow set.
				 */
				int ruleNumber = ruleSet[i].size();
				for(int j = 0; j < ruleNumber; ++j){
					int[] rule = ruleSet[i].get(j);
					if(EPSILON == rule[0])
						continue;
					
					int ruleLength = rule.length;
					for(int k = 0; k < ruleLength; ++k){
						if(rule[k] <= BinOp){
							int l;
							for(l = k+1; l < ruleLength; ++l){
								if(rule[l] <= BinOp){
									/*
									 * if non-terminal rule[l]'s first set has been added to rule[k]'s follow set before in previous iterations,
									 * then we don't need to check it again. 
									 * Otherwise we should check the tokens in rule[l]'s first set one by one to see if any new token will be added to rule[k]'s follow set.
									 * Since although we know there are already tokens in rule[l]'s first set, but maybe these tokens already exist in rule[k]'s follow set(added by other non-terminals in previous iterations).
									 */
									if(true == firstNonTerminalProcessed[rule[k]].add(rule[l])){
										ListIterator<Integer> firstIter = firstSets[rule[l]].listIterator();
										while(firstIter.hasNext()){
											int token = firstIter.next();
											if(false == followSets[rule[k]][token-PUBLIC]){
												followSets[rule[k]][token-PUBLIC] = true;
												changed = true;		
											}
										}
									}
									
									if(false == nullableNonTerminals[rule[l]])
										break;
								}else{
									if(false == followSets[rule[k]][rule[l]-PUBLIC]){
										followSets[rule[k]][rule[l]-PUBLIC] = true;
										changed = true;
									}
									
									break;
								}									
							}
							if(l == ruleLength){
								/*
								 * No matter the non-terminal i's follow set has been added to rule[k]'s in previous iterations.
								 * We should check it here since non-terminal i's follow set maybe changed in previous iterations.
								 */
								int columnSize = EPSILON-PUBLIC;
								for(int col = 0; col < columnSize; ++col){
									if((true == followSets[i][col]) && (false == followSets[rule[k]][col])){
										followSets[rule[k]][col] = true;
										changed = true;
									}
								}
							}
						}
					}	
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void setupRules(){
		ruleSet = (ArrayList<int[]>[]) new ArrayList[Number];
		for(int i = 0; i < Number; ++i)
			ruleSet[i] = new ArrayList<int[]>();
		//System.out.println("freeMemory = " + runtime.freeMemory()); 
		
		/*
		 *  0. Program -> MainClass ClassDeclOpt
		 */
		int[] programRule1 =	{MainClass, ClassDeclOpt};
		ruleSet[Program].add(programRule1);

		/*
		 * 1. MainClass -> class id{ public void main(){ StmtOpt } }
		 */
		int[] mainClassRule1 = {CLASS, IDENTIFIER, LBRACE, PUBLIC, VOID, MAIN, LPAREN, RPAREN, LBRACE, StmtOpt, RBRACE, RBRACE};
		ruleSet[MainClass].add(mainClassRule1);
		
		/*
		 * 2. ClassDeclOpt -> ClassDecl ClassDeclOpt
         *   		       -> EPSILON
		 */
		int[] classDeclOptRule1 = {ClassDecl, ClassDeclOpt};		
		int[] classDeclOptRule2 = {EPSILON};
		ruleSet[ClassDeclOpt].add(classDeclOptRule1);
		ruleSet[ClassDeclOpt].add(classDeclOptRule2);		

		/*
		 * 3. ClassDecl -> class id ClassDeclRest
         */
		int[] classDeclRule1 = {CLASS, IDENTIFIER, ClassDeclRest};
		ruleSet[ClassDecl].add(classDeclRule1);

		/*
		 * 4. ClassDeclRest -> { VarDeclOpt MethodDeclOpt }
		 *                  -> extends id { VarDeclOpt MethodDeclOpt }
		 */
		int[] classDeclRestRule1 = {LBRACE, VarDeclOpt, MethodDeclOpt, RBRACE};
		int[] classDeclRestRule2 = {EXTENDS, IDENTIFIER, LBRACE, VarDeclOpt, MethodDeclOpt, RBRACE};
		ruleSet[ClassDeclRest].add(classDeclRestRule1);
		ruleSet[ClassDeclRest].add(classDeclRestRule2);

		/*
		 * 5. VarDeclOpt -> VarDecl VarDeclOpt
         *				 -> EPSILON
         */
		int[] varDeclOptRule1 = {VarDecl, VarDeclOpt};
		int[] varDeclOptRule2 = {EPSILON};
		ruleSet[VarDeclOpt].add(varDeclOptRule1);
		ruleSet[VarDeclOpt].add(varDeclOptRule2);		

		/*
		 * 6. VarDecl -> Type id;
		 */
		int[] varDeclRule1 = {Type, IDENTIFIER, SEMICOLON};
		ruleSet[VarDecl].add(varDeclRule1);

		/* 
		 * 7. MethodDeclOpt -> MethodDecl MethodDeclOpt
         *     				-> EPSILON
		 */
		int[] methodDeclOptRule1 = {MethodDecl, MethodDeclOpt};
		int[] methodDeclOptRule2 = {EPSILON};
		ruleSet[MethodDeclOpt].add(methodDeclOptRule1);
		ruleSet[MethodDeclOpt].add(methodDeclOptRule2);		

		/*
		 * 8. MethodDecl -> public Type id(ParamList){ VarDeclOpt StmtOpt }
		 */
		int[] methodDeclRule1 = {PUBLIC, Type, IDENTIFIER, LPAREN, ParamList, RPAREN, LBRACE, VarDeclOpt, StmtOpt, RBRACE};
		ruleSet[MethodDecl].add(methodDeclRule1);

		/*
		 * 9. ParamList -> Type id ParamListRest
         *				-> EPSILON
         */
		int[] paramListRule1 = {Type, IDENTIFIER, ParamListRest};
		int[] paramListRule2 = {EPSILON};
		ruleSet[ParamList].add(paramListRule1);
		ruleSet[ParamList].add(paramListRule2);		

		/*
		 * 10. ParamListRest -> ,Type id ParamListRest
         *				    -> EPSILON
         */
		int[] paramListRestRule1 = {COMMA, Type, IDENTIFIER, ParamListRest};
		int[] paramListRestRule2 = {EPSILON};
		ruleSet[ParamListRest].add(paramListRestRule1);
		ruleSet[ParamListRest].add(paramListRestRule2);

		/*
		 * 11. Type -> void
		 *		   -> boolean
	     *		   -> int IntTypeRest
		 * 		   -> id  # user-defined class 
		 */
		int[] typeRule1 = {VOID};
		int[] typeRule2 = {BOOLEAN};
		int[] typeRule3 = {INT, IntTypeRest};
		int[] typeRule4 = {IDENTIFIER};
		ruleSet[Type].add(typeRule1);
		ruleSet[Type].add(typeRule2);
		ruleSet[Type].add(typeRule3);
		ruleSet[Type].add(typeRule4);

		/*
		 * 12. IntTypeRest -> []
         *			       -> EPSILON
		 */
		int[] intTypeRestRule1 = {LSQBRACE, RSQBRACE};
		int[] intTypeRestRule2 = {EPSILON};
		ruleSet[IntTypeRest].add(intTypeRestRule1);
		ruleSet[IntTypeRest].add(intTypeRestRule2);

		/* 
		 * 13. StmtOpt -> Stmt StmtOpt
         *			   -> EPSILON
		 */
		int[] stmtOptRule1 = {Stmt, StmtOpt};
		int[] stmtOptRule2 = {EPSILON};
		ruleSet[StmtOpt].add(stmtOptRule1);
		ruleSet[StmtOpt].add(stmtOptRule2);

		/*
		 * 14. Stmt -> {StmtOpt}
		 *		    -> AssgnStmt
		 *		    -> IfStmt
		 *		    -> WhileStmt
	     *		    -> PrintStmt
		 *		    -> ReturnStmt
		 */
		int[] stmtRule1 = {LBRACE, StmtOpt, RBRACE};
		int[] stmtRule2 = {AssgnStmt};
		int[] stmtRule3 = {IfStmt};
		int[] stmtRule4 = {WhileStmt};
		int[] stmtRule5 = {PrintStmt};
		int[] stmtRule6 = {ReturnStmt};
		ruleSet[Stmt].add(stmtRule1);
		ruleSet[Stmt].add(stmtRule2);
		ruleSet[Stmt].add(stmtRule3);
		ruleSet[Stmt].add(stmtRule4);
		ruleSet[Stmt].add(stmtRule5);
		ruleSet[Stmt].add(stmtRule6);

		/*
		 * 15. AssgnStmt -> id AssgnStmtRest
		 */
		int[] assgnStmtRule1 = {IDENTIFIER, AssgnStmtRest};
		ruleSet[AssgnStmt].add(assgnStmtRule1);

		/*
		 * 16. AssgnStmtRest -> = Expr;
         *	                 -> [Expr] = Expr;
		 */
		int[] assgnStmtRestRule1 = {ASSGN, Expr, SEMICOLON};
		int[] assgnStmtRestRule2 = {LSQBRACE, Expr, RSQBRACE, ASSGN, Expr, SEMICOLON};
		ruleSet[AssgnStmtRest].add(assgnStmtRestRule1);
		ruleSet[AssgnStmtRest].add(assgnStmtRestRule2);

		/*
		 * 17. IfStmt -> if(Expr)Stmt ElsePart
		 */
		int[] ifStmtRule1 = {IF, LPAREN, Expr, RPAREN, Stmt, ElsePart};
		ruleSet[IfStmt].add(ifStmtRule1);

		/*
		 * 18. ElsePart -> else Stmt
		 * 				-> EPSILON
		 */
		int[] elsePartRule1 = {ELSE, Stmt};
		int[] elsePartRule2 = {EPSILON};
		ruleSet[ElsePart].add(elsePartRule1);
		ruleSet[ElsePart].add(elsePartRule2);
		
		/*
		 * 19. WhileStmt -> while(Expr) Stmt
		 */
		int[] whileStmtRule = {WHILE, LPAREN, Expr, RPAREN, Stmt};
		ruleSet[WhileStmt].add(whileStmtRule);

		/*
		 * 20. PrintStmt -> print(Expr);
		 */
		int[] printStmtRule = {PRINT, LPAREN, Expr, RPAREN, SEMICOLON};
		ruleSet[PrintStmt].add(printStmtRule);

		/*
		 * 21. ReturnStmt -> return Expr;
		 */
		int[] returnStmtRule = {RETURN, Expr, SEMICOLON};
		ruleSet[ReturnStmt].add(returnStmtRule);

		/*
		 * 22. Expr -> UnaryExpr ExprRest
		 */
		int[] exprRule1 = {UnaryExpr, ExprRest};
		ruleSet[Expr].add(exprRule1);
		
		/*
		 * 23. ExprRest -> BinOp UnaryExpr ExprRest
		 *              -> EPSILON
		 */
		int[] exprRestRule1 = {BinOp, UnaryExpr, ExprRest};
		int[] exprRestRule2 = {EPSILON};
		ruleSet[ExprRest].add(exprRestRule1);
		ruleSet[ExprRest].add(exprRestRule2);
		
		/*
		 * 24. UnaryExpr -> !UnaryExpr
		 *               -> PostfixExpr
		 */
		int[] unaryExprRule1 = {NOR, UnaryExpr};
		int[] unaryExprRule2 = {PostfixExpr};
		ruleSet[UnaryExpr].add(unaryExprRule1);
		ruleSet[UnaryExpr].add(unaryExprRule2);
		
		/*
		 * 25. PostfixExpr -> BasicExpr PostfixExprRest
		 */
		int[] postfixExprRule1 = {BasicExpr, PostfixExprRest};
		ruleSet[PostfixExpr].add(postfixExprRule1);
		
		/*
		 * 26. PostfixExprRest -> PostfixOp PostfixExprRest
      	 *                     -> EPSILON
      	 */
		int[] postfixExprRestRule1 = {PostfixOp, PostfixExprRest};
		int[] postfixExprRestRule2 = {EPSILON};
		ruleSet[PostfixExprRest].add(postfixExprRestRule1);
		ruleSet[PostfixExprRest].add(postfixExprRestRule2);
		
		/*
		 * 27. PostfixOp -> .PostfixOpRest
         *               -> [Expr]
         */
		int[] postfixOpRule1 = {DOT, PostfixOpRest};
		int[] postfixOpRule2 = {LSQBRACE, Expr, RSQBRACE};
		ruleSet[PostfixOp].add(postfixOpRule1);
		ruleSet[PostfixOp].add(postfixOpRule2);
		
		/*
		 * 28. PostfixOpRest -> length
		 *                   -> id(ExpList)
		 */
		int[] postfixOpRestRule1 = {LENGTH};
		int[] postfixOpRestRule2 = {IDENTIFIER, LPAREN, ExprList, RPAREN};
		ruleSet[PostfixOpRest].add(postfixOpRestRule1);
		ruleSet[PostfixOpRest].add(postfixOpRestRule2);
		                  
		/*
		 * 29. BasicExpr -> NewExpr
		 *               -> id
		 *               -> INTEGER_LITERAL
		 *               -> this # this expression
		 *               -> true
		 *               -> false
	     *               -> (Expr)
		 */
		int[] basicExprRule1 = {NewExpr};
		int[] basicExprRule2 = {IDENTIFIER};
		int[] basicExprRule3 = {INTEGER_LITERAL};
		int[] basicExprRule4 = {THIS};
		int[] basicExprRule5 = {TRUE};
		int[] basicExprRule6 = {FALSE};
		int[] basicExprRule7 = {LPAREN, Expr, RPAREN};
		ruleSet[BasicExpr].add(basicExprRule1);
		ruleSet[BasicExpr].add(basicExprRule2);
		ruleSet[BasicExpr].add(basicExprRule3);
		ruleSet[BasicExpr].add(basicExprRule4);
		ruleSet[BasicExpr].add(basicExprRule5);
		ruleSet[BasicExpr].add(basicExprRule6);
		ruleSet[BasicExpr].add(basicExprRule7);

		/*
		 * 30. NewExpr -> new NewExprRest
		 */
		int[] newExprRule1 = {NEW, NewExprRest};
		ruleSet[NewExpr].add(newExprRule1);
		
		/*
		 * 31. NewExprRest -> id()
		 *                 -> int[Expr]
		 */
		int[] newExprRestRule1 = {IDENTIFIER, LPAREN, RPAREN};
		int[] newExprRestRule2 = {INT, LSQBRACE, Expr, RSQBRACE};
		ruleSet[NewExprRest].add(newExprRestRule1);
		ruleSet[NewExprRest].add(newExprRestRule2);
		
		/*
		 * 32. ExprList -> Expr ExprListRest
		 *				-> EPSILON
		 */
		int[] exprListRule1 = {Expr, ExprListRest};
		int[] exprListRule2 = {EPSILON};
		ruleSet[ExprList].add(exprListRule1);
		ruleSet[ExprList].add(exprListRule2);

		/*
		 * 33. ExprListRest -> ,Expr
		 * 					-> EPSILON
		 */
		int[] exprListRestRule1 = {COMMA, Expr};
		int[] exprListRestRule2 = {EPSILON};
		ruleSet[ExprListRest].add(exprListRestRule1);
		ruleSet[ExprListRest].add(exprListRestRule2);

		/*
		 * 34. BinOp -> &&
		 *		     -> <
		 *		     -> +
		 *		     -> -
		 *		     -> *
		 */
		int[] binOpRule1 = {ANDAND};
		int[] binOpRule2 = {LT};
		int[] binOpRule3 = {ADD};
		int[] binOpRule4 = {SUB};
		int[] binOpRule5 = {MUL};
		ruleSet[BinOp].add(binOpRule1);
		ruleSet[BinOp].add(binOpRule2);
		ruleSet[BinOp].add(binOpRule3);
		ruleSet[BinOp].add(binOpRule4);
		ruleSet[BinOp].add(binOpRule5);
	}
	
	public static int getRule(int nonTerminal, Token token){
		return predictTable[nonTerminal][token.getType()-TokenType.PUBLIC];
	}
	
	public static boolean isFollowItem(int nonTerminal, Token token){
		return followSets[nonTerminal][token.getType()-TokenType.PUBLIC];
	}
}	 
