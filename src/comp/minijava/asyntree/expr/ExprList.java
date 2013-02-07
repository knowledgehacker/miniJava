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

public class ExprList {
	private ExprNode head, tail;
	private int exprNum;
	
	public ExprList(Expr expr){
		head = tail = new ExprNode(expr);
		exprNum = 1;
	}
	
	public int size(){
		return exprNum;
	}
	
	public void add(Expr expr){
		ExprNode exprNode = new ExprNode(expr);
		tail.next = exprNode;
		tail = exprNode;
		
		++exprNum;
	}
	
	public ExprListIterator exprListIterator(){
		return new ExprListIterator();
	}
	
	// @inner class, iterator for expression list
	public class ExprListIterator{
		private ExprNode curr;
		
		public ExprListIterator(){
			curr = head;
		}
		
		public boolean hasNext(){
			return null != curr;
		}
		
		public Expr next(){
			ExprNode tmp = curr;
			curr = curr.next;
			return tmp.expr;
		}
	}

	// @inner class, expression node class
	private class ExprNode{
		public Expr expr;
		public ExprNode next;
		
		public ExprNode(Expr expr){
			this.expr = expr;
		}
	}
}
