/* Copyright (c) 2013, Ian Dees
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.openstreetmap.josm.plugins.notes.api.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.openstreetmap.josm.tools.Utils;

public class HttpUtils {
    public static String get(String url, String charset) throws IOException {
        System.out.println("Fetching GET " + url);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int length = -1;
        byte[] b = new byte[1024];
        InputStream in = Utils.openURL(new URL(url));
        while( (length = in.read(b)) > 0 ) {
            bos.write(b, 0, length);
        }
        Utils.close(in);

        return new String(bos.toByteArray(), charset);
    }

    /**
     *
     * @param url
     * @param content the post body
     * @param responseCharset the expected charset of the response
     * @return
     * @throws IOException
     */
    public static String post(String url, String content, String responseCharset) {
    	try {
    		System.out.println("Fetching POST " + url);
    		// initialize the connection

    		URL page = new URL(url);
    		HttpURLConnection connection = Utils.openHttpConnection(page);

    		NoteConnection noteConnection = new NoteConnection();

    		noteConnection.addAuth(connection);
    		connection.setDoOutput(true);
    		connection.connect();

    		//send the post
    		OutputStream os = connection.getOutputStream();
    		os.write(content.getBytes("UTF-8"));
    		os.flush();

    		// read the response
    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    		int length = -1;
    		byte[] b = new byte[1024];
    		InputStream in = connection.getInputStream();
    		while( (length = in.read(b)) > 0 ) {
    			bos.write(b, 0, length);
    		}
    		Utils.close(in);
    		Utils.close(os);

    		if(connection.getResponseCode() == 200) {
    			return "ok";
    		}
    		return new String(bos.toByteArray(), responseCharset);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		return e.getMessage();
    	}
    }
}
