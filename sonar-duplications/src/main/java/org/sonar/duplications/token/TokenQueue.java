/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2008-2011 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.duplications.token;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class TokenQueue implements Iterable<Token> {

  private final LinkedList<Token> tokenQueue;

  public TokenQueue(List<Token> tokenList) {
    tokenQueue = new LinkedList<Token>(tokenList);
  }

  public TokenQueue() {
    tokenQueue = new LinkedList<Token>();
  }

  /**
   * Retrieves, but does not remove, token from this queue.
   * 
   * @return token from this queue, or <tt>null</tt> if this queue is empty.
   */
  public Token peek() {
    return tokenQueue.peek();
  }

  /**
   * Retrieves and removes token from this queue.
   * 
   * @return token from this queue, or <tt>null</tt> if this queue is empty.
   */
  public Token poll() {
    return tokenQueue.poll();
  }

  public int size() {
    return tokenQueue.size();
  }

  public void add(Token token) {
    tokenQueue.addLast(token);
  }

  public boolean isNextTokenValue(String expectedValue) {
    Token nextToken = tokenQueue.peek();
    if (nextToken == null) {
      // queue is empty
      return false;
    }
    return nextToken.getValue().equals(expectedValue);
  }

  public Iterator<Token> iterator() {
    return tokenQueue.iterator();
  }

  public void pushForward(List<Token> matchedTokenList) {
    ListIterator<Token> iter = matchedTokenList.listIterator(matchedTokenList.size());
    while (iter.hasPrevious()) {
      tokenQueue.addFirst(iter.previous());
    }
  }

}
