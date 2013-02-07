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
import java.util.ListIterator;

public class MiniClassManager {
	private static ArrayList<MiniClass> classes = new ArrayList<MiniClass>();
	
	public static void add(MiniClass oneClass){
		classes.add(oneClass);
	}
	
	public static MiniClass find(String className){
		ListIterator<MiniClass> iter = classes.listIterator();
		while(iter.hasNext()){
			MiniClass currClass = iter.next();
			/*
			 * Tip: Semantic differences among str1 == str2, str1.equals(str2), and str1.compareTo(str2).
			 * Please refer to chapter 15, section 15.21 "Equality Operators", <<Java Language Sepcification, 3rd edition>>, to get the semantics of equality operators.
			 * As to semantics of equals and compareTo, please refer to link, http://docs.oracle.com/javase/6/docs/api/java/lang/Comparable.html
			 * Virtually all Java core classes that implement Comparable have natural orderings that are consistent with equals. 
			 * One exception is java.math.BigDecimal, whose natural ordering equates BigDecimal objects with equal values and different precisions (such as 4.0 and 4.00).
			 */
			if(currClass.getClassName().equals(className)){
				return currClass;
			}
		}
		
		return null;
	}
	
	public static ArrayList<MiniClass> getClasses(){
		return classes;
	}
	
	public static void inspect(){
		ListIterator<MiniClass> iter = classes.listIterator();
		while(iter.hasNext())
			iter.next().inspect();
	}
}
