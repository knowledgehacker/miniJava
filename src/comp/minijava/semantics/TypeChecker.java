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

package comp.minijava.semantics;

import java.util.Set;
import java.util.Iterator;
import java.util.ListIterator;

import comp.minijava.type.*;
import comp.minijava.symbol.*;
import comp.minijava.miniclass.*;
import comp.minijava.asyntree.Cordinate;
import comp.minijava.asyntree.expr.*;
import comp.minijava.asyntree.stmt.*;

// TODO: I do not check multiple definition of fields/methods in a class here.

/*
 * We only do type-check on each kind of statement and expression.
 * We check each statement/expression's operands' types and deduce its type by its operands' types.
 * We do not calculate each expression's value.
 * 
 * We must pay attention to one special case, new an instance by the following grammars:
 * id -> Expr
 * Expr -> new id()
 * NewExpr's id stores the name of the runtime class, and ObjectSymbol.value is used to stores the name of the runtime class.
 * So we calculate the object symbol's value using NewExpr in check(AssgnStmt), actually it is still a type deduction. 
 */
public class TypeChecker {
	private MiniClass currClass;
	private String currMethodName;
	
	public void checkWholeProgram(){
		ListIterator<MiniClass> iter = MiniClassManager.getClasses().listIterator();
		while(iter.hasNext()){
			currClass = iter.next();
			checkClass(currClass);
		}
	}
	
	public void checkClass(String className){
		currClass = MiniClassManager.find(className);
		checkClass(currClass);
	}

	private void checkClass(MiniClass miniClass){
		Set<String> methodNameList = miniClass.getMethodNameList();
		Iterator<String> iter = methodNameList.iterator();
		while(iter.hasNext()){
			currMethodName = iter.next(); 
			Stmt stmt = miniClass.getMethodBody(currMethodName);
			while(null != stmt){
				check(stmt);
				stmt = stmt.next;
			}
		}				
	}
	
	/*
	 * We can have overloaded methods whose parameters are class inheritance compatible.
	 * Ex. 
	 * 	public boolean check(Stmt stmt);
	 * 	public boolean check(AssgnStmt assgnStmt);
	 * 	Stmt stmt = new AssgnStmt(...);
	 * 
	 * 	check(stmt) will call check(Stmt), but check((AssgnStmt)stmt) will call check(AssgnStmt).
	 */
	private boolean check(Stmt stmt){
		boolean result = true;
		if(stmt instanceof AssgnStmt){
			result = check((AssgnStmt)stmt);
		}else if(stmt instanceof IfStmt){
			result = check((IfStmt)stmt);
		}else if(stmt instanceof WhileStmt){
			result = check((WhileStmt)stmt);
		}else if(stmt instanceof PrintStmt){
			result = check((PrintStmt)stmt);
		}else if(stmt instanceof ReturnStmt){
			result = check((ReturnStmt)stmt);
		}else{
			System.out.println("Error - check(Stmt stmt): Illegal statement!");
			
			result = false;
		}
		
		return result;
	}
	
	private boolean check(AssgnStmt assgnStmt){
		//check assgnStmt.id
		if(false == check(assgnStmt.id))
			return false;
		
		/*
		 * case 1: left side is an array, check assgnStmt.subscript and ssgnStmt.rvalue.
		 * assgnStmt.subscript should be of type TypeEnum.INT, assgnStmt.rvalue should be of type TypeEnum.INT_ARRAY.
		 * And assgnStmt.id and assgnStmt.rvalue should be type compatible.
		 *  
		 * case 2: left side is an identifier, check assgnStmt.rvalue.
		 * assgnStmt.id and assgnStmt.rvalue should be type compatible.
		 */
		IdentifierExpr idExpr = assgnStmt.id;
		String idName = idExpr.idName;
		Symbol idSym = currClass.findSymbolInMethods(currMethodName, idName);
		if(null == assgnStmt.subscript){
			if(false == check(assgnStmt.rvalue))
				return false;
			if(false == TypeEquivalence.check(idSym.type, assgnStmt.rvalue.type)){
				System.out.println("Error- check(AssgnStmt stmt): " + idExpr.cord.src + ":" + idExpr.cord.x + "," + idExpr.cord.y  
						+ "Can not assign to variable " + idName + " has type " +  idSym.type + "with value of type " + assgnStmt.rvalue.type);
				
				return false;
			}
			/*
			 * If idSym is an object symbol, we deduce its runtime class type by assgnStmt.rvalue. 
			 * Othersiew we don't need to calculate idSym.type since it has been determined when the variable is declared.
			 */
			if((idSym instanceof ObjectSymbol) && (assgnStmt.rvalue instanceof NewExpr)){
				ObjectSymbol objSym = (ObjectSymbol)idSym;
				NewExpr newExpr = (NewExpr)assgnStmt.rvalue;
				objSym.value = newExpr.value;
			}
		}else{
			// check assgnStmt.subscript, it should be of type TypeEnum.INT
			if(false == check(assgnStmt.subscript))
				return false;
			Expr subscriptExpr = assgnStmt.subscript;
			PrimitiveType intType = new PrimitiveType(TypeEnum.INT);
			if(false == TypeEquivalence.check(subscriptExpr.type, intType)){
				System.out.println("Error- check(AssgnStmt stmt): " + idExpr.cord.src + ":" + idExpr.cord.x + "," + idExpr.cord.y
						+ " Type of assgnStmt.subscript is " + subscriptExpr.type + ", it should be INT!");
				
				return false;
			}
			
			// check assgnStmt.rvalue, it should be of type TypeEnum.INT_ARRAY, and should be type compatible with assgnStmt.id
			if(false == check(assgnStmt.rvalue))
				return false;
			PrimitiveType intArrayType = new PrimitiveType(TypeEnum.INT_ARRAY);
			if(false == TypeEquivalence.check(assgnStmt.rvalue.type, intArrayType)){
				System.out.println("Error- check(AssgnStmt stmt): " + idExpr.cord.src + ":" + idExpr.cord.x + "," + idExpr.cord.y
						+ " Type of assgnStmt.rvalue is " + assgnStmt.rvalue.type + ", it should be INT_ARRAY!");
	
				return false;
			}
			if(false == TypeEquivalence.check(idSym.type, assgnStmt.rvalue.type)){
				System.out.println("Error- check(AssgnStmt stmt): " + idExpr.cord.src + ":" + idExpr.cord.x + "," + idExpr.cord.y
						+ "Can not assign to variable " + idName + "has type " +  idSym.type  + "with value of type " + assgnStmt.rvalue.type);
				
				return false;
			}
			// we don't need to calculate its type since it has been determined when it is declared
		}
		
		return true;
	}
	
	private boolean check(IfStmt ifStmt){
		// check ifStmt.condition
		if(false == check(ifStmt.condition))
				return false;
		Expr conditionExpr = ifStmt.condition;
		if(false == TypeEquivalence.check(conditionExpr.type, new PrimitiveType(TypeEnum.BOOLEAN))){
			// TODO: output useful information
			System.out.println("Error- check(IfStmt stmt): " + conditionExpr.cord.src + ":" + conditionExpr.cord.x + "," + conditionExpr.cord.y
					+ " Type of ifStmt.condition is " + conditionExpr.type + ", not BOOLEAN as expected!");
			
			return false;
		}
		
		// check ifStmt.trueStmt
		if(false == check(ifStmt.trueStmt))
			return false;
		
		// check ifStmt.falseStmt if exists
		if(null != ifStmt.falseStmt){
			if(false == check(ifStmt.falseStmt))
				return false;
		}
		
		return true;
	}

	private boolean check(WhileStmt whileStmt){
		// check whileStmt.condition
		if(false == check(whileStmt.condition))
				return false;
		Expr conditionExpr = whileStmt.condition;
		if(false == TypeEquivalence.check(whileStmt.condition.type, new PrimitiveType(TypeEnum.BOOLEAN))){
			// TODO: output useful information
			System.out.println("Error- check(WhileStmt stmt): " + conditionExpr.cord.src + ":" + conditionExpr.cord.x + "," + conditionExpr.cord.y
					+ " Type of whileStmt.condition is " + conditionExpr.type + ", not BOOLEAN as expected!");
			
			return false;
		}
		
		if(false == check(whileStmt.stmt))
			return false;
		
		return true;
	}

	private boolean check(PrintStmt printStmt){
		if(false == check(printStmt.expr))
			return false;
		
		return true;
	}

	private boolean check(ReturnStmt returnStmt){
		if(false == check(returnStmt.expr))
			return false;
		
		return true;
	}

	private boolean check(Expr expr){
		boolean result = true;
		
		if(expr instanceof BinaryExpr){
			result = check((BinaryExpr)expr);
		}else if(expr instanceof UnaryExpr){
			result = check((UnaryExpr)expr);
		}else if(expr instanceof ArraySubscriptExpr){
			result = check((ArraySubscriptExpr)expr);
		}else if(expr instanceof ArrayLengthExpr){
			result = check((ArrayLengthExpr)expr);
		}else if(expr instanceof CallExpr){
			result = check((CallExpr)expr);
		}else if(expr instanceof NewExpr){
			result = check((NewExpr)expr);
		}else if(expr instanceof NewArrayExpr){
			result = check((NewArrayExpr)expr);
		}else if(expr instanceof IdentifierExpr){
			result = check((IdentifierExpr)expr);
		}else if(expr instanceof ThisExpr){
			result = check((ThisExpr)expr);
		}else if(expr instanceof IntegerLiteralOperand){
			result = check((IntegerLiteralOperand)expr);
		}else if(expr instanceof BoolLiteralOperand){
			result = check((BoolLiteralOperand)expr);
		}else{
			System.out.println("Error - check(Expr expr): Unknown Expression!");
			
			result = false;
		}
		
		return result;
	}

	private boolean checkBinaryExprOperands(Opcode op, Expr leftOperand, Expr rightOperand, BaseType operandType){
		if(false == TypeEquivalence.check(leftOperand.type, operandType)){
			Cordinate cord = leftOperand.cord;
			System.out.println("Error - check(BinaryExpr expr): " + cord.src + ":" + cord.x + "," + cord.y 
					+ " Type of left operand of operator " + op + " is " + leftOperand.type + ", it should be BOOLEAN!");
			
			return false;
		}
		if(false == TypeEquivalence.check(rightOperand.type, operandType)){
			Cordinate cord = rightOperand.cord;
			System.out.println("Error - check(BinaryExpr expr): " + cord.src + ":" + cord.x + "," + cord.y 
					+ " Type of right operand of operator " + op + " is " + rightOperand.type + ", it should be BOOLEAN!"); 
				
			return false;
		}
		
		return true;
	}
	
	private boolean check(BinaryExpr expr){
		if(false == check(expr.leftOperand))
			return false;
		
		if(false == check(expr.rightOperand))
			return false;
		
		if(expr.op == Opcode.ANDAND){
			PrimitiveType booleanType = new PrimitiveType(TypeEnum.BOOLEAN);
			if(false == checkBinaryExprOperands(expr.op, expr.leftOperand, expr.rightOperand, booleanType))
				return false;
			// calculate expr.type and expr.value
			expr.type = booleanType;
			/*
			Boolean leftValue = (Boolean)expr.leftOperand.value;
			Boolean rightValue = (Boolean)expr.rightOperand.value;
			expr.value = leftValue && rightValue;
			*/
		}else if(expr.op == Opcode.LT){
			PrimitiveType integerType = new PrimitiveType(TypeEnum.INT);
			if(false == checkBinaryExprOperands(expr.op, expr.leftOperand, expr.rightOperand, integerType))
				return false;
			// calculate expr.type
			expr.type = new PrimitiveType(TypeEnum.BOOLEAN);
		}else if((expr.op == Opcode.ADD) || (expr.op == Opcode.SUB) || (expr.op == Opcode.MUL)){
			PrimitiveType integerType = new PrimitiveType(TypeEnum.INT);
			if(false == checkBinaryExprOperands(expr.op, expr.leftOperand, expr.rightOperand, integerType))
				return false;

			// calculate expr.type
			expr.type = integerType;
			/*
			Integer leftValue = (Integer)expr.leftOperand.value;
			Integer rightValue = (Integer)expr.leftOperand.value;
			if(expr.op == Opcode.LT){
				expr.value = leftValue < rightValue; 
			}else if(expr.op == Opcode.ADD){
				expr.value = leftValue + rightValue;
			}else if(expr.op == Opcode.SUB){
				expr.value = leftValue - rightValue;
			}else if(expr.op == Opcode.MUL){
				expr.value = leftValue * rightValue;
			}
			*/
		}else{
			Cordinate cord = expr.cord;
			System.out.println("Error - check(BinaryExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
					+ " Illegal operator " + expr.op + "!");
			
			return false;
		}
		
		return true;
	}

	private boolean check(UnaryExpr expr){
		if(expr.op != Opcode.NOR){
			Cordinate cord = expr.cord;
			System.out.println("Error - check(UnaryExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
					+ " Illegal operator " + expr.op + "!");
			
			return false;
		}
		PrimitiveType booleanType = new PrimitiveType(TypeEnum.BOOLEAN);
		if(false == TypeEquivalence.check(expr.operand.type, booleanType))
			return false;
		// calculate expr.type
		expr.type = booleanType;
		/*
		Boolean value = (Boolean)expr.operand.value;
		expr.value = !value;
		*/
		
		return true;
	}

	private boolean check(ArraySubscriptExpr expr){
		// check expr.array
		if(expr.array instanceof IdentifierExpr){
			if(false == check(expr.array))
				return false;
			PrimitiveType intArrayType = new PrimitiveType(TypeEnum.INT_ARRAY);
			if(false == TypeEquivalence.check(expr.array.type, intArrayType)){
				Cordinate cord = expr.array.cord;
				System.out.println("Error - check(ArraySubscriptExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
						+ " Type of expr.array is " + expr.array.type + ", it should be TypeEnum.INT_ARRAY!");
				
				return false;
			}
		}else{
			Cordinate cord = expr.array.cord;
			System.out.println("Error - check(ArraySubscriptExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
					+ " expr.array is not an IdentifierExpr as expected!");
			return false;
		}
		
		// check expr.subscript
		if(false == check(expr.subscript))
			return false;
		PrimitiveType intType = new PrimitiveType(TypeEnum.INT);
		if(false == TypeEquivalence.check(expr.subscript.type, intType)){
			Cordinate cord = expr.subscript.cord;
			System.out.println("Error - check(ArraySubscriptExpr expr): " + cord.src + ":" + cord.x + "," + cord.y 
					+ " Type of expr.subscript is " + expr.subscript.type + ", it should be TypeEnum.INT!");
			
			return false;
		}

		// calculate expr.type
		expr.type = intType;
		/*
		IdentifierExpr array = (IdentifierExpr)expr.array;
		Symbol idSym = currClass.findSymbolInMethods(currMethodName, array.idName);
		expr.value = ((int[])idSym.value)[(Integer)expr.subscript.value];
		*/
		
		return true;
	}

	private boolean check(ArrayLengthExpr expr){
		if(expr.array instanceof IdentifierExpr){
			if(false == check(expr.array))
				return false;
			if(false == TypeEquivalence.check(expr.array.type, new PrimitiveType(TypeEnum.INT_ARRAY))){
				Cordinate cord = expr.array.cord;
				System.out.println("Error - check(ArrayLengthExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
						+ " Can not get length of array ");
				
				return false;
			}
		}else{
			Cordinate cord = expr.array.cord;
			System.out.println("Error - check(ArrayLengthExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
					+ " expr.array is not an IdentifierExpr as expected!");
			
			return false;			
		}
		
		// calculate expr.type
		expr.type = new PrimitiveType(TypeEnum.INT);
		/*
		IdentifierExpr array = (IdentifierExpr)expr.array;
		Symbol idSym = currClass.findSymbolInMethods(currMethodName, array.idName);
		expr.value = ((int[])idSym.value).length;
		*/
		
		return true;
	}

	private boolean check(CallExpr expr){
		// check expr.object, it should be an IdentifierExpr
		if(expr.object instanceof IdentifierExpr){
			if(false == check((IdentifierExpr)expr.object))
				return false;
			if(false == check(expr.method))
				return false;
			// get method's signature from symbol table
			IdentifierExpr obj = (IdentifierExpr)expr.object;
			Symbol objSym = currClass.findSymbolInMethods(currMethodName, obj.idName);
			String runtimeClassName = (String)objSym.value;
			MiniClass objClass = MiniClassManager.find(runtimeClassName);
			String methodName = expr.method.idName;
			FuncSymbol methodSym = objClass.getMethodSymbol(methodName);
			
			// type check expr.parameters against method's signature
			BaseType dummyRetType = new PrimitiveType(TypeEnum.VOID);
			BaseType[] parameterTypeList = new BaseType[expr.parameters.size()];
			ExprList.ExprListIterator iter = expr.parameters.exprListIterator();
			int i = 0;
			while(iter.hasNext()){
				Expr parameter = iter.next();
				// check each parameter
				if(false == check(parameter))
					return false;
				parameterTypeList[i++] = parameter.type;
			}
			if(false == TypeEquivalence.check(methodSym.type, new FuncType(dummyRetType, parameterTypeList))){
				// TODO: output more useful information
				System.out.println("Error - check(CallExpr expr): function call arguments do not match parameters!");
				
				return false;
			}
			// we don't have to calculate expr.type, since it has been determined when the corresponding method is declared
		}
		return true;
	}

	private boolean check(NewExpr expr){
		String idName = expr.id.idName;
		Symbol idSym = currClass.findSymbolInMethods(currMethodName, idName);
		if(null == idSym){
			Cordinate cord = expr.cord;
			System.out.println("Error - check(NewExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
					+ " Can't not instantiate undefined class " + idName + "!");
			
			return false;
		}
		// calculate expr.type
		expr.type = idSym.type;
		// expr.value stores id's runtime class name
		expr.value = idSym.name;
		
		return true;
	}

	private boolean check(NewArrayExpr expr){
		// check expr.subscript
		if(false == check(expr.subscript))
			return false;
		if(false == TypeEquivalence.check(expr.subscript.type, new PrimitiveType(TypeEnum.INT))){
			Cordinate cord = expr.cord;
			System.out.println("Error - check(NewArrayExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
					+ " Type of array subscript is " + expr.subscript.type + ", it should be of type TypeEnum.INT!");
			
			return false;
		}
		
		// calculate expr.type
		expr.type = new PrimitiveType(TypeEnum.INT_ARRAY);
		/*
		expr.value = new int[(Integer)expr.subscript.value];
		*/
		
		return true;
	}

	private boolean check(IdentifierExpr expr){
		String idName = expr.idName;
		Symbol idSym = currClass.findSymbolInMethods(currMethodName, idName);
		if(null == idSym){
			Cordinate cord = expr.cord;
			System.out.println("Error - check(IdentifierExpr expr): " + cord.src + ":" + cord.x + "," + cord.y
					+ " Can not access to undeclared variable " + idName + "!");
			
			return false;
		}
		// calculate expr.type
		expr.type = idSym.type;
		
		return true;
	}

	private boolean check(ThisExpr expr){
		if(null != expr.operand){
			if(false == check(expr.operand))
				return false;
		}
		// calculate expr.type
		expr.type = new ObjectType(expr.className);
		
		return true;
	}

	private boolean check(IntegerLiteralOperand expr){
		// calculate expr.type
		expr.type = new PrimitiveType(TypeEnum.INT);
		
		return true;
	}

	private boolean check(BoolLiteralOperand expr){
		// calculate expr.type
		expr.type = new PrimitiveType(TypeEnum.BOOLEAN);

		return true;
	}
}
