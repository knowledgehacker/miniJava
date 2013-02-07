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

import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.FileNotFoundException;

/*
 * Lexical analysis on a single miniJava source file.
 * TODO: recognize comments.
 */
public class Lexer{ 
    public String fileName;
    private LineNumberReader reader;
    private char line[];
    public int lineNo;
    public int lineOffset;

    public static final int IDEN_MAX_CHARS = 256;
    public static final char SPACE = ' ';
	public static final char TAB4 = '\t';

	public Lexer(String fileName){
        this.fileName = fileName;
        FileReader filereader = null;
        try{
            filereader = new FileReader(fileName);
        }catch(FileNotFoundException fnfe){
            System.out.println("Create FileReader from file " + fileName + "failed: " + fnfe.toString());
        }
        reader = new LineNumberReader(filereader);
		
		line = null;
		lineNo = 0;
		lineOffset = 0;
    }

	private boolean nextLine(){
       try{
			String lineRead = reader.readLine();
			if(null == lineRead){
				System.out.println("Reach the end of the stream when reading from file " + fileName);
				reader.close();

				return false;
			}

            line = lineRead.toCharArray();
        }catch(IOException ioe){
            System.out.println("Read a line from file " + fileName + "failed: " + ioe.toString());
        }
        ++lineNo;
        lineOffset = 0;

		System.out.println(">line " + lineNo + ":" + new String(line));	

		return true;
	}

	private void skipSpaces(){
		if(line.length != 0){
			while((lineOffset < line.length) && ((line[lineOffset] == SPACE) || (line[lineOffset] == TAB4)))
				++lineOffset;	
		}
	}

    public Token nextToken() throws IOException {
		Token token = null;

		if(null == line){
			if(false == nextLine())
				return null;
		}

		skipSpaces();
		// "lineOffset == line.length" indicates current line has been analyzed, read the next line
        while(lineOffset == line.length){
			if(false == nextLine())
				return null;

			// determine whether the line read is a blank line or not
			skipSpaces();
        }
		//System.out.println("lineOffset = " + lineOffset + ", line[lineOffset] = " + line[lineOffset]);

        char c = line[lineOffset];
        if(isLetter(c)){
            switch(c){
			// boolean
            case 'b':
                if(line[lineOffset + 1] == 'o' && line[lineOffset + 2] == 'o' && line[lineOffset + 3] == 'l' 
					&& line[lineOffset + 4] == 'e' && line[lineOffset + 5] == 'a' && line[lineOffset + 6] == 'n'){
                    token = new KeywordToken(TokenType.BOOLEAN, "boolean");
                    lineOffset += 7;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// class
            case 'c':
                if(line[lineOffset + 1] == 'l' && line[lineOffset + 2] == 'a' && line[lineOffset + 3] == 's' 
					&& line[lineOffset + 4] == 's'){
                    token = new KeywordToken(TokenType.CLASS, "class");
                    lineOffset += 5;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// extends, else
            case 'e':
                if(line[lineOffset + 1] == 'x'){
                    if(line[lineOffset + 2] == 't' && line[lineOffset + 3] == 'e' && line[lineOffset + 4] == 'n' 
						&& line[lineOffset + 5] == 'd' && line[lineOffset + 6] == 's'){
                        token = new KeywordToken(TokenType.EXTENDS, "extends");
                        lineOffset += 7;
                        break;
                    }
                } else if(line[lineOffset + 1] == 'l' && line[lineOffset + 2] == 's' && line[lineOffset + 3] == 'e'){
                    token = new KeywordToken(TokenType.ELSE, "else");
                    lineOffset += 4;
                    break;
                }

                token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// false
            case 'f':
                if(line[lineOffset + 1] == 'a' && line[lineOffset + 2] == 'l' && line[lineOffset + 3] == 's' 
					&& line[lineOffset + 4] == 'e'){
                    token = new KeywordToken(TokenType.FALSE, "false");
                    lineOffset += 5;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// if, int
            case 'i':
                if(line[lineOffset + 1] == 'f'){
                    token = new KeywordToken(TokenType.IF, "if");
                    lineOffset += 2;
                } else if(line[lineOffset + 1] == 'n' && line[lineOffset + 2] == 't'){
                    token = new KeywordToken(TokenType.INT, "int");
                    lineOffset += 3;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// length
            case 'l': // 'l'
                if(line[lineOffset + 1] == 'e' && line[lineOffset + 2] == 'n' && line[lineOffset + 3] == 'g' 
					&& line[lineOffset + 4] == 't' && line[lineOffset + 5] == 'h'){
                    token = new KeywordToken(TokenType.LENGTH, "length");
                    lineOffset += 6;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// main
            case 'm':
                if(line[lineOffset + 1] == 'a' && line[lineOffset + 2] == 'i' && line[lineOffset + 3] == 'n'){
                    token = new KeywordToken(TokenType.MAIN, "main");
                    lineOffset += 4;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

            // new
			case 'n':
                if(line[lineOffset + 1] == 'e' && line[lineOffset + 2] == 'w'){
                    token = new KeywordToken(TokenType.NEW, "new");
                    lineOffset += 3;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// print, public
            case 'p':
                if(line[lineOffset + 1] == 'r'){
                    if(line[lineOffset + 2] == 'i' && line[lineOffset + 3] == 'n' && line[lineOffset + 4] == 't'){
                        token = new KeywordToken(TokenType.PRINT, "print");
                        lineOffset += 5;
                        break;
                    }
                } else if(line[lineOffset + 1] == 'u' && line[lineOffset + 2] == 'b' && line[lineOffset + 3] == 'l' 
					&& line[lineOffset + 4] == 'i' && line[lineOffset + 5] == 'c'){
                    token = new KeywordToken(TokenType.PUBLIC, "public");
                    lineOffset += 6;
                    break;
                }

                token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// return
            case 'r':
                if(line[lineOffset + 1] == 'e' && line[lineOffset + 2] == 't' && line[lineOffset + 3] == 'u'
					&& line[lineOffset + 4] == 'r' && line[lineOffset + 5] == 'n'){
                    token = new KeywordToken(TokenType.RETURN, "return");
                    lineOffset += 6;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

		
			// this, true
            case 't':
                if(line[lineOffset + 1] == 'h'){
                    if(line[lineOffset + 2] == 'i' && line[lineOffset + 3] == 's'){
                        token = new KeywordToken(TokenType.THIS, "this");
                        lineOffset += 4;
                        break;
                    }
                } else if(line[lineOffset + 1] == 'r' && line[lineOffset + 2] == 'u' && line[lineOffset + 3] == 'e'){
                    token = new KeywordToken(TokenType.TRUE, "true");
                    lineOffset += 4;
                    break;
                }
                token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// void
            case 'v':
                if(line[lineOffset + 1] == 'o' && line[lineOffset + 2] == 'i' && line[lineOffset + 3] == 'd'){
                    token = new KeywordToken(TokenType.VOID, "void");
                    lineOffset += 4;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

			// while
            case 'w':
                if(line[lineOffset + 1] == 'h' && line[lineOffset + 2] == 'i' && line[lineOffset + 3] == 'l' 
					&& line[lineOffset + 4] == 'e'){
                    token = new KeywordToken(TokenType.WHILE, "while");
                    lineOffset += 5;
                } else
                    token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;

            default:
                token = new IdentifierToken(TokenType.IDENTIFIER, recogIden());
                break;
            }
        }else if(isDigit(c)){
            int digit = c - '0';
			while(isDigit(c = line[++lineOffset]))
				digit = digit * 10 + c - '0';
        
		    token = new IntegerLiteralToken(digit);
        } else if(isOperator(c)){
            char op[] = {c};
            switch(c){
	            case '!':
    	            token = new OperatorToken(TokenType.NOR, op);
        	        break;

            	case '&':
					{
	                	c = line[++lineOffset];
		                if(c == '&'){
							char andAndOp[] = {'&', '&'};
    		                token = new OperatorToken(TokenType.ANDAND, andAndOp);
						}
        		        else
            		        System.out.println("Illegal operator &" + c+ " in file "+ fileName + ":" + lineNo + ", " + lineOffset);
                		break;
					}
	            case '=':
    	            token = new OperatorToken(TokenType.ASSGN, op);
        	        break;

            	case '<':
                	token = new OperatorToken(TokenType.LT, op);
	                break;

    	        case '+':
        	        token = new OperatorToken(TokenType.ADD, op);
            	    break;

	            case '-':
    	            token = new OperatorToken(TokenType.SUB, op);
        	        break;

            	case '*':
                	token = new OperatorToken(TokenType.MUL, op);
	                break;

    	        case ',':
        	        token = new OperatorToken(TokenType.COMMA, op);
            	    break;

	            case '.':
    	            token = new OperatorToken(TokenType.DOT, op);
        	        break;
            }
            lineOffset++;
        } else if(isDelimiter(c)){
            switch(c){
            case '{':
                token = new DelimiterToken(TokenType.LBRACE, c);
                break;

            case '}':
                token = new DelimiterToken(TokenType.RBRACE, c);
                break;

            case '(':
                token = new DelimiterToken(TokenType.LPAREN, c);
                break;

            case ')':
                token = new DelimiterToken(TokenType.RPAREN, c);
                break;

            case ';':
                token = new DelimiterToken(TokenType.SEMICOLON, c);
                break;

            case '[':
                token = new DelimiterToken(TokenType.LSQBRACE, c);
                break;

            case ']':
                token = new DelimiterToken(TokenType.RSQBRACE, c);
                break;
            }
            lineOffset++;
        }else
            System.out.println("Illegal character \'" + c + "\' in file " + fileName +  ":" + lineNo + ", " + lineOffset);

        return token;
    }

    private String recogIden(){
        char ac[] = new char[IDEN_MAX_CHARS];
        ac[0] = line[lineOffset++];

		int i = 1;
		while(isLetter(line[lineOffset]) || isDigit(line[lineOffset]) || (line[lineOffset] == '_'))
			ac[i++] = line[lineOffset++];

        return new String(ac, 0, i);
    }

    private boolean isLetter(char c){
        return 'A' <= c && 'Z' >= c || 'a' <= c && 'z' >= c;
    }

    private boolean isDigit(char c){
        return '0' <= c && '9' >= c;
    }

    private boolean isDelimiter(char c){
        return c == '{' || c == '}' || c == '(' || c == ')' || c == ';'
			|| c == '[' || c == ']';
    }

    private boolean isOperator(char c){
        return c == '!' || c == '=' || c == '&' || c == '<' || c == '+' 
			|| c == '-' || c == '*' || c == ',' || c == '.';
    }
}
