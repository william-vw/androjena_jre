/*
 *  (c) Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
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
 *
 * Seq.java
 *
 * Created on 26 July 2000, 15:24
 */

package com.hp.hpl.jena.rdf.model;


/** RDF Sequence container.
 *
 * <p>This interface defines methods for accessing RDF Sequence resources.
 * These methods operate on the RDF statements contained in a model.  The 
 * Sequence implementation may cache state from the underlying model, so
 * objects should not be added to or removed from the Sequence by directly
 * manipulating its properties, whilst the Sequence is being
 * accessed through this interface.</p>
 *
 * <p>When a member is deleted from a sequence using this interface, or an
 * iterator returned through this interface, all the other members with
 * higher ordinals are renumbered to one below what they previously were.</p>
 *
 * <p>This interface provides methods supporting typed literals.  This means
 *    that methods are provided which will translate a built in type, or an
 *    object to an RDF Literal.  This translation is done by invoking the
 *    <CODE>toString()</CODE> method of the object, or its built in equivalent.
 *    The reverse translation is also supported.  This is built in for built
 *    in types.  Factory objects, provided by the application, are used
 *    for application objects.</p>
 * <p>This interface provides methods for supporting enhanced resources.  An
 *    enhanced resource is a resource to which the application has added
 *    behaviour.  RDF containers are examples of enhanced resources built in
 *    to this package.  Enhanced resources are supported by encapsulating a
 *    resource created by an implementation in another class which adds
 *    the extra behaviour.  Factory objects are used to construct such
 *    enhanced resources.</p>
 * @author bwm
 * @version Release='$Name:  $' Revision='$Revision: 1.1 $' Date='$Date: 2009/06/29 08:55:38 $'
 */
public interface Seq extends Container {
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, RDFNode o) ;
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, boolean o) ;
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, long o) ;
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, char o) ;
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, float o) ;
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, double o) ;
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, String o) ;
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     * @param l the langauge of the value added
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, String o, String l) ;
    
    /** Insert a new member into the sequence at the specified position.
     *
     * <p>The existing member at that position, and all others with higher indexes,
     * have their index increased by one.</p>
     * @param index The index of the new member,
     * @param o The member to be added.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq add(int index, Object o) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public boolean getBoolean(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public byte getByte(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public short getShort(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public int getInt(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public long getLong(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public char getChar(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public float getFloat(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public double getDouble(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public String getString(int index) ; 
    
    /** Get the language of the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public String getLanguage(int index) ;
    
    /** Get the member at a given index.
     *
     * <p>The supplied factory object is used to create the returned object.</p>
     * @return The member at the given index.
     * @param index The index of the required member.
     * @param f The factory object used to create the returned object.
     .
     */
    @Deprecated public Resource getResource(int index, ResourceF f) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public Literal getLiteral(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public Resource  getResource(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public RDFNode getObject(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public Bag getBag(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public Alt getAlt(int index) ;
    
    /** Get the member at a given index.
     *
     * @param index The index of the required member.
     .
     * @return The member at the given index.
     */
    public Seq getSeq(int index) ;

    /** Remove the member at the specified index.
     *
     * <p>All other members with a higher index will have their index reduced by
     * one.</p>
     * @param index The index of the member to be removed.
     .
     * @return this object to enable cascading of method calls.
     */
    public Seq remove(int index) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(RDFNode o) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(boolean o) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(long o) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(char o) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(float o) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(double o) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(String o) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     * @param l the language of the member sought
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(String o, String l) ;
    
    /** Return the index of a given member of the sequence.
     *
     * <p>If more the the same value appears more than once in the sequence,
     * it is undefined which of the indexes will be returned.</p>
     *
     * <p>If the member is not found in this sequence, a value of 0 is returned.</p>
     * @param o The member sought.
     .
     * @return an index of the member in this sequence or 0 if the
     * member is not found in this sequence.
     */
    public int indexOf(Object o) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then an exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, RDFNode o) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then a Jena exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, boolean o) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then a Jena exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, long o) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then a Jena exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, char o) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then a Jena exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, float o) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then a Jena exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, double o) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then a Jena exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, String o) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then a Jena exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @param l The language of the value set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, String o, String l) ;
    
    /** Set the value at a given index in the sequence.
     *
     * <p>If the index is not in the range 1 to the size of the
     * sequence, then a Jena exception is raised.</p>
     * @param index The index whose member is to be set.
     * @param o The value to be set.
     * @throws SeqIndexBoundsException
     * @return this object to enable cascading method calls.
     */
    public Seq set(int index, Object o) ;
}

