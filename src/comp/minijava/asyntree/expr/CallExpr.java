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

public class CallExpr extends Expr {
	public Opcode op;
	public Expr object;
	public IdentifierExpr method;
	/*
	 * parameters is optional, since some method doesn't have any parameter.
	 * it will be filled later if any parameter exists.
	 */
	public ExprList parameters;
	
	public CallExpr(Cordinate cord, Opcode op, Expr object, IdentifierExpr method){
		super(cord);
		
		this.op = op;
		this.object = object;
		this.method = method;
	}
	
	public String toString(){
		String resp = object.toString() + " " + op + " " + method.toString() + "(";
		if(null != parameters){
			ExprList.ExprListIterator iter = parameters.exprListIterator();
			Expr firstParam = iter.next();
			resp += firstParam.toString();
			while(iter.hasNext())
				resp += ", " + iter.next();
		}
		resp += ")";
		
		return resp;
	}
}
