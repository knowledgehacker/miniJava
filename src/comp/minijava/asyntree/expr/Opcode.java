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

package comp.minijava.asyntree.expr;

/*
 * We can also use interface and constants here.
 * Use enumeration is instinctive, but enumeration can not be used as a case label(switch-case statement).
 */
public enum Opcode {
	/*
	 * unary expression operators, unary operators(expr)
	 * 		NOR
	 * 		/
	 * 	Expr
	 */
	NOR,

	/*
	 * binary expression operators, binary operators(expr, expr)
	 * 		Opcode
	 * 		/	\
	 * 	Expr	Expr
	 */
	ANDAND,
	LT,
	ADD,
	SUB,
	MUL,

	/*
	 * array operators, ARRAY_SUBSCRIPT is a binary operator(array, subscript), while ARRAY_LENGTH is a unary operator(array)
	 * 		ARRAY_SUBSCRIPT
	 * 		/			\
	 * 	Expr			Expr
	 * 
	 * 		ARRAY_LENGTH
	 * 		/
	 * 	Expr
	 */
	ARRAY_SUBSCRIPT,
	ARRAY_LENGTH,

	/*
	 * call a method of an object, triple-ary operator(object, method, parameter list)
	 * 			CALL
	 * 		/	|	\
	 * 	Expr	id	ExprList
	 */		
	CALL,
	
	
	/*
	 * new expression operators, NEW is a unary operator(id), and NEW_ARRAY is also a unary operator(subscript)
	 * 		NEW
	 * 		/
	 * 	id
	 * 
	 * 		NEW_ARRAY
	 * 		/
	 * 	Expr
	 */
	NEW,
	NEW_ARRAY
}
