/*
 * #%L
 * Image encoding and decoding routines.
 * %%
 * Copyright (C) 2005 - 2017 Open Microscopy Environment:
 *   - Board of Regents of the University of Wisconsin-Madison
 *   - Glencoe Software, Inc.
 *   - University of Dundee
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package ome.codecs;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

import loci.common.ByteArrayHandle;
import loci.common.RandomAccessInputStream;
import ome.codecs.CodecException;

/**
 * This class implements ZLIB decompression.
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
public class ZlibCodec extends BaseCodec {

  /* @see Codec#compress(byte[], CodecOptions) */
  @Override
  public byte[] compress(byte[] data, CodecOptions options)
    throws CodecException
  {
    if (data == null || data.length == 0)
      throw new IllegalArgumentException("No data to compress");
    Deflater deflater = new Deflater();
    deflater.setInput(data);
    deflater.finish();
    byte[] buf = new byte[8192];
    ByteVector bytes = new ByteVector();
    int r = 0;
    // compress until eof reached
    while ((r = deflater.deflate(buf, 0, buf.length)) > 0) {
      bytes.add(buf, 0, r);
    }
    return bytes.toByteArray();
  }

  /* @see Codec#decompress(RandomAccessInputStream, CodecOptions) */
  @Override
  public byte[] decompress(RandomAccessInputStream in, CodecOptions options)
    throws CodecException, IOException
  {
    InflaterInputStream i = new InflaterInputStream(in);
    ByteArrayHandle bytes = new ByteArrayHandle();
    byte[] buf = new byte[8192];
    int r = 0;
    // read until eof reached
    try {
      while ((r = i.read(buf, 0, buf.length)) > 0) bytes.write(buf, 0, r);
    }
    catch (EOFException e) { }
    ByteBuffer decompressed = bytes.getByteBuffer();
    byte[] rtn = new byte[decompressed.position()];
    decompressed.position(0);
    decompressed.get(rtn);
    return rtn;
  }

}
