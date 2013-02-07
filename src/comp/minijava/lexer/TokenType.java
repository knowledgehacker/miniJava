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

package comp.minijava.lexer;

/*
public enum TokenType{
	// keywords
    PUBLIC, CLASS, EXTENDS,
    IF, ELSE, WHILE, RETURN,
    PRINT, LENGTH, MAIN,
    INT, BOOLEAN, VOID,
    TRUE, FALSE,
	THIS, NEW,

	// operators
	NOR,
	ASSGN, ANDAND, LT, ADD, SUB, MUL, COMMA, DOT,

	// delimiters
	LPAREN, RPAREN, LBRACKET, RBRACKET, SEMICOLON, LSQBRACKET, RSQBRACKET,

	// identifiers
	IDENTIFIER,

	// integer literals
	INTEGER_LITERAL
} 
*/

public interface TokenType{
	// keywords
	public final static int PUBLIC = 200;
	public final static int CLASS = 201;
	public final static int EXTENDS = 202;
	public final static int IF = 203;
	public final static int ELSE = 204;
	public final static int WHILE = 205;
	public final static int RETURN = 206;
	public final static int PRINT = 207;
	public final static int LENGTH = 208;
	public final static int MAIN = 209;
	public final static int INT = 210;
	public final static int BOOLEAN = 211;
	public final static int VOID = 212;
	public final static int TRUE = 213;
	public final static int FALSE = 214;
	public final static int THIS = 215;
	public final static int NEW = 216;

	// operators
	public final static int NOR = 217;
	public final static int ASSGN = 218;
	public final static int ANDAND = 219;
	public final static int LT = 220;
	public final static int ADD = 221;
	public final static int SUB = 222;
	public final static int MUL = 223;
	public final static int COMMA = 224;	// is COMMA a operator or delimiter???
	public final static int DOT = 225;

	// delimiters
	public final static int LPAREN = 226;		// (
	public final static int RPAREN = 227;		// )
	public final static int LBRACE = 228;		// {
	public final static int RBRACE = 229;		// }
	public final static int SEMICOLON = 230;	// ;
	public final static int LSQBRACE = 231;		// [
	public final static int RSQBRACE = 232;		// ]

	// identifiers
	public final static int IDENTIFIER = 233;

	// integer literals
	public final static int INTEGER_LITERAL = 234;

	// epsilon, a placeholder used in parsing context free grammar
	public final static int EPSILON = 235;
}
