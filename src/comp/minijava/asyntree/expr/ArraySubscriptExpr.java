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

import comp.minijava.asyntree.Cordinate;

/*
 * CFG for miniJava indicate array in array subscript expression "Expr[Expr]" can be any expression.
 * I think array can only be an IdentifierExpr.
 * But to impose such constraint is responsibility of semantics analysis instead of syntax analysis. 
 */
public class ArraySubscriptExpr extends Expr {
	public Opcode op;
	public Expr array;
	public Expr subscript;

	public ArraySubscriptExpr(Cordinate cord, Opcode op, Expr array, Expr subscript){
		super(cord);
		
		this.op = op;
		this.array = array;
		this.subscript = subscript;
	}
	
	public String toString(){
		return array.toString() + " " + op + " " + subscript.toString();
	}
}
