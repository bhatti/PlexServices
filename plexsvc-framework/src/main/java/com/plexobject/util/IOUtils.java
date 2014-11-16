package com.plexobject.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class defines helper methods for Input/output
 * 
 * @author shahzad bhatti
 *
 */
public class IOUtils {
    public static String toString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        try {

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }
}