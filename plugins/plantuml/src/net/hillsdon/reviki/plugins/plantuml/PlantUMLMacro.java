/**
 * Copyright 2008 Matthew Hillsdon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hillsdon.reviki.plugins.plantuml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import net.hillsdon.reviki.vc.PageInfo;
import net.hillsdon.reviki.wiki.renderer.macro.Macro;
import net.hillsdon.reviki.wiki.renderer.macro.ResultFormat;
import org.apache.commons.io.Charsets;

public class PlantUMLMacro implements Macro {

  public String getName() {
    return "plantuml";
  }

  public String handle(final PageInfo page, final String remainder) {
    try {
      final String slug = encode(deflate(remainder));
      return "{{http://www.plantuml.com/plantuml/svg/" + slug + "}}";
    } catch (IOException e) {
      return "** FAILED TO RENDER PLANTUML DIAGRAM **";
    }
  }

  public ResultFormat getResultFormat() {
    return ResultFormat.WIKI;
  }

  private static byte[] deflate(String text) throws IOException {
    final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);

    final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    final DeflaterOutputStream out = new DeflaterOutputStream(bytes, deflater);
    out.write(text.getBytes(Charsets.UTF_8));
    out.flush();
    out.close();
    bytes.flush();

    return bytes.toByteArray();
  }

  private static String encode(byte[] deflated) {
    final StringBuilder result = new StringBuilder();
    for(int i=0;i<deflated.length;i+=3) {
      if(i+1==deflated.length) {
        appendEncoded3Bytes(result, deflated[i], (byte)0, (byte)0);
      }
      else if(i+2==deflated.length) {
        appendEncoded3Bytes(result, deflated[i], deflated[i+1], (byte)0);
      }
      else {
        appendEncoded3Bytes(result, deflated[i], deflated[i+1],deflated[i+2]);
      }
    }

    return result.toString();
  }

  private static void appendEncoded3Bytes(final StringBuilder result, final byte b1, final byte b2, final byte b3) {
    final int c1 = b1 >>> 2;
    final int c2 = ((b1 & 0x3) << 4) | ((b2 & 0xff) >>> 4);
    final int c3 = ((b2 & 0xf) << 2) | ((b3 & 0xff) >>> 6);
    final int c4 = b3 & 0x3F;
    appendEncoded6Bits(result, c1 & 0x3F);
    appendEncoded6Bits(result, c2 & 0x3F);
    appendEncoded6Bits(result, c3 & 0x3F);
    appendEncoded6Bits(result, c4 & 0x3F);
  }

  private static void appendEncoded6Bits(final StringBuilder result, int b) {
    if(b<10) {
      // numeric
      result.append((char)(48+b));
      return;
    }
    b-=10;
    if(b<26) {
      // UPPERCASE
      result.append((char)(65+b));
      return;
    }
    b-=26;
    if(b<26) {
      // lowercase
      result.append((char)(97+b));
      return;
    }
    b-=26;
    if(b==0) {
      result.append('-');
    }
    else if(b==1) {
      result.append('_');
    }
    else {
      result.append('?');
    }
  }
}
