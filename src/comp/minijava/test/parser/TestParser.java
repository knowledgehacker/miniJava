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

package comp.minijava.test.parser;

import comp.minijava.parser.Parser;

public class TestParser{
	public static void testLanguageFeatures(){
		String str1 = new String("happy new year");
		//String str2 = str1;
		String str2 = new String("happy new year");
		
		/*
		 * Remember that if both str1 and str2 are created using a same string literal. 
		 * Then are will pointer to the same memory location allocated form this string literal, this is an optimization.
		 * For example:
		 * String str1 = "happy new year";
		 * String str2 = "happy new year";
		 * str1 == str2 returns true;
		 */
		if(str1 == str2)
			System.out.println("str1 == str2");
		else
			System.out.println("str1 != str2");
	
		if(true == str1.equals(str2))
			System.out.println("true == str1.equals(str2)");
		else
			System.out.println("true != str1.equals(str2)");
		
		if(0 == str1.compareTo(str2))
			System.out.println("0 == str1.compareTo(str2)");
		else
			System.out.println("0 != str1.compareTo(str2)");
	}
	
	public static void main(String[] args){
		//testLanguageFeatures();
		
/*		
		// Test CFG
		System.out.println("CFG.ruleSet:");
		ArrayList<int[]>[] rules = CFG.ruleSet;
		for(int i = 0; i < NonTerminalType.Number; ++i){
			ArrayList<int[]> rule = rules[i];
			int ruleSize = rule.size();
			for(int j = 0; j < ruleSize; ++j){
				System.out.print(i + " -> ");
				int[] ruleXX = rule.get(j);
				for(int k = 0; k < ruleXX.length; ++k)
					System.out.print(ruleXX[k] + " ");
				System.out.println();
			}
		}
*/
	
/*		
		int NonTerminalNumber = NonTerminalType.Number;
		
		// Test nullable non-terminals
		System.out.println("CFG.nullableNonTerminals:");
		//Boolean[] nullable = CFG.nullableNonTerminals;
		boolean[] nullable = CFG.nullableNonTerminals;
		for(int i = 0; i < NonTerminalNumber-1; ++i)
			System.out.print(i + ": " + nullable[i] + ", ");
		System.out.println(NonTerminalNumber-1 + ": " + nullable[NonTerminalNumber-1]);
		
			
		// Test first/follow sets, and predict table
		System.out.println("CFG.firstSets:");
		ArrayList<Integer>[] firstSet = CFG.firstSets;
		for(int i = 0; i < NonTerminalNumber; ++i){
			System.out.print(i + " = {");
			ArrayList<Integer> first = firstSet[i];
			int firstSize = first.size();
			if(firstSize > 0){
				int j;
				for(j = 0; j < firstSize-1; ++j)
					System.out.print(first.get(j) + " ");
				System.out.print(first.get(j));
			}
			System.out.println("}");
		}

		System.out.println("CFG.followSets:");
		boolean[][] followSet = CFG.followSets;
		for(int i = 0; i < NonTerminalNumber; ++i){
			System.out.print(i + " = {");
			boolean[] follow = followSet[i];
			int followSize = follow.length;
			if(followSize > 0){	
				int j;
				for(j = 0; j < followSize-1; ++j){
					if(true == follow[j])
					System.out.print(j+TokenType.PUBLIC + ": " + follow[j] + ", ");
				}
				if(true == follow[j])
					System.out.print(j+TokenType.PUBLIC + ": " + follow[j]);
			}
			System.out.println("}");
		}
*/
	
		int argNum = args.length;
		if(argNum != 1){
			System.out.println("Usage: TestParser Factorial.ava");
			
			return;
		}

		Parser parser = new Parser(args[0]);
		parser.program();
	}		
}
