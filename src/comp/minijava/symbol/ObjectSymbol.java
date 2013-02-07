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

package comp.minijava.symbol;

import comp.minijava.type.ObjectType;

/*
 * A object variable has a declared class type and a runtime class type.
 * Ex. 
 * 	DeclaredClassType obj;
 * 	obj = new RuntimeClassType();
 * DeclaredClassType is obj's declared class type, while RuntimeClassType is obj's runtime class type.
 * Of course, RuntimeClassType should be sub-class of DeclaredClassType.
 * 
 * Declared class type is encoded in symbol's type(ObjectType), while runtime class type is encoded in symbol's value.
 * 
 * type: ObjectType(declared class name)
 * value: runtime class name
 */
public class ObjectSymbol extends Symbol {
	public ObjectSymbol(String symName, String className){
		super(symName, new ObjectType(className));
	}
}
