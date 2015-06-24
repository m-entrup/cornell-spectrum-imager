/*
 * [LETest.java]
 *
 * Summary: Tests Little Endian LEDataInputStream and LEDataOutputStream.
 *
 * Copyright: (c) 1998-2015 Roedy Green, Canadian Mind Products, http://mindprod.com
 *
 * Licence: This software may be copied and used freely for any purpose but military.
 *          http://mindprod.com/contact/nonmil.html
 *
 * Requires: JDK 1.7+
 *
 * Created with: JetBrains IntelliJ IDEA IDE http://www.jetbrains.com/idea/
 *
 * Version History:
 *  1.0 1998-01-06
 *  1.1 1998-01-07 officially implements DataInput
 *  1.2 1998-01-09 add LERandomAccessFile
 *  1.3 1998-08-27
 *  1.4 1998-11-10 add new address and phone.
 *  1.5 1999-10-08 use com.mindprod.ledatastream package name.
 *  1.6 2005-06-13 made readLine deprecated
 *  1.7 2007-01-01
 *  1.8 2007-05-24 add pad, icon, pass Intellij inspector
 */
package com.mindprod.ledatastream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.*;

/**
 * Tests Little Endian LEDataInputStream and LEDataOutputStream.
 * <p/>
 * Demonstrates the use of its methods. Output should look like this: 44 -1 a -249 -123456789012 -649 -749 true 3.14
 * 4.14 echidna kangaroo u dingo Then repeated.
 *
 * @author Roedy Green, Canadian Mind Products
 * @version 1.8 2007-05-24
 * @since 1998-01-06
 */
public final class LETest {
    /**
     * Debug harness.
     *
     * @param args
     *            not used
     */
    public static void main(String[] args) {
	// Write little-endian binary data into a sequential file
	// O P E N
	FileOutputStream fos;
	try {
	    fos = new FileOutputStream("C:/temp/temp.dat", false
	    /* append */);
	} catch (IOException e) {
	    out.println("Unexpected IOException opening LEDataOutputStream");
	    return;
	}
	LEDataOutputStream ledos = new LEDataOutputStream(fos);
	// W R I T E
	try {
	    ledos.writeByte((byte) 44);
	    ledos.writeByte((byte) 0xff);
	    ledos.writeChar('a');
	    ledos.writeInt(-249);
	    ledos.writeLong(-123456789012L);
	    ledos.writeShort((short) -649);
	    ledos.writeShort((short) -749);
	    ledos.writeBoolean(true);
	    ledos.writeDouble(3.14D);
	    ledos.writeFloat(4.14F);
	    ledos.writeUTF("echidna");
	    ledos.writeBytes("kangaroo"/* string -> LSB 8-bit */);
	    ledos.writeChars("dingo"/* string 16-bit Unicode */);
	} catch (IOException e) {
	    out.println("Unexpected IOException writing LEDataOutputStream");
	    return;
	}
	// C L O S E
	try {
	    ledos.close();
	} catch (IOException e) {
	    out.println("Unexpected IOException closing LEDataOutputStream");
	    return;
	}
	// Read little-endian binary data from a sequential file
	// import java.io.*;
	// O P E N
	FileInputStream fis;
	try {
	    fis = new FileInputStream("C:/temp/temp.dat");
	} catch (FileNotFoundException e) {
	    out.println("Unexpected IOException opening LEDataInputStream");
	    return;
	}
	LEDataInputStream ledis = new LEDataInputStream(fis);
	// R E A D
	try {
	    byte b = ledis.readByte();
	    out.println(b);
	    byte ub = (byte) ledis.readUnsignedByte();
	    out.println(ub);
	    char c = ledis.readChar();
	    out.println(c);
	    int j = ledis.readInt();
	    out.println(j);
	    long l = ledis.readLong();
	    out.println(l);
	    short ii = ledis.readShort();
	    out.println(ii);
	    short us = (short) ledis.readUnsignedShort();
	    out.println(us);
	    boolean q = ledis.readBoolean();
	    out.println(q);
	    double d = ledis.readDouble();
	    out.println(d);
	    float f = ledis.readFloat();
	    out.println(f);
	    String u = ledis.readUTF();
	    out.println(u);
	    byte[] ba = new byte[8];
	    ledis.readFully(ba, 0/* offset in ba */, ba.length/*
							       * bytes to read
							       */);
	    out.println(new String(ba));
	    /* there is no readChars method */
	    c = ledis.readChar();
	    out.print(c);
	    c = ledis.readChar();
	    out.print(c);
	    c = ledis.readChar();
	    out.print(c);
	    c = ledis.readChar();
	    out.print(c);
	    c = ledis.readChar();
	    out.println(c);
	} catch (IOException e) {
	    out.println("Unexpected IOException reading LEDataInputStream");
	    return;
	}
	// C L O S E
	try {
	    ledis.close();
	} catch (IOException e) {
	    out.println("Unexpected IOException closing LEDataInputStream");
	    return;
	}
	// Write little endian data to a random access files
	// O P E N
	LERandomAccessFile leraf;
	try {
	    leraf = new LERandomAccessFile("C:/temp/rand.dat", "rw"
	    /* read/write */);
	} catch (IOException e) {
	    out.println("Unexpected IOException creating LERandomAccessFile");
	    return;
	}
	try {
	    // W R I T E
	    leraf.seek(0/* byte offset in file */);
	    leraf.writeByte((byte) 44);
	    leraf.writeByte((byte) 0xff);
	    leraf.writeChar('a');
	    leraf.writeInt(-249);
	    leraf.writeLong(-123456789012L);
	    leraf.writeShort((short) -649);
	    leraf.writeShort((short) -749);
	    leraf.writeBoolean(true);
	    leraf.writeDouble(3.14D);
	    leraf.writeFloat(4.14F);
	    leraf.writeUTF("echidna");
	    leraf.writeBytes("kangaroo"/* string -> LSB 8-bit */);
	    leraf.writeChars("dingo"/* string 16-bit Unicode */);
	    leraf.seek(0/* byte offset in file */);
	} catch (IOException e) {
	    out.println("Unexpected IOException writing LERandomAccessFile");
	    return;
	}
	try {
	    // R E A D
	    byte b = leraf.readByte();
	    out.println(b);
	    byte ub = (byte) leraf.readUnsignedByte();
	    out.println(ub);
	    char c = leraf.readChar();
	    out.println(c);
	    int j = leraf.readInt();
	    out.println(j);
	    long l = leraf.readLong();
	    out.println(l);
	    short ss = leraf.readShort();
	    out.println(ss);
	    short us = (short) leraf.readUnsignedShort();
	    out.println(us);
	    boolean q = leraf.readBoolean();
	    out.println(q);
	    double d = leraf.readDouble();
	    out.println(d);
	    float f = leraf.readFloat();
	    out.println(f);
	    String u = leraf.readUTF();
	    out.println(u);
	    byte[] ba = new byte[8];
	    leraf.readFully(ba, 0/* offset in ba */, ba.length/*
							       * bytes to read
							       */);
	    out.println(new String(ba));
	    /* there is no readChars method */
	    c = leraf.readChar();
	    out.print(c);
	    c = leraf.readChar();
	    out.print(c);
	    c = leraf.readChar();
	    out.print(c);
	    c = leraf.readChar();
	    out.print(c);
	    c = leraf.readChar();
	    out.println(c);
	} catch (IOException e) {
	    out.println("Unexpected IOException reading LERandomAccessFile");
	    return;
	}
	// C L O S E
	try {
	    leraf.close();
	} catch (IOException e) {
	    out.println("Unexpected IOException closing LERandomAccessFile");
	}
    } // end main
}
