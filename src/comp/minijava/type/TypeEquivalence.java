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

package comp.minijava.type;

import comp.minijava.miniclass.MiniClass;
import comp.minijava.miniclass.MiniClassManager;

public class TypeEquivalence {
	/*
	 * Attention: order of two types to check is important when calling check here.
	 * As to the case type1 and type2 are both ObjectType.
	 * They are regarded as equivalent if and only if type2 is a sub-type of type1 considering class inheritance.
	 */
	public static boolean check(BaseType type1, BaseType type2){
		boolean equivalent = true;
		
		if((type1 instanceof PrimitiveType) && (type2 instanceof PrimitiveType)){
			PrimitiveType concreteType1 = (PrimitiveType)type1;
			PrimitiveType concreteType2 = (PrimitiveType)type2;
			
			if(concreteType1.type != concreteType2.type)
				equivalent = false;
		}else if((type1 instanceof FuncType) && (type2 instanceof FuncType)){
			FuncType concreteType1 = (FuncType)type1;
			FuncType concreteType2 = (FuncType)type2;
			
			// function type compatibility does not take return type into account 
			BaseType[] paramTypeList1 = concreteType1.paramTypeList;
			BaseType[] paramTypeList2 = concreteType2.paramTypeList;
				
			int paramNum1 = paramTypeList1.length;
			int paramNum2 = paramTypeList2.length;
			if(paramNum1 != paramNum2)
				equivalent = false;
			else{
				for(int i = 0; i < paramNum1; ++i)
					if(false == check(paramTypeList1[i], paramTypeList2[i])){
						equivalent = false;
						break;
					}
			}
		}else if((type1 instanceof ObjectType) && (type2 instanceof ObjectType)){
			ObjectType concreteType1 = (ObjectType)type1;
			ObjectType concreteType2 = (ObjectType)type2;
			
			String className1 = concreteType1.className;
			String className2 = concreteType2.className;
			MiniClass miniClass1 = MiniClassManager.find(className1);
			MiniClass miniClass2 = MiniClassManager.find(className2);
			if((null == miniClass1) || (null == miniClass2)){
				if(null == miniClass1)
					System.out.println("Class " + className1 + "is undeclared!");
				
				if(null == miniClass2)
					System.out.println("Class " + className2 + "is undeclared!");
				
				equivalent = false;
			}else{
				while(null != miniClass2){
					if(className2.equals(className1))
						break;
					else{
						miniClass2 = miniClass2.getParent();
						className2 = miniClass2.getClassName();
					}
				}
				if(null == miniClass2)
					equivalent = false;
			}
		}
		
		return equivalent;
	}
}
