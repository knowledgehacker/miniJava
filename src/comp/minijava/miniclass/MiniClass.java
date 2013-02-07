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

package comp.minijava.miniclass;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;

import comp.minijava.type.BaseType;
import comp.minijava.type.FuncType;
import comp.minijava.symbol.Symbol;
import comp.minijava.symbol.FuncSymbol;
import comp.minijava.asyntree.stmt.Stmt;

/*
 * We use one symbol table to store all variables and functions in a class.
 */
public class MiniClass{
	private String className;	
	private HashMap<String, Symbol> fields;
	private HashMap<String, Method> methods;
	
	// link to className's super class's symbol table, if className has one.
	private MiniClass parent;

	public MiniClass(String className){
		this.className = className;
		fields = new HashMap<String, Symbol>();
		methods = new HashMap<String, Method>();
	}

	/**
	 * Get name of the class.
	 * @return
	 */
	public String getClassName(){
		return className;
	}

	public MiniClass getParent(){
		return parent;
	}
	
	public void setParent(MiniClass parent){
		this.parent = parent;
	}
	
	/**
	 * Get name list of methods in the class.
	 * @return
	 */
	public Set<String> getMethodNameList(){
		return methods.keySet();
	}

	public void addSymbolToFields(Symbol sym){
		fields.put(sym.name, sym);
	}

	public Symbol findSymbolInFields(String idName){
		Symbol sym = fields.get(idName);
		if(null != sym)
			return sym;
		
		/*
		 * Tip: access control acts on per-class instead of per-object scope in Java, so we can access to private parent field in object symTab here
		 * If access control acts on per-object scope, then we can't access to private parent field in object symTab, since symTab is another object. 
		 */
		MiniClass symTab = parent;
		while(null != symTab){
			sym = symTab.fields.get(idName);
			if(null != sym)
				return sym;
			else
				symTab = symTab.parent;
		}
		
		return null;
	}

	public void createMethodSymbol(FuncSymbol methodSymbol, ArrayList<Symbol> params){
		Method entry = new Method(methodSymbol);
		if(null != params){
			ListIterator<Symbol> iter = params.listIterator();
			while(iter.hasNext())
				entry.add(iter.next());
		}
		methods.put(methodSymbol.name, entry);
	}
	
	public FuncSymbol getMethodSymbol(String methodName){
		return methods.get(methodName).methodSymbol;
	}
	
	
	public void addSymbolToMethods(String methodName, Symbol sym){
		methods.get(methodName).add(sym);
	}

	public Symbol findSymbolInMethods(String methodName, String idName){
		Symbol sym = findSymbolInFields(idName);
		if(null == sym)
			sym = methods.get(methodName).find(idName);
		
		return sym;
	}
	
	public void addMethodBody(String methodName, Stmt stmt){
		Method method = methods.get(methodName);
		method.stmt = stmt;
	}
	
	public Stmt getMethodBody(String methodName){
		return methods.get(methodName).stmt;
	}
	
	public void inspect(){
		// show class name
		System.out.println("Class: " + className);
		// show fields
		System.out.println("Fields:");
		Set<String> fieldNameList = fields.keySet();
		Iterator<String> fieldIter = fieldNameList.iterator();
		while(fieldIter.hasNext()){
			String fieldName = fieldIter.next();
			System.out.println(fieldName + ": " + fields.get(fieldName));
		}
					
		// show methods
		System.out.println("Methods:");
		Set<String> methodNameList = methods.keySet();
		Iterator<String> methodIter = methodNameList.iterator();
		while(methodIter.hasNext()){
			String methodName = methodIter.next();
			Method method = methods.get(methodName);
			FuncSymbol sym = method.methodSymbol;
			FuncType type = (FuncType)sym.type;
			System.out.print(type.retType + " ");
			System.out.print(methodName + "(");
			if(null != type.paramTypeList){
				BaseType[] paramTypeList = type.paramTypeList;
				int paramNum = paramTypeList.length;
				int k;
				for(k = 0; k < paramNum-1; ++k)
					System.out.print(paramTypeList[k] + ", ");
				System.out.print(paramTypeList[paramNum-1]);
			}
			System.out.println(")");			
			System.out.println("Parameters and local variables:");
			method.dumpParametersAndLocalVariables();
			System.out.println("Method Body:");
			Stmt methodBody = getMethodBody(methodName);
			while(null != methodBody){
				System.out.println(methodBody);
				methodBody = methodBody.next;
			}
		}	
	}
	
	/**
	 * inner class Method
	 * class Method contains all necessary information related to a method in the class,
	 * including method signature, parameters and local variables, and statements in the method body.
	 * @author LM
	 */
	private class Method{
		// method signature
		private FuncSymbol methodSymbol;
		// parameters and local variables symbols
		private final static int BUCKET_SIZE = 131;
		private LinkedList<Symbol>[] buckets;
		// method body
		private Stmt stmt;

		@SuppressWarnings("unchecked")
		public Method(FuncSymbol methodSymbol){
			this.methodSymbol = methodSymbol;

			/*
			 * Attention: I can't create a generic array using the commented statement below.
			 * It's very strange, it seems to be a flaw/feature of java generics.
			 */ 
			//buckets = new LinkedList<Symbol>[BUCKET_SIZE];
			buckets = (LinkedList<Symbol>[])new LinkedList[BUCKET_SIZE];
			
			for(int i = 0; i < BUCKET_SIZE; ++i)
				buckets[i] = new LinkedList<Symbol>();
		}
		
		private int hash(String identifier){
			//TODO: implement an efficient hash function
			int idx = 0;
			int len = identifier.length();
			for(int i = 0; i < len; ++i)
				idx += identifier.charAt(i) * (i+1);
			idx %= BUCKET_SIZE;
			
			return idx;
		}

		private void add(Symbol sym){
			int idx = hash(sym.name);
			buckets[idx].add(sym);
		}

		public Symbol find(String symName){
			int idx = hash(symName);
			if(null == buckets[idx])
				return null;

			/*
			 * Search symbol in buckets. 
			 * We don't need to search symbol in methodSymbol.parameterList, since all parameter symbols also exist in buckets.
			 */
			Symbol sym = null;
			ListIterator<Symbol> localVarIter = buckets[idx].listIterator();
			while(localVarIter.hasNext()){
				Symbol localVar = localVarIter.next();
				if(symName.equals(localVar.name)){
					sym = localVar;
					break;
				}
			}
			
			return sym;
		}
		
		private void dumpParametersAndLocalVariables(){
			for(int i = 0; i < BUCKET_SIZE; ++i){
				if(false == buckets[i].isEmpty()){
					ListIterator<Symbol> iter = buckets[i].listIterator();
					while(iter.hasNext()){
						Symbol sym = iter.next();
						System.out.println(sym.name + ": " + sym.type);
					}
				}
			}
		}
	}
}
